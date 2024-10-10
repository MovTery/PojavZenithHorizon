package net.kdt.pojavlaunch.tasks;

import com.movtery.pojavzh.event.sticky.MinecraftVersionValueEvent;

import net.kdt.pojavlaunch.JMinecraftVersionList;

import org.greenrobot.eventbus.EventBus;

public class AsyncMinecraftDownloader {
    public static String normalizeVersionId(String versionString) {
        JMinecraftVersionList versionList = getJMinecraftVersionList();
        if(versionList == null || versionList.versions == null) return versionString;
        if("latest-release".equals(versionString)) versionString = versionList.latest.get("release");
        if("latest-snapshot".equals(versionString)) versionString = versionList.latest.get("snapshot");
        return versionString;
    }

    public static JMinecraftVersionList.Version getListedVersion(String normalizedVersionString) {
        JMinecraftVersionList versionList = getJMinecraftVersionList();
        if(versionList == null || versionList.versions == null) return null; // can't have listed versions if there's no list
        for(JMinecraftVersionList.Version version : versionList.versions) {
            if(version.id.equals(normalizedVersionString)) return version;
        }
        return null;
    }

    private static JMinecraftVersionList getJMinecraftVersionList() {
        MinecraftVersionValueEvent event = EventBus.getDefault().getStickyEvent(MinecraftVersionValueEvent.class);

        if (event != null) return event.getList();
        else return null;
    }

    public interface DoneListener{
        void onDownloadDone();
        void onDownloadFailed(Throwable throwable);
    }
}
