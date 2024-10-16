package net.kdt.pojavlaunch.modloaders;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class FabriclikeDownloadTask implements Runnable, Tools.DownloaderFeedback {
    private final ModloaderDownloadListener mListener;
    private final FabriclikeUtils mUtils;
    private String mGameVersion = null;
    private String mLoaderVersion = null;

    public FabriclikeDownloadTask(ModloaderDownloadListener modloaderDownloadListener, FabriclikeUtils utils) {
        this.mListener = modloaderDownloadListener;
        this.mUtils = utils;
    }

    public FabriclikeDownloadTask(ModloaderDownloadListener modloaderDownloadListener, FabriclikeUtils utils, String gameVersion, String loaderVersion) {
        this(modloaderDownloadListener, utils);
        this.mGameVersion = gameVersion;
        this.mLoaderVersion = loaderVersion;
    }

    @Override
    public void run() {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.mod_download_progress, mUtils.getName());
        if (mGameVersion == null && mLoaderVersion == null) downloadInstaller();
        else legacyInstall();
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

    private void downloadInstaller() {
        try {
            File outputFile = new File(PathAndUrlManager.DIR_CACHE, "fabric-installer.jar");

            String installerDownloadUrl = mUtils.getInstallerDownloadUrl();
            byte[] buffer = new byte[8192];
            DownloadUtils.downloadFileMonitored(installerDownloadUrl, outputFile, buffer, this);

            mListener.onDownloadFinished(outputFile);
        } catch (FileNotFoundException e) {
            mListener.onDataNotAvailable();
        } catch (Exception e) {
            mListener.onDownloadError(e);
        }
    }

    private void legacyInstall() {
        try {
            String jsonString = DownloadUtils.downloadString(mUtils.createJsonDownloadUrl(mGameVersion, mLoaderVersion));

            JSONObject jsonObject = new JSONObject(jsonString);
            String versionId = jsonObject.getString("id");

            File versionJsonDir = new File(ProfilePathHome.getVersionsHome(), versionId);
            File versionJsonFile = new File(versionJsonDir, versionId+".json");
            FileUtils.ensureDirectory(versionJsonDir);
            Tools.write(versionJsonFile.getAbsolutePath(), jsonString);

            LauncherProfiles.load();
            MinecraftProfile profile = new MinecraftProfile();
            profile.lastVersionId = versionId;
            profile.name = mUtils.getName();
            profile.icon = mUtils.getIconName();
            LauncherProfiles.insertMinecraftProfile(profile);
            LauncherProfiles.write(ProfilePathManager.getCurrentProfile());

            mListener.onDownloadFinished(null);
        } catch (Exception e) {
            mListener.onDownloadError(e);
        }
    }

    @Override
    public void updateProgress(int curr, int max) {
        int progress100 = (int)(((float)curr / (float)max)*100f);
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, progress100, R.string.mod_download_progress, mUtils.getName());
    }
}
