package com.movtery.zalithlauncher.feature.download.platform.modrinth

import com.movtery.zalithlauncher.feature.download.enums.Classify
import com.movtery.zalithlauncher.feature.download.install.InstallHelper
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.item.ModLoaderWrapper
import com.movtery.zalithlauncher.feature.download.item.SearchResult
import com.movtery.zalithlauncher.feature.download.item.VersionItem
import com.movtery.zalithlauncher.feature.download.platform.AbstractPlatformHelper
import com.movtery.zalithlauncher.feature.download.platform.PlatformNotSupportedException
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler
import java.io.File

class ModrinthHelper : AbstractPlatformHelper(ApiHandler("https://api.modrinth.com/v2")) {
    override fun copy(): AbstractPlatformHelper {
        val new = ModrinthHelper()
        new.filters = this.filters
        new.currentClassify = this.currentClassify
        return new
    }

    override fun getWebUrl(infoItem: InfoItem): String? {
        return "https://modrinth.com/${
            when (currentClassify) {
                Classify.ALL -> return null
                Classify.MOD -> "mod"
                Classify.MODPACK -> "modpack"
                Classify.RESOURCE_PACK -> "resourcepacks"
                Classify.WORLD -> return null
            }
        }/${infoItem.projectId}"
    }

    @Throws(Throwable::class)
    override fun searchMod(lastResult: SearchResult): SearchResult? {
        return ModrinthModHelper.modLikeSearch(api, lastResult, filters, "mod")
    }

    @Throws(Throwable::class)
    override fun searchModPack(lastResult: SearchResult): SearchResult? {
        return ModrinthModHelper.modLikeSearch(api, lastResult, filters, "modpack")
    }

    @Throws(Throwable::class)
    override fun searchResourcePack(lastResult: SearchResult): SearchResult? {
        return ModrinthCommonUtils.getResults(api, lastResult, filters, "resourcepack")
    }

    @Throws(Throwable::class)
    override fun searchWorld(lastResult: SearchResult): SearchResult? {
        throw PlatformNotSupportedException("Modrinth does not provide archive download support.") //Modrinth不提供MC存档
    }

    @Throws(Throwable::class)
    override fun getModVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return ModrinthModHelper.getModVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getModPackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return ModrinthModHelper.getModPackVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getResourcePackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return ModrinthCommonUtils.getVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getWorldVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        throw PlatformNotSupportedException("Modrinth does not provide archive download support.") //Modrinth不提供MC存档
    }

    @Throws(Throwable::class)
    override fun installMod(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        InstallHelper.downloadFile(version, targetPath)
    }

    @Throws(Throwable::class)
    override fun installModPack(infoItem: InfoItem, version: VersionItem): ModLoaderWrapper? {
        return ModrinthModPackInstallHelper.startInstall(infoItem.copy(), version)
    }

    @Throws(Throwable::class)
    override fun installResourcePack(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        InstallHelper.downloadFile(version, targetPath)
    }

    @Throws(Throwable::class)
    override fun installWorld(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        throw PlatformNotSupportedException("Modrinth does not provide archive download support.")
    }
}