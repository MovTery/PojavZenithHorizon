package com.movtery.pojavzh.feature.download.platform.curseforge

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.movtery.pojavzh.feature.download.Filters
import com.movtery.pojavzh.feature.download.InfoCache
import com.movtery.pojavzh.feature.download.enums.Category
import com.movtery.pojavzh.feature.download.enums.ModLoader
import com.movtery.pojavzh.feature.download.enums.Platform
import com.movtery.pojavzh.feature.download.item.DependenciesInfoItem
import com.movtery.pojavzh.feature.download.item.InfoItem
import com.movtery.pojavzh.feature.download.item.ModInfoItem
import com.movtery.pojavzh.feature.download.item.ModLikeVersionItem
import com.movtery.pojavzh.feature.download.item.ModVersionItem
import com.movtery.pojavzh.feature.download.item.SearchResult
import com.movtery.pojavzh.feature.download.item.VersionItem
import com.movtery.pojavzh.feature.download.platform.PlatformNotSupportedException
import com.movtery.pojavzh.feature.download.utils.DependencyUtils
import com.movtery.pojavzh.feature.download.utils.ModLoaderUtils
import com.movtery.pojavzh.feature.download.utils.VersionTypeUtils
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.utils.MCVersionRegex.Companion.RELEASE_REGEX
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler
import net.kdt.pojavlaunch.utils.GsonJsonUtils
import java.io.IOException
import java.util.TreeSet

class CurseForgeModHelper {
    companion object {
        @Throws(Throwable::class)
        internal fun modLikeSearch(api: ApiHandler, lastResult: SearchResult, filters: Filters, type: Int): SearchResult? {
            if (filters.category != Category.ALL && filters.category.curseforgeID == null) {
                throw PlatformNotSupportedException("The platform does not support the ${filters.category} category!")
            }

            val params = HashMap<String, Any>()
            CurseForgeCommonUtils.putDefaultParams(params, filters, lastResult.previousCount)
            params["classId"] = type
            filters.modloader?.let { params["modLoaderTypes"] = "[${it.curseforgeId}]" }

            val response = api.get("mods/search", params, JsonObject::class.java) ?: return null
            val dataArray = response.getAsJsonArray("data") ?: return null

            val infoItems: MutableList<InfoItem> = ArrayList()
            for (data in dataArray) {
                val dataElement = data.asJsonObject
                CurseForgeCommonUtils.getInfoItem(dataElement)?.let { item ->
                    infoItems.add(
                        ModInfoItem(
                            Platform.CURSEFORGE,
                            item.projectId,
                            item.author,
                            item.title,
                            item.description,
                            item.downloadCount,
                            item.uploadDate,
                            item.iconUrl,
                            item.category,
                            getModLoaders(dataElement.getAsJsonArray("latestFilesIndexes"))
                        )
                    )
                }
            }

            return CurseForgeCommonUtils.returnResults(lastResult, infoItems, dataArray, response)
        }

        @Throws(Throwable::class)
        internal fun <T : VersionItem> getModOrModPackVersions(
            api: ApiHandler,
            infoItem: InfoItem,
            force: Boolean,
            cache: InfoCache.CacheBase<MutableList<T>>,
            createVersionItem: (
                projectId: String,
                title: String,
                downloadCount: Long,
                fileDate: String,
                mcVersions: List<String>,
                releaseType: String,
                fileHash: String?,
                downloadUrl: String,
                modloaders: List<ModLoader>,
                fileName: String,
                dependencies: List<DependenciesInfoItem>?
            ) -> T
        ): List<T>? {
            if (!force && cache.containsKey(api, infoItem.projectId))
                return cache.get(api, infoItem.projectId)

            val allModData: List<JsonObject>
            try {
                allModData = CurseForgeCommonUtils.getPaginatedData(api, infoItem.projectId)
            } catch (e: IOException) {
                e("CurseForgeModHelper", Tools.printToString(e))
                return null
            }

            val versionItems: MutableList<T> = ArrayList()
            val invalidDependencies: MutableList<String> = ArrayList()
            for (modData in allModData) {
                // 获取版本信息
                val mcVersions: MutableSet<String> = TreeSet()
                for (gameVersionElement in modData.getAsJsonArray("gameVersions")) {
                    val gameVersion = gameVersionElement.asString
                    mcVersions.add(gameVersion)
                }

                val modloaders: MutableList<ModLoader> = ArrayList()
                mcVersions.forEach { ModLoaderUtils.addModLoaderToList(modloaders, it) }

                // 过滤非MC版本的元素
                val releaseRegex = RELEASE_REGEX
                val nonMCVersion: MutableSet<String> = TreeSet()
                mcVersions.forEach { string: String ->
                    if (!releaseRegex.matcher(string).find()) nonMCVersion.add(string)
                }
                if (nonMCVersion.isNotEmpty()) mcVersions.removeAll(nonMCVersion)

                val dependencies = modData.get("dependencies")?.asJsonArray
                val dependencyInfoList: MutableList<DependenciesInfoItem> = ArrayList()
                if (dependencies != null && dependencies.size() != 0) {
                    for (dependency in dependencies) {
                        val dObject = dependency.asJsonObject
                        val modId = dObject.get("modId").asString
                        if (invalidDependencies.contains(modId)) continue

                        if (!InfoCache.DependencyInfoCache.containsKey(api, modId)) {
                            val response = CurseForgeCommonUtils.searchModFromID(api, modId)
                            val hit = GsonJsonUtils.getJsonObjectSafe(response, "data")

                            if (hit != null) {
                                val dModLoaders = getModLoaders(hit.getAsJsonArray("latestFilesIndexes"))
                                InfoCache.DependencyInfoCache.put(
                                    api, modId, DependenciesInfoItem(
                                        Platform.CURSEFORGE,
                                        modId,
                                        CurseForgeCommonUtils.getAuthors(hit.get("authors").asJsonArray).toTypedArray(),
                                        hit.get("name").asString,
                                        hit.get("summary").asString,
                                        hit.get("downloadCount").asLong,
                                        ZHTools.getDate(hit.get("dateCreated").asString),
                                        CurseForgeCommonUtils.getIconUrl(hit),
                                        CurseForgeCommonUtils.getAllCategories(hit).toList(),
                                        dModLoaders,
                                        DependencyUtils.getDependencyType(dObject.get("relationType").asString)
                                    )
                                )
                            } else invalidDependencies.add(modId)
                        }

                        val cacheItem = InfoCache.DependencyInfoCache.get(api, modId)
                        cacheItem?.let { dependencyInfoList.add(it) }
                    }
                }

                versionItems.add(
                    createVersionItem(
                        infoItem.projectId,
                        modData.get("displayName").asString,
                        modData.get("downloadCount").asLong,
                        modData.get("fileDate").asString,
                        mcVersions.toList(),
                        modData.get("releaseType").asString,
                        CurseForgeCommonUtils.getSha1FromData(modData),
                        modData.get("downloadUrl").asString,
                        modloaders,
                        modData.get("fileName").asString,
                        dependencyInfoList.ifEmpty { null }
                    )
                )
            }

            cache.put(api, infoItem.projectId, versionItems)
            return versionItems
        }

        @Throws(Throwable::class)
        internal fun getModVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<ModVersionItem>? {
            return getModOrModPackVersions(
                api,
                infoItem,
                force,
                InfoCache.ModVersionCache
            ) { projectId, title, downloadCount, fileDate, mcVersions, releaseType, fileHash, downloadUrl, modloaders, fileName, dependencies ->
                ModVersionItem(
                    projectId,
                    title,
                    downloadCount,
                    ZHTools.getDate(fileDate),
                    mcVersions,
                    VersionTypeUtils.getVersionType(releaseType),
                    fileName,
                    fileHash,
                    downloadUrl,
                    modloaders,
                    dependencies ?: emptyList()
                )
            }
        }

        @Throws(Throwable::class)
        internal fun getModPackVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<ModLikeVersionItem>? {
            return getModOrModPackVersions(
                api,
                infoItem,
                force,
                InfoCache.ModPackVersionCache
            ) { projectId, title, downloadCount, fileDate, mcVersions, releaseType, fileHash, downloadUrl, modloaders, fileName, _ ->
                ModLikeVersionItem(
                    projectId,
                    title,
                    downloadCount,
                    ZHTools.getDate(fileDate),
                    mcVersions,
                    VersionTypeUtils.getVersionType(releaseType),
                    fileName,
                    fileHash,
                    downloadUrl,
                    modloaders
                )
            }
        }

        private fun getModLoaders(data: JsonArray): List<ModLoader> {
            val modLoaders: MutableSet<ModLoader> = HashSet()
            for (element in data) {
                val jsonObject = element.asJsonObject
                val modloader = jsonObject.get("modLoader")?.asString ?: continue
                ModLoaderUtils.getModLoaderByCurseForge(modloader)?.let {
                    modLoaders.add(it)
                }
            }
            return modLoaders.toList()
        }
    }
}