package com.movtery.pojavzh.feature.download.platform.modrinth

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
import com.movtery.pojavzh.feature.mod.ModMirror
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler

class ModrinthModHelper {
    companion object {
        @Throws(Throwable::class)
        internal fun modLikeSearch(api: ApiHandler, lastResult: SearchResult, filters: Filters, type: String): SearchResult? {
            if (filters.category != Category.ALL && filters.category.modrinthName == null) {
                throw PlatformNotSupportedException("The platform does not support the ${filters.category} category!")
            }

            val response = api.get("search",
                ModrinthCommonUtils.getParams(lastResult, filters, type), JsonObject::class.java) ?: return null
            val responseHits = response.getAsJsonArray("hits") ?: return null

            val infoItems: MutableList<InfoItem> = ArrayList()
            responseHit@for (responseHit in responseHits) {
                val hit = responseHit.asJsonObject

                val categories = hit.get("categories").asJsonArray
                val modloaders: MutableList<ModLoader> = ArrayList()
                for (category in categories) {
                    val string = category.asString
                    if (string == "datapack") continue@responseHit //这里经常能搜到数据包，很奇怪...
                    ModLoaderUtils.getModLoaderByModrinth(string)?.let { modloaders.add(it) }
                }

                infoItems.add(
                    ModInfoItem(
                        Platform.MODRINTH,
                        hit.get("project_id").asString,
                        arrayOf(hit.get("author").asString),
                        hit.get("title").asString,
                        hit.get("description").asString,
                        hit.get("downloads").asLong,
                        ZHTools.getDate(hit.get("date_created").asString),
                        ModrinthCommonUtils.getIconUrl(hit),
                        ModrinthCommonUtils.getAllCategories(hit).toList(),
                        modloaders
                    )
                )
            }

            return ModrinthCommonUtils.returnResults(lastResult, infoItems, response, responseHits)
        }

        @Throws(Throwable::class)
        internal fun getModVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<VersionItem>? {
            return ModrinthCommonUtils.getCommonVersions(
                api, infoItem, force, InfoCache.ModVersionCache
            ) { versionObject, filesJsonObject, invalidDependencies ->
                val dependencies = versionObject.get("dependencies").asJsonArray
                val dependencyInfoItems: MutableList<DependenciesInfoItem> = ArrayList()
                if (dependencies.size() != 0) {
                    for (dependency in dependencies) {
                        val dObject = dependency.asJsonObject
                        val dProjectId = dObject.get("project_id").asString
                        val dependencyType = dObject.get("dependency_type").asString

                        if (invalidDependencies.contains(dProjectId)) continue
                        if (!InfoCache.DependencyInfoCache.containsKey(api, dProjectId)) {
                            val hit = ModrinthCommonUtils.searchModFromID(api, dProjectId)
                            if (hit != null) {
                                InfoCache.DependencyInfoCache.put(
                                    api, dProjectId, DependenciesInfoItem(
                                        Platform.MODRINTH,
                                        dProjectId,
                                        null,
                                        hit.get("title").asString,
                                        hit.get("description").asString,
                                        hit.get("downloads").asLong,
                                        ZHTools.getDate(hit.get("published").asString),
                                        ModrinthCommonUtils.getIconUrl(hit),
                                        ModrinthCommonUtils.getAllCategories(hit).toList(),
                                        getModLoaders(hit.getAsJsonArray("loaders")),
                                        DependencyUtils.getDependencyType(dependencyType)
                                    )
                                )
                            } else invalidDependencies.add(dProjectId)
                        }
                        InfoCache.DependencyInfoCache.get(api, dProjectId)?.let {
                            dependencyInfoItems.add(it)
                        }
                    }
                }
                ModVersionItem(
                        infoItem.projectId,
                        versionObject.get("name").asString,
                        versionObject.get("downloads").asLong,
                        ZHTools.getDate(versionObject.get("date_published").asString),
                        ModrinthCommonUtils.getMcVersions(versionObject.getAsJsonArray("game_versions")),
                        VersionTypeUtils.getVersionType(versionObject.get("version_type").asString),
                        filesJsonObject.get("filename").asString,
                        ModrinthCommonUtils.getSha1Hash(filesJsonObject),
                        ModMirror.replaceMirrorDownloadUrl(filesJsonObject.get("url").asString),
                        getModLoaders(versionObject.getAsJsonArray("loaders")),
                        dependencyInfoItems
                    )
            }
        }

        @Throws(Throwable::class)
        internal fun getModPackVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<ModLikeVersionItem>? {
            return ModrinthCommonUtils.getCommonVersions(
                api, infoItem, force, InfoCache.ModPackVersionCache
            ) { versionObject, filesJsonObject, _ ->
                ModLikeVersionItem(
                    infoItem.projectId,
                    versionObject.get("name").asString,
                    versionObject.get("downloads").asLong,
                    ZHTools.getDate(versionObject.get("date_published").asString),
                    ModrinthCommonUtils.getMcVersions(versionObject.getAsJsonArray("game_versions")),
                    VersionTypeUtils.getVersionType(versionObject.get("version_type").asString),
                    filesJsonObject.get("filename").asString,
                    ModrinthCommonUtils.getSha1Hash(filesJsonObject),
                    ModMirror.replaceMirrorDownloadUrl(filesJsonObject.get("url").asString),
                    getModLoaders(versionObject.getAsJsonArray("loaders"))
                )
            }
        }

        private fun getModLoaders(jsonArray: JsonArray): List<ModLoader> {
            val modLoaders: MutableList<ModLoader> = ArrayList()
            jsonArray.forEach {
                ModLoaderUtils.getModLoader(it.asString)?.let {
                    ml -> modLoaders.add(ml)
                }
            }
            return modLoaders
        }
    }
}