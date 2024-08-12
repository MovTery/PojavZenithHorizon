package net.kdt.pojavlaunch.tasks;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;
import static net.kdt.pojavlaunch.utils.DownloadUtils.downloadString;

import androidx.annotation.Nullable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/** Class getting the version list, and that's all really */
public class AsyncVersionList {

    public void getVersionList(@Nullable VersionDoneListener listener, boolean secondPass){
        sExecutorService.execute(() -> {
            File versionFile = new File(PathAndUrlManager.DIR_DATA + "/version_list.json");
            JMinecraftVersionList versionList = null;
            try{
                if(!versionFile.exists() || (ZHTools.getCurrentTimeMillis() > versionFile.lastModified() + 86400000 )){
                    versionList = downloadVersionList(LauncherPreferences.PREF_VERSION_REPOS);
                }
            }catch (Exception e){
                Logging.e("AsyncVersionList", "Refreshing version list failed :" + e);
                Logging.e("GetVersionList", Tools.printToString(e));
            }

            // Fallback when no network or not needed
            if (versionList == null) {
                try {
                    versionList = Tools.GLOBAL_GSON.fromJson(new JsonReader(new FileReader(versionFile)), JMinecraftVersionList.class);
                } catch (FileNotFoundException e) {
                    Logging.e("File Not Found", Tools.printToString(e));
                } catch (JsonIOException | JsonSyntaxException e) {
                    Logging.e("AsyncVersionList", Tools.printToString(e));
                    versionFile.delete();
                    if(!secondPass)
                        getVersionList(listener, true);
                }
            }

            if(listener != null)
                listener.onVersionDone(versionList);
        });
    }


    @SuppressWarnings("SameParameterValue")
    private JMinecraftVersionList downloadVersionList(String mirror){
        JMinecraftVersionList list = null;
        try{
            Logging.i("ExtVL", "Syncing to external: " + mirror);
            String jsonString = downloadString(mirror);
            list = Tools.GLOBAL_GSON.fromJson(jsonString, JMinecraftVersionList.class);
            Logging.i("ExtVL","Downloaded the version list, len=" + list.versions.length);

            // Then save the version list
            //TODO make it not save at times ?
            FileOutputStream fos = new FileOutputStream(PathAndUrlManager.DIR_DATA + "/version_list.json");
            fos.write(jsonString.getBytes());
            fos.close();



        }catch (IOException e){
            Logging.e("AsyncVersionList", e.toString());
        }
        return list;
    }

    /** Basic listener, acting as a callback */
    public interface VersionDoneListener{
        void onVersionDone(JMinecraftVersionList versions);
    }

}
