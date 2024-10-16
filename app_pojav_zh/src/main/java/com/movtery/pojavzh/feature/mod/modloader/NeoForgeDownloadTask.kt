package com.movtery.pojavzh.feature.mod.modloader

import com.kdt.mcgui.ProgressLayout
import com.movtery.pojavzh.feature.log.Logging
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

class NeoForgeDownloadTask(listener: ModloaderDownloadListener, neoforgeVersion: String) : Runnable, DownloaderFeedback {
    private val mListener: ModloaderDownloadListener = listener
    private var mDownloadUrl: String? = null
    private var mLoaderVersion: String? = neoforgeVersion
    private var mGameVersion: String? = null

    init {
        if (neoforgeVersion.contains("1.20.1")) {
            this.mDownloadUrl = getNeoForgedForgeInstallerUrl(neoforgeVersion)
        } else {
            this.mDownloadUrl = getNeoForgeInstallerUrl(neoforgeVersion)
        }
        Logging.i("NeoForgeDownloadTask", "Version : $mLoaderVersion, Download Url : $mDownloadUrl")
    }

    override fun run() {
        try {
            if (if (mLoaderVersion!!.contains("1.20.1")) determineNeoForgedForgeDownloadUrl() else determineNeoForgeDownloadUrl()) {
                downloadNeoForge()
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
    }

    override fun updateProgress(curr: Int, max: Int) {
        val progress100 = ((curr.toFloat() / max.toFloat()) * 100f).toInt()
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            progress100,
            R.string.mod_download_progress,
            mLoaderVersion
        )
    }

    private fun downloadNeoForge() {
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            0,
            R.string.mod_download_progress,
            mLoaderVersion
        )
        try {
            val destinationFile = File(PathAndUrlManager.DIR_CACHE, "neoforge-installer.jar")
            val buffer = ByteArray(8192)
            DownloadUtils.downloadFileMonitored(mDownloadUrl, destinationFile, buffer, this)
            mListener.onDownloadFinished(destinationFile)
        } catch (_: FileNotFoundException) {
            mListener.onDataNotAvailable()
        } catch (e: IOException) {
            mListener.onDownloadError(e)
        }
    }

    private fun determineDownloadUrl(findVersion: Boolean): Boolean {
        if (mDownloadUrl != null && mLoaderVersion != null) return true
        ProgressKeeper.submitProgress(
            ProgressLayout.INSTALL_MODPACK,
            0,
            R.string.mod_neoforge_searching
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
            mLoaderVersion = versionName
            mDownloadUrl = installerUrl
            return true
        }
        return false
    }

    @Throws(Exception::class)
    fun findNeoForgeVersion(): Boolean {
        return findVersion(downloadNeoForgeVersions(false), getNeoForgeInstallerUrl(mLoaderVersion))
    }

    @Throws(Exception::class)
    fun findNeoForgedForgeVersion(): Boolean {
        return findVersion(
            downloadNeoForgedForgeVersions(false),
            getNeoForgedForgeInstallerUrl(mLoaderVersion)
        )
    }
}