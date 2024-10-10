package net.kdt.pojavlaunch.modloaders;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class FabriclikeDownloadTask implements Runnable, Tools.DownloaderFeedback {
    private final ModloaderDownloadListener mListener;
    private final FabriclikeUtils mUtils;
    public FabriclikeDownloadTask(ModloaderDownloadListener modloaderDownloadListener, FabriclikeUtils utils) {
        this.mListener = modloaderDownloadListener;
        this.mUtils = utils;
    }

    @Override
    public void run() {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.modloader_dl_progress, mUtils.getName());
        downloadInstaller();
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

    @Override
    public void updateProgress(int curr, int max) {
        int progress100 = (int)(((float)curr / (float)max)*100f);
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, progress100, R.string.modloader_dl_progress, mUtils.getName());
    }
}
