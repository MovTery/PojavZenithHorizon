package com.movtery.pojavzh.feature.mod

import android.content.Context
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters.ApiPlatform

object SearchModPlatform {
    private val indexList: MutableList<String> = ArrayList()

    @JvmStatic
    fun getIndexList(context: Context): List<String> {
        if (indexList.isEmpty()) {
            indexList.add(context.getString(R.string.zh_profile_mods_search_platform_both))
            indexList.add("Modrinth")
            indexList.add("CurseForge")
        }
        return indexList
    }

    @JvmStatic
    fun getIndex(platform: ApiPlatform?): Int {
        return when (platform) {
            ApiPlatform.MODRINTH -> 1
            ApiPlatform.CURSEFORGE -> 2
            else -> 0
        }
    }

    @JvmStatic
    fun getPlatform(index: Int): ApiPlatform {
        return when (index) {
            1 -> ApiPlatform.MODRINTH
            2 -> ApiPlatform.CURSEFORGE
            else -> ApiPlatform.BOTH
        }
    }
}
