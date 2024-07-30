package com.movtery.pojavzh.feature.mod

import android.content.Context
import net.kdt.pojavlaunch.R

object SearchModSort {
    private const val CURSEFORGE_SORT_INDEX_RELEVANT: Int = 1
    private const val CURSEFORGE_SORT_INDEX_DOWNLOADS: Int = 6
    private const val CURSEFORGE_SORT_INDEX_POPULARITY: Int = 2
    private const val CURSEFORGE_SORT_INDEX_RECENTLY_CREATED: Int = 11
    private const val CURSEFORGE_SORT_INDEX_RECENTLY_UPDATED: Int = 3
    private val indexList: MutableList<String> = ArrayList()

    @JvmStatic
    fun getIndexList(context: Context): List<String> {
        if (indexList.isEmpty()) {
            indexList.add(context.getString(R.string.zh_profile_mods_search_sort_by_relevant)) //相关 0
            indexList.add(context.getString(R.string.zh_profile_mods_search_sort_by_total_downloads)) //总下载 1
            indexList.add(context.getString(R.string.zh_profile_mods_search_sort_by_popularity)) //人气 2
            indexList.add(context.getString(R.string.zh_profile_mods_search_sort_by_recently_created)) //最近创建 3
            indexList.add(context.getString(R.string.zh_profile_mods_search_sort_by_recently_updated)) //最近更新 4
        }
        return indexList
    }

    @JvmStatic
    fun getModrinthIndexById(id: Int): String {
        return when (id) {
            1 -> "downloads"
            2 -> "follows"
            3 -> "newest"
            4 -> "updated"
            else -> "relevance"
        }
    }

    @JvmStatic
    fun getCurseforgeIndexById(id: Int): Int {
        return when (id) {
            1 -> CURSEFORGE_SORT_INDEX_DOWNLOADS
            2 -> CURSEFORGE_SORT_INDEX_POPULARITY
            3 -> CURSEFORGE_SORT_INDEX_RECENTLY_CREATED
            4 -> CURSEFORGE_SORT_INDEX_RECENTLY_UPDATED
            else -> CURSEFORGE_SORT_INDEX_RELEVANT
        }
    }
}
