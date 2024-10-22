package com.movtery.pojavzh.feature.download.platform.curseforge

import com.google.gson.JsonObject
import com.movtery.pojavzh.feature.download.install.InstallHelper
import com.movtery.pojavzh.feature.download.install.UnpackWorldZipHelper
import com.movtery.pojavzh.feature.download.item.InfoItem
import com.movtery.pojavzh.feature.download.item.ModLoaderWrapper
import com.movtery.pojavzh.feature.download.item.SearchResult
import com.movtery.pojavzh.feature.download.item.VersionItem
import com.movtery.pojavzh.feature.download.platform.AbstractPlatformHelper
import com.movtery.pojavzh.feature.download.platform.curseforge.CurseForgeCommonUtils.Companion.CURSEFORGE_MODPACK_CLASS_ID
import com.movtery.pojavzh.feature.download.platform.curseforge.CurseForgeCommonUtils.Companion.CURSEFORGE_MOD_CLASS_ID
import com.movtery.pojavzh.feature.download.utils.PlatformUtils
import net.kdt.pojavlaunch.utils.GsonJsonUtils
import java.io.File

class CurseForgeHelper : AbstractPlatformHelper(PlatformUtils.createCurseForgeApi()) {
    override fun copy(): AbstractPlatformHelper {
        val new = CurseForgeHelper()
        new.filters = this.filters
        new.currentClassify = this.currentClassify
        return new
    }

    override fun getWebUrl(infoItem: InfoItem): String? {
        val response: JsonObject = CurseForgeCommonUtils.searchModFromID(api, infoItem.projectId)
        val hit = GsonJsonUtils.getJsonObjectSafe(response, "data")
        if (hit != null) {
            val links = hit.getAsJsonObject("links")
            return links["websiteUrl"].asString
        }
        return null
    }

    @Throws(Throwable::class)
    override fun searchMod(lastResult: SearchResult): SearchResult? {
        return CurseForgeModHelper.modLikeSearch(api, lastResult, filters, CURSEFORGE_MOD_CLASS_ID)
    }

    @Throws(Throwable::class)
    override fun searchModPack(lastResult: SearchResult): SearchResult? {
        return CurseForgeModHelper.modLikeSearch(api, lastResult, filters, CURSEFORGE_MODPACK_CLASS_ID)
    }

    @Throws(Throwable::class)
    override fun searchResourcePack(lastResult: SearchResult): SearchResult? {
        return CurseForgeCommonUtils.getResults(api, lastResult, filters, 12)
    }

    @Throws(Throwable::class)
    override fun searchWorld(lastResult: SearchResult): SearchResult? {
        return CurseForgeCommonUtils.getResults(api, lastResult, filters, 17)
    }

    @Throws(Throwable::class)
    override fun getModVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return CurseForgeModHelper.getModVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getModPackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return CurseForgeModHelper.getModPackVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getResourcePackVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return CurseForgeCommonUtils.getVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun getWorldVersions(infoItem: InfoItem, force: Boolean): List<VersionItem>? {
        return CurseForgeCommonUtils.getVersions(api, infoItem, force)
    }

    @Throws(Throwable::class)
    override fun installMod(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        InstallHelper.downloadFile(version, targetPath)
    }

    @Throws(Throwable::class)
    override fun installModPack(infoItem: InfoItem, version: VersionItem): ModLoaderWrapper? {
        return CurseForgeModPackInstallHelper.startInstall(api, infoItem.copy(), version)
    }

    @Throws(Throwable::class)
    override fun installResourcePack(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        InstallHelper.downloadFile(version, targetPath)
    }

    @Throws(Throwable::class)
    override fun installWorld(infoItem: InfoItem, version: VersionItem, targetPath: File?) {
        InstallHelper.downloadFile(version, targetPath) { file ->
            targetPath!!.parentFile?.let { UnpackWorldZipHelper.unpackFile(file, it) }
        }
    }
}