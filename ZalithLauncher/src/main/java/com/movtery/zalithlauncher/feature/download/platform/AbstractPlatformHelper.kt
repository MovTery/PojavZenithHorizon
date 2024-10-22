package com.movtery.zalithlauncher.feature.download.platform

import android.content.Context
import com.kdt.mcgui.ProgressLayout
import com.movtery.zalithlauncher.feature.download.Filters
import com.movtery.zalithlauncher.feature.download.enums.Classify
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.item.ModLoaderWrapper
import com.movtery.zalithlauncher.feature.download.item.SearchResult
import com.movtery.zalithlauncher.feature.download.item.VersionItem
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler
import net.kdt.pojavlaunch.modloaders.modpacks.api.NotificationDownloadListener
import java.io.File

abstract class AbstractPlatformHelper(val api: ApiHandler) {
    var filters: Filters = Filters()
    var currentClassify: Classify = Classify.MOD

    @Throws(Throwable::class)
    fun search(lastResult: SearchResult): SearchResult? {
        return when (currentClassify) {
            Classify.ALL -> throw IllegalArgumentException("Cannot be the enum value ${Classify.ALL}")
            Classify.MOD -> searchMod(lastResult)
            Classify.MODPACK -> searchModPack(lastResult)
            Classify.RESOURCE_PACK -> searchResourcePack(lastResult)
            Classify.WORLD -> searchWorld(lastResult)
        }
    }

    @Throws(Throwable::class)
    fun getVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return when (currentClassify) {
            Classify.ALL -> throw IllegalArgumentException("Cannot be the enum value ${Classify.ALL}")
            Classify.MOD -> getModVersions(infoItem, force)
            Classify.MODPACK -> getModPackVersions(infoItem, force)
            Classify.RESOURCE_PACK -> getResourcePackVersions(infoItem, force)
            Classify.WORLD -> getWorldVersions(infoItem, force)
        }
    }

    @Throws(Throwable::class)
    fun install(context: Context, infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        when (currentClassify) {
            Classify.ALL -> throw IllegalArgumentException("Cannot be the enum value ${Classify.ALL}")
            Classify.MOD -> installMod(infoItem, version, targetPath)
            Classify.RESOURCE_PACK -> installResourcePack(infoItem, version, targetPath)
            Classify.WORLD -> installWorld(infoItem, version, targetPath)
            Classify.MODPACK -> {
                PojavApplication.sExecutorService.execute {
                    runCatching {
                        val modloader = installModPack(infoItem, version) ?: return@execute
                        val task = modloader.getDownloadTask(NotificationDownloadListener(context, modloader))
                        task?.run()
                    }.getOrElse { e ->
                        Tools.showErrorRemote(context, R.string.modpack_install_download_failed, e)
                    }
                }
            }
        }
        ProgressLayout.setProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.generic_waiting)
    }

    abstract fun copy(): AbstractPlatformHelper
    abstract fun getWebUrl(infoItem: InfoItem): String?

    abstract fun searchMod(lastResult: SearchResult): SearchResult?
    abstract fun searchModPack(lastResult: SearchResult): SearchResult?
    abstract fun searchResourcePack(lastResult: SearchResult): SearchResult?
    abstract fun searchWorld(lastResult: SearchResult): SearchResult?

    abstract fun getModVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>?
    abstract fun getModPackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>?
    abstract fun getResourcePackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>?
    abstract fun getWorldVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>?

    abstract fun installMod(infoItem: InfoItem, version: VersionItem, targetPath: File?)
    abstract fun installModPack(infoItem: InfoItem, version: VersionItem): ModLoaderWrapper?
    abstract fun installResourcePack(infoItem: InfoItem, version: VersionItem, targetPath: File?)
    abstract fun installWorld(infoItem: InfoItem, version: VersionItem, targetPath: File?)
}