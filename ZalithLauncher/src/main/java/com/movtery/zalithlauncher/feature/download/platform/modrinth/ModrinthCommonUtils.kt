package com.movtery.zalithlauncher.feature.download.platform.modrinth

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.movtery.zalithlauncher.feature.download.Filters
import com.movtery.zalithlauncher.feature.download.InfoCache
import com.movtery.zalithlauncher.feature.download.enums.Category
import com.movtery.zalithlauncher.feature.download.enums.Platform
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.item.ScreenshotItem
import com.movtery.zalithlauncher.feature.download.item.SearchResult
import com.movtery.zalithlauncher.feature.download.item.VersionItem
import com.movtery.zalithlauncher.feature.download.platform.PlatformNotSupportedException
import com.movtery.zalithlauncher.feature.download.utils.CategoryUtils
import com.movtery.zalithlauncher.feature.download.utils.VersionTypeUtils
import com.movtery.zalithlauncher.utils.ZHTools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler
import java.util.StringJoiner
import java.util.TreeSet

class ModrinthCommonUtils {
    companion object {
        private const val MODRINTH_SEARCH_COUNT = 20

        private fun getCategories(filters: Filters): String {
            val categories = mutableListOf<String>().apply {
                filters.modloader?.let { add(it.modrinthName) }
                if (filters.category != Category.ALL) {
                    add(filters.category.modrinthName!!)
                }
            }
            return if (categories.isEmpty()) ""
            else categories.joinToString(prefix = "[", postfix = "]") { "\"categories:$it\"" }
        }

        private fun putDefaultParams(params: HashMap<String, Any>, filters: Filters, previousCount: Int) {
            params["query"] = filters.name
            params["limit"] = MODRINTH_SEARCH_COUNT
            params["index"] = filters.sort.modrinth
            params["offset"] = previousCount
        }

        internal fun getAllCategories(hit: JsonObject): Set<Category> {
            val list: MutableSet<Category> = TreeSet()
            for (categories in hit["categories"].asJsonArray) {
                val name = categories.asString
                CategoryUtils.getCategoryByModrinth(name)?.let { list.add(it) }
            }
            return list
        }

        internal fun getIconUrl(hit: JsonObject): String? {
            return runCatching {
                hit.get("icon_url").asString
            }.getOrNull()
        }

        internal fun getScreenshots(hit: JsonObject): List<ScreenshotItem> {
            val screenshotItems: MutableList<ScreenshotItem> = ArrayList()
            hit.getAsJsonArray("gallery").forEach { element ->
                screenshotItems.add(
                    if (element.isJsonObject) {
                        val screenshotObject = element.asJsonObject
                        ScreenshotItem(
                            screenshotObject.get("url").asString,
                            screenshotObject.get("title").asString.takeIf { it.isNotEmpty() && it.isNotBlank() }
                        )
                    } else {
                        // Modrinth 只提供了链接
                        ScreenshotItem(
                            element.asString,
                            null
                        )
                    }
                )
            }
            return screenshotItems
        }

        internal fun getResults(api: ApiHandler, lastResult: SearchResult, filters: Filters, type: String): SearchResult? {
            if (filters.category != Category.ALL && filters.category.modrinthName == null) {
                throw PlatformNotSupportedException("The platform does not support the ${filters.category} category!")
            }

            val response = api.get("search", getParams(lastResult, filters, type), JsonObject::class.java) ?: return null
            val responseHits = response.getAsJsonArray("hits") ?: return null

            val infoItems: MutableList<InfoItem> = ArrayList()
            for (responseHit in responseHits) {
                val hit = responseHit.asJsonObject
                getInfoItem(hit)?.let { item ->
                    infoItems.add(item)
                }
            }

            return returnResults(lastResult, infoItems, response, responseHits)
        }

        internal fun getParams(lastResult: SearchResult, filters: Filters, type: String): HashMap<String, Any> {
            val params = HashMap<String, Any>()
            val facetString = StringJoiner(",", "[", "]")
            facetString.add("[\"project_type:$type\"]")

            filters.mcVersion?.let { facetString.add("[\"versions:$it\"]") }
            getCategories(filters).let { if (it.isNotBlank()) facetString.add(it) }

            params["facets"] = facetString.toString()
            putDefaultParams(params, filters, lastResult.previousCount)

            return params
        }

        private fun getInfoItem(hit: JsonObject): InfoItem? {
            val categories = hit.get("categories").asJsonArray
            for (category in categories) {
                if (category.asString == "datapack") return null //没有数据包安装的需求，一律排除
            }
            return InfoItem(
                Platform.MODRINTH,
                hit.get("project_id").asString,
                arrayOf(hit.get("author").asString),
                hit.get("title").asString,
                hit.get("description").asString,
                hit.get("downloads").asLong,
                ZHTools.getDate(hit.get("date_created").asString),
                getIconUrl(hit),
                getScreenshots(hit),
                getAllCategories(hit).toList(),
            )
        }

        @Throws(Throwable::class)
        internal fun <T> getCommonVersions(
            api: ApiHandler,
            infoItem: InfoItem,
            force: Boolean,
            cache: InfoCache.CacheBase<MutableList<T>>,
            createItem: (JsonObject, JsonObject, MutableList<String>) -> T
        ): List<T>? {
            if (!force && cache.containsKey(api, infoItem.projectId))
                return cache.get(api, infoItem.projectId)

            val response: JsonArray = api.get(
                "project/${infoItem.projectId}/version",
                JsonArray::class.java
            ) ?: return null

            val items: MutableList<T> = ArrayList()
            //如果第一次获取依赖信息失败，则记录其id，之后不再尝试获取
            val invalidDependencies: MutableList<String> = ArrayList()
            for (element in response) {
                val versionObject = element.asJsonObject
                val filesJsonObject: JsonObject = versionObject.getAsJsonArray("files").get(0).asJsonObject

                items.add(createItem(versionObject, filesJsonObject, invalidDependencies))
            }

            cache.put(api, infoItem.projectId, items)
            return items
        }

        @Throws(Throwable::class)
        internal fun getVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<VersionItem>? {
            return getCommonVersions(
                api, infoItem, force, InfoCache.VersionCache
            ) { versionObject, filesJsonObject, _ ->
                VersionItem(
                    infoItem.projectId,
                    versionObject.get("name").asString,
                    versionObject.get("downloads").asLong,
                    ZHTools.getDate(versionObject.get("date_published").asString),
                    getMcVersions(versionObject.getAsJsonArray("game_versions")),
                    VersionTypeUtils.getVersionType(versionObject.get("version_type").asString),
                    filesJsonObject.get("filename").asString,
                    getSha1Hash(filesJsonObject),
                    filesJsonObject.get("url").asString
                )
            }
        }

        internal fun getMcVersions(gameVersionJson: JsonArray): List<String> {
            val mcVersions: MutableList<String> = java.util.ArrayList()
            for (gameVersion in gameVersionJson) {
                mcVersions.add(gameVersion.asString)
            }
            return mcVersions
        }

        internal fun getSha1Hash(filesJsonObject: JsonObject): String? {
            val hashesMap = filesJsonObject.getAsJsonObject("hashes")
            return if ((hashesMap != null && hashesMap.has("sha1"))) hashesMap["sha1"].asString else null
        }

        internal fun searchModFromID(api: ApiHandler, id: String): JsonObject? {
            val jsonObject = api.get("project/$id", JsonObject::class.java)
            return jsonObject
        }

        internal fun returnResults(
            lastResult: SearchResult,
            infoItems: List<InfoItem>,
            response: JsonObject,
            responseHits: JsonArray
        ): SearchResult = lastResult.apply {
            this.infoItems.addAll(infoItems)
            this.previousCount += responseHits.size()
            this.totalResultCount = response.get("total_hits").asInt
            this.isLastPage = responseHits.size() < MODRINTH_SEARCH_COUNT
        }
    }
}