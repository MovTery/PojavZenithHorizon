package com.movtery.pojavzh.feature.download.platform.curseforge

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.movtery.pojavzh.feature.download.Filters
import com.movtery.pojavzh.feature.download.InfoCache
import com.movtery.pojavzh.feature.download.enums.Category
import com.movtery.pojavzh.feature.download.enums.Platform
import com.movtery.pojavzh.feature.download.item.InfoItem
import com.movtery.pojavzh.feature.download.item.SearchResult
import com.movtery.pojavzh.feature.download.item.VersionItem
import com.movtery.pojavzh.feature.download.platform.PlatformNotSupportedException
import com.movtery.pojavzh.feature.download.utils.CategoryUtils
import com.movtery.pojavzh.feature.download.utils.VersionTypeUtils
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.feature.mod.ModMirror
import com.movtery.pojavzh.feature.mod.ModMirror.Companion.isInfoMirrored
import com.movtery.pojavzh.utils.MCVersionRegex.Companion.RELEASE_REGEX
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler
import net.kdt.pojavlaunch.utils.GsonJsonUtils
import java.io.IOException
import java.util.TreeSet
import java.util.function.Consumer

class CurseForgeCommonUtils {
    companion object {
        private const val ALGO_SHA_1 = 1
        private const val CURSEFORGE_MINECRAFT_GAME_ID = 432
        private const val CURSEFORGE_SEARCH_COUNT = 20
        private const val CURSEFORGE_PAGINATION_SIZE = 500
        internal const val CURSEFORGE_MODPACK_CLASS_ID = 4471
        internal const val CURSEFORGE_MOD_CLASS_ID = 6

        internal fun putDefaultParams(params: HashMap<String, Any>, filters: Filters, index: Int) {
            params["gameId"] = CURSEFORGE_MINECRAFT_GAME_ID
            params["searchFilter"] = filters.name
            params["sortField"] = filters.sort.curseforge
            params["sortOrder"] = "desc"
            params["pageSize"] = CURSEFORGE_SEARCH_COUNT
            filters.category.curseforgeID?.let { if (filters.category != Category.ALL) params["categoryId"] = it }
            filters.mcVersion?.let { if (it.isNotEmpty()) params["gameVersion"] = it }
            params["index"] = index
        }

        internal fun getAllCategories(hit: JsonObject): Set<Category> {
            val list: MutableSet<Category> = TreeSet()
            for (categories in hit["categories"].asJsonArray) {
                val id = categories.asJsonObject["id"].asString
                CategoryUtils.getCategoryByCurseForge(id)?.let { list.add(it) }
            }
            return list
        }

        internal fun getIconUrl(hit: JsonObject): String? {
            return runCatching {
                hit.getAsJsonObject("logo").get("thumbnailUrl").asString
            }.getOrNull()
        }

        internal fun getResults(api: ApiHandler, lastResult: SearchResult, filters: Filters, classId: Int): SearchResult? {
            if (filters.category != Category.ALL && filters.category.curseforgeID == null) {
                throw PlatformNotSupportedException("The platform does not support the ${filters.category} category!")
            }

            val params = HashMap<String, Any>()
            putDefaultParams(params, filters, lastResult.previousCount)
            params["classId"] = classId

            val response = api.get("mods/search", params, JsonObject::class.java) ?: return null
            val dataArray = response.getAsJsonArray("data") ?: return null

            val infoItems: MutableList<InfoItem> = ArrayList()
            for (data in dataArray) {
                val dataElement = data.asJsonObject
                getInfoItem(dataElement)?.let { item ->
                    infoItems.add(item)
                }
            }

            return returnResults(lastResult, infoItems, dataArray, response)
        }

        internal fun getInfoItem(dataObject: JsonObject): InfoItem? {
            val allowModDistribution = dataObject.get("allowModDistribution")
            // Gson automatically casts null to false, which leans to issues
            // So, only check the distribution flag if it is non-null
            if (!allowModDistribution.isJsonNull && !allowModDistribution.asBoolean) {
                Logging.i("CurseForgeCommonUtils", "Skipping project ${dataObject["name"].asString} because curseforge sucks")
                return null
            }

            return InfoItem(
                Platform.CURSEFORGE,
                dataObject.get("id").asString,
                getAuthors(dataObject.get("authors").asJsonArray).toTypedArray(),
                dataObject.get("name").asString,
                dataObject.get("summary").asString,
                dataObject.get("downloadCount").asLong,
                ZHTools.getDate(dataObject.get("dateCreated").asString),
                getIconUrl(dataObject),
                getAllCategories(dataObject).toList(),
            )
        }

        @Throws(Throwable::class)
        internal fun getVersions(api: ApiHandler, infoItem: InfoItem, force: Boolean): List<VersionItem>? {
            if (!force && InfoCache.VersionCache.containsKey(api, infoItem.projectId))
                return InfoCache.VersionCache.get(api, infoItem.projectId)

            val allData: List<JsonObject>
            try {
                allData = getPaginatedData(api, infoItem.projectId)
            } catch (e: IOException) {
                Logging.e("CurseForgeCommonHelper", Tools.printToString(e))
                return null
            }

            val versionsItem: MutableList<VersionItem> = ArrayList()
            for (data in allData) {
                //获取版本信息
                val mcVersions: MutableSet<String> = TreeSet()
                for (gameVersionElement in data.getAsJsonArray("gameVersions")) {
                    val gameVersion = gameVersionElement.asString
                    mcVersions.add(gameVersion)
                }
                //过滤非MC版本的元素
                val releaseRegex = RELEASE_REGEX
                val nonMCVersion: MutableSet<String> = TreeSet()
                mcVersions.forEach(Consumer { string: String ->
                    if (!releaseRegex.matcher(string).find()) nonMCVersion.add(string)
                })
                if (nonMCVersion.isNotEmpty()) mcVersions.removeAll(nonMCVersion)

                versionsItem.add(
                    VersionItem(
                        infoItem.projectId,
                        data.get("displayName").asString,
                        data.get("downloadCount").asLong,
                        ZHTools.getDate(data.get("fileDate").asString),
                        mcVersions.toList(),
                        VersionTypeUtils.getVersionType(data.get("releaseType").asString),
                        data.get("fileName").asString,
                        getSha1FromData(data),
                        ModMirror.replaceMirrorDownloadUrl(data.get("downloadUrl").asString)
                    )
                )
            }

            InfoCache.VersionCache.put(api, infoItem.projectId, versionsItem)
            return versionsItem
        }

        internal fun getAuthors(array: JsonArray): List<String> {
            val authors: MutableList<String> = ArrayList()
            for (authorElement in array) {
                val authorObject = authorElement.asJsonObject
                authors.add(authorObject.get("name").asString)
            }
            return authors
        }

        internal fun getSha1FromData(jsonObject: JsonObject): String? {
            val hashes = GsonJsonUtils.getJsonArraySafe(jsonObject, "hashes") ?: return null
            for (jsonElement in hashes) {
                // The sha1 = 1; md5 = 2;
                val jsonObject1 = GsonJsonUtils.getJsonObjectSafe(jsonElement)
                if (GsonJsonUtils.getIntSafe(jsonObject1, "algo", -1) == ALGO_SHA_1) {
                    return GsonJsonUtils.getStringSafe(jsonObject1, "value")
                }
            }
            return null
        }

        internal fun getDownloadUrl(api: ApiHandler, projectID: Long, fileID: Long): String? {
            // First try the official api endpoint
            val response = api.get("mods/$projectID/files/$fileID/download-url", JsonObject::class.java)
            if (response != null && !response["data"].isJsonNull) return response["data"].asString

            // Otherwise, fallback to building an edge link
            val fallbackResponse = api.get("mods/$projectID/files/$fileID", JsonObject::class.java)
            if (fallbackResponse != null && !fallbackResponse["data"].isJsonNull) {
                val modData = fallbackResponse["data"].asJsonObject
                val id = modData["id"].asInt
                return "https://edge.forgecdn.net/files/${id / 1000}/${id % 1000}/${modData["fileName"].asString}"
            }

            return null
        }

        internal fun getDownloadSha1(api: ApiHandler, projectID: Long, fileID: Long): String? {
            // Try the api endpoint, die in the other case
            val response: JsonObject = api.get("mods/$projectID/files/$fileID", JsonObject::class.java)
            val data = GsonJsonUtils.getJsonObjectSafe(response, "data") ?: return null
            return getSha1FromData(data)
        }

        internal fun searchModFromID(api: ApiHandler, id: String): JsonObject {
            val response: JsonObject = api.get("mods/$id", JsonObject::class.java)
            return response
        }

        @Throws(IOException::class)
        internal fun getPaginatedData(api: ApiHandler, projectId: String): List<JsonObject> {
            val dataList: MutableList<JsonObject> = ArrayList()
            var index = 0
            var isMirrored = false
            while (index != -1 && !isMirrored) {
                val params = HashMap<String, Any>()
                params["index"] = index
                params["pageSize"] = CURSEFORGE_PAGINATION_SIZE

                val response: JsonObject = api.get("mods/$projectId/files", params, JsonObject::class.java)
                val data = GsonJsonUtils.getJsonArraySafe(response, "data")
                    ?: throw IOException("Invalid data!")

                for (i in 0 until data.size()) {
                    val fileInfo = data[i].asJsonObject
                    if (fileInfo["isServerPack"].asBoolean) continue
                    dataList.add(fileInfo)
                }
                if (data.size() < CURSEFORGE_PAGINATION_SIZE) {
                    index = -1
                    continue
                }
                index += CURSEFORGE_PAGINATION_SIZE
                isMirrored = isInfoMirrored()
            }

            return dataList
        }

        internal fun returnResults(
            lastResult: SearchResult,
            infoItems: List<InfoItem>,
            dataArray: JsonArray,
            response: JsonObject
        ): SearchResult = lastResult.apply {
            this.infoItems.addAll(infoItems)
            this.previousCount += dataArray.size()
            this.totalResultCount = response.getAsJsonObject("pagination").get("totalCount").asInt
            this.isLastPage = dataArray.size() < CURSEFORGE_SEARCH_COUNT
        }
    }
}