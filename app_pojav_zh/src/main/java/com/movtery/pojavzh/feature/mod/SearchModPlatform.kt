package com.movtery.pojavzh.feature.mod

import android.content.Context
import net.kdt.pojavlaunch.R

class SearchModPlatform {
    companion object {
        private val indexList: MutableList<String> = ArrayList()

        @JvmStatic
        fun getIndexList(context: Context): List<String> {
            if (indexList.isEmpty()) {
                indexList.add(context.getString(R.string.profile_mods_search_platform_both))
                indexList.add("Modrinth")
                indexList.add("CurseForge")
            }
            return indexList
        }

        @JvmStatic
        fun getPlatform(index: Int): ModFilters.ApiPlatform {
            return when (index) {
                1 -> ModFilters.ApiPlatform.MODRINTH
                2 -> ModFilters.ApiPlatform.CURSEFORGE
                else -> ModFilters.ApiPlatform.BOTH
            }
        }
    }
}
