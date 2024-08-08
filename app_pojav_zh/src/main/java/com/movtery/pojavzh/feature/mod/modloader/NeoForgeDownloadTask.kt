package com.movtery.pojavzh.feature.mod.modloader

import com.kdt.mcgui.ProgressLayout
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgedForgeVersions
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.getNeoForgeInstallerUrl
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.getNeoForgedForgeInstallerUrl
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools.DownloaderFeedback
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.utils.DownloadUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class NeoForgeDownloadTask : Runnable, DownloaderFeedback {
    private val mListener: ModloaderDownloadListener
    private var mDownloadUrl: String? = null
    private var mFullVersion: String? = null
    private var mLoaderVersion: String? = null
    private var mGameVersion: String? = null

    constructor(listener: ModloaderDownloadListener, neoforgeVersion: String) {
        this.mListener = listener
        if (neoforgeVersion.contains("1.20.1")) {
            this.mDownloadUrl = getNeoForgedForgeInstallerUrl(neoforgeVersion)
        } else {
            this.mDownloadUrl = getNeoForgeInstallerUrl(neoforgeVersion)
        }
        this.mFullVersion = neoforgeVersion
    }

    constructor(listener: ModloaderDownloadListener, gameVersion: String?, loaderVersion: String?) {
        this.mListener = listener
        this.mLoaderVersion = loaderVersion
        this.mGameVersion = gameVersion
    }

    override fun run() {
        if (this.mFullVersion != null) {
            try {
                if (if (mFullVersion!!.contains("1.20.1")) determineNeoForgedForgeDownloadUrl() else determineNeoForgeDownloadUrl()) {
                    downloadNeoForge()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else {
            try {
                if (if (this.mGameVersion == "1.20.1") determineNeoForgedForgeDownloadUrl() else determineNeoForgeDownloadUrl()) {
                    downloadNeoForge()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
    }

    override fun updateProgress(curr: Int, max: Int) {
        val progress100 = ((curr.toFloat() / max.toFloat()) * 100f).toInt()
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            progress100,
            R.string.forge_dl_progress,
            mFullVersion
        )
    }

    private fun downloadNeoForge() {
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            0,
            R.string.forge_dl_progress,
            mFullVersion
        )
        try {
            val destinationFile = File(PathAndUrlManager.DIR_CACHE, "neoforge-installer.jar")
            val buffer = ByteArray(8192)
            DownloadUtils.downloadFileMonitored(mDownloadUrl, destinationFile, buffer, this)
            mListener.onDownloadFinished(destinationFile)
        } catch (e: FileNotFoundException) {
            mListener.onDataNotAvailable()
        } catch (e: IOException) {
            mListener.onDownloadError(e)
        }
    }

    private fun determineDownloadUrl(findVersion: Boolean): Boolean {
        if (mDownloadUrl != null && mFullVersion != null) return true
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            0,
            R.string.zh_neoforge_dl_searching
        )
        if (!findVersion) {
            mListener.onDataNotAvailable()
            return false
        }
        return true
    }

    @Throws(Exception::class)
    fun determineNeoForgeDownloadUrl(): Boolean {
        return determineDownloadUrl(findNeoForgeVersion())
    }

    @Throws(Exception::class)
    fun determineNeoForgedForgeDownloadUrl(): Boolean {
        return determineDownloadUrl(findNeoForgedForgeVersion())
    }

    private fun findVersion(neoForgeUtils: List<String>?, installerUrl: String): Boolean {
        if (neoForgeUtils == null) return false
        val versionStart = "$mGameVersion-$mLoaderVersion"
        for (versionName in neoForgeUtils) {
            if (!versionName.startsWith(versionStart)) continue
            mFullVersion = versionName
            mDownloadUrl = installerUrl
            return true
        }
        return false
    }

    @Throws(Exception::class)
    fun findNeoForgeVersion(): Boolean {
        return findVersion(downloadNeoForgeVersions(), getNeoForgeInstallerUrl(mFullVersion))
    }

    @Throws(Exception::class)
    fun findNeoForgedForgeVersion(): Boolean {
        return findVersion(
            downloadNeoForgedForgeVersions(),
            getNeoForgedForgeInstallerUrl(mFullVersion)
        )
    }
}