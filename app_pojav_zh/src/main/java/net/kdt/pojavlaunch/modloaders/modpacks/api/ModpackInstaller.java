package net.kdt.pojavlaunch.modloaders.modpacks.api;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.progresskeeper.DownloaderProgressWrapper;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

public class ModpackInstaller {
    public static ModLoader installMod(ModDetail modDetail, String path, ModVersionItem modVersionItem) throws IOException {
        String modFileName = "[" + modDetail.title + "] " + modVersionItem.name;

        File modFile = new File(path, modFileName.replace("/", "-"));

        try {
            byte[] downloadBuffer = new byte[8192];
            DownloadUtils.ensureSha1(modFile, modVersionItem.versionHash, (Callable<Void>) () -> {
                Logging.i("ModpackInstaller", "Download Url: " + modVersionItem.downloadUrl);
                DownloadUtils.downloadFileMonitored(modVersionItem.downloadUrl, modFile, downloadBuffer,
                        new DownloaderProgressWrapper(R.string.modpack_download_downloading_mods,
                                ProgressLayout.INSTALL_MODPACK));
                return null;
            });
        } finally {
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
        }

        return null;
    }


    public static ModLoader installModpack(ModDetail modDetail, ModVersionItem modVersionItem, ModpackInstaller. InstallFunction installFunction) throws IOException {
        String modpackName = modDetail.title.toLowerCase(Locale.ROOT).trim().replace(" ", "_" );

        // Build a new minecraft instance, folder first

        // Get the modpack file
        File modpackFile = new File(PathAndUrlManager.DIR_CACHE, modpackName.replace("/", "-") + ".cf"); // Cache File
        ModLoader modLoaderInfo;
        try {
            byte[] downloadBuffer = new byte[8192];
            DownloadUtils.ensureSha1(modpackFile, modVersionItem.versionHash, (Callable<Void>) () -> {
                Logging.i("ModpackInstaller", "Download Url: " + modVersionItem.downloadUrl);
                DownloadUtils.downloadFileMonitored(modVersionItem.downloadUrl, modpackFile, downloadBuffer,
                        new DownloaderProgressWrapper(R.string.modpack_download_downloading_metadata,
                                ProgressLayout.INSTALL_MODPACK));
                return null;
            });

            // Install the modpack
            modLoaderInfo = installFunction.installModpack(modpackFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/"+modpackName));

        } finally {
            FileUtils.deleteQuietly(modpackFile);
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
        }
        if(modLoaderInfo == null) {
            return null;
        }

        // Create the instance
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "./custom_instances/" + modpackName;
        profile.name = modDetail.title;
        profile.lastVersionId = modLoaderInfo.getVersionId();
        profile.icon = ModIconCache.getBase64Image(modDetail.getIconCacheTag());


        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.write(ProfilePathManager.getCurrentProfile());

        return modLoaderInfo;
    }

    interface InstallFunction {
        ModLoader installModpack(File modpackFile, File instanceDestination) throws IOException;
    }
}
