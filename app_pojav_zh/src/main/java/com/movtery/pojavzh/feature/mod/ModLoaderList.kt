package com.movtery.pojavzh.feature.mod

import java.util.Collections
import java.util.Locale

class ModLoaderList {
    companion object {
        // 不可变的 modloaderList
        @JvmField
        val modloaderList: MutableList<String> = Collections.unmodifiableList(
            listOf(
                ModLoader.FORGE.loaderName,
                ModLoader.FABRIC.loaderName,
                ModLoader.QUILT.loaderName,
                ModLoader.NEO_FORGE.loaderName
            )
        )

        private var modloaderNameMap: Map<String, String>? = null
        private var modloaderIdMap: Map<Int, String?>? = null

        init {
            val tempNameMap: MutableMap<String, String> = HashMap()
            val tempIdMap: MutableMap<Int, String?> = HashMap()
            for (modLoader in ModLoader.entries) {
                tempNameMap[modLoader.loaderName.lowercase(Locale.getDefault())] = modLoader.loaderName
                tempIdMap[modLoader.id] = modLoader.loaderName
            }
            modloaderNameMap = Collections.unmodifiableMap(tempNameMap)
            modloaderIdMap = Collections.unmodifiableMap(tempIdMap)
        }

        @JvmStatic
        fun getModloaderName(modloader: String): String {
            return modloaderNameMap!!.getOrDefault(modloader.lowercase(Locale.getDefault()), "none")
        }

        @JvmStatic
        fun getModloaderNameByCurseId(id: Int): String? {
            return modloaderIdMap!!.getOrDefault(id, null)
        }

        @JvmStatic
        fun notModloaderName(modloader: String?): Boolean {
            return modloader.isNullOrEmpty() || !modloaderNameMap!!.containsKey(
                modloader.lowercase(Locale.getDefault())
            )
        }
    }

    private enum class ModLoader(val id: Int, val loaderName: String) {
        FORGE(1, "Forge"),
        FABRIC(4, "Fabric"),
        QUILT(5, "Quilt"),
        NEO_FORGE(6, "NeoForge")
    }
}
