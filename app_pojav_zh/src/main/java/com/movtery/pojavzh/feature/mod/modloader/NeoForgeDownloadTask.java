package com.movtery.pojavzh.feature.mod.modloader;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class NeoForgeDownloadTask implements Runnable, Tools.DownloaderFeedback {
    private final ModloaderDownloadListener mListener;
    private String mDownloadUrl;
    private String mFullVersion;
    private String mLoaderVersion;
    private String mGameVersion;

    public NeoForgeDownloadTask(ModloaderDownloadListener listener, String neoforgeVersion) {
        this.mListener = listener;
        if (neoforgeVersion.contains("1.20.1")) {
            this.mDownloadUrl = NeoForgeUtils.getNeoForgedForgeInstallerUrl(neoforgeVersion);
        } else {
            this.mDownloadUrl = NeoForgeUtils.getNeoForgeInstallerUrl(neoforgeVersion);
        }
        this.mFullVersion = neoforgeVersion;
    }

    public NeoForgeDownloadTask(ModloaderDownloadListener listener, String gameVersion, String loaderVersion) {
        this.mListener = listener;
        this.mLoaderVersion = loaderVersion;
        this.mGameVersion = gameVersion;
    }

    @Override
    public void run() {
        if (this.mFullVersion != null) {
            try {
                if (this.mFullVersion.contains("1.20.1") ? determineNeoForgedForgeDownloadUrl() : determineNeoForgeDownloadUrl()) {
                    downloadNeoForge();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                if (this.mGameVersion.equals("1.20.1") ? determineNeoForgedForgeDownloadUrl() : determineNeoForgeDownloadUrl()) {
                    downloadNeoForge();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

    @Override
    public void updateProgress(int curr, int max) {
        int progress100 = (int) (((float) curr / (float) max) * 100f);
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, progress100, R.string.forge_dl_progress, mFullVersion);
    }

    private void downloadNeoForge() {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.forge_dl_progress, mFullVersion);
        try {
            File destinationFile = new File(Tools.DIR_CACHE, "neoforge-installer.jar");
            byte[] buffer = new byte[8192];
            DownloadUtils.downloadFileMonitored(mDownloadUrl, destinationFile, buffer, this);
            mListener.onDownloadFinished(destinationFile);
        } catch (FileNotFoundException e) {
            mListener.onDataNotAvailable();
        } catch (IOException e) {
            mListener.onDownloadError(e);
        }
    }

    private boolean determineDownloadUrl(boolean findVersion) {
        if (mDownloadUrl != null && mFullVersion != null) return true;
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.zh_neoforge_dl_searching);
        if (!findVersion) {
            mListener.onDataNotAvailable();
            return false;
        }
        return true;
    }

    public boolean determineNeoForgeDownloadUrl() throws Exception {
        return determineDownloadUrl(findNeoForgeVersion());
    }

    public boolean determineNeoForgedForgeDownloadUrl() throws Exception {
        return determineDownloadUrl(findNeoForgedForgeVersion());
    }

    private boolean findVersion(List<String> neoForgeUtils, String installerUrl) {
        if (neoForgeUtils == null) return false;
        String versionStart = mGameVersion + "-" + mLoaderVersion;
        for (String versionName : neoForgeUtils) {
            if (!versionName.startsWith(versionStart)) continue;
            mFullVersion = versionName;
            mDownloadUrl = installerUrl;
            return true;
        }
        return false;
    }

    public boolean findNeoForgeVersion() throws Exception {
        return findVersion(NeoForgeUtils.downloadNeoForgeVersions(), NeoForgeUtils.getNeoForgeInstallerUrl(mFullVersion));
    }

    public boolean findNeoForgedForgeVersion() throws Exception {
        return findVersion(NeoForgeUtils.downloadNeoForgedForgeVersions(), NeoForgeUtils.getNeoForgedForgeInstallerUrl(mFullVersion));
    }

}