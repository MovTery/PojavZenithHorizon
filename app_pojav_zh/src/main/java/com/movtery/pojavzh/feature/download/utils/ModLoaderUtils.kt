package com.movtery.pojavzh.feature.download.utils

import com.movtery.pojavzh.feature.download.enums.ModLoader

class ModLoaderUtils {
    companion object {
        fun getModLoaderList(): List<ModLoader> {
            val list: MutableList<ModLoader> = ArrayList()
            ModLoader.entries.forEach { modLoader ->
                list.add(modLoader)
            }
            return list
        }

        private fun checkForModLoader(func: (modloader: ModLoader) -> Boolean): ModLoader? {
            ModLoader.entries.forEach { modLoader ->
                if (func.invoke(modLoader)) return modLoader
            }
            return null
        }

        fun getModLoaderByModrinth(modLoaderName: String): ModLoader? {
            return checkForModLoader { modLoader -> modLoader.modrinthName == modLoaderName }
        }

        fun getModLoaderByCurseForge(modLoaderId: String): ModLoader? {
            return checkForModLoader { modloader -> modloader.curseforgeId == modLoaderId }
        }

        fun getModLoader(type: Int): ModLoader {
            return when (type) {
                0 -> ModLoader.FORGE
                1 -> ModLoader.NEOFORGE
                2 -> ModLoader.FABRIC
                3 -> ModLoader.QUILT
                else -> ModLoader.ALL
            }
        }

        fun getModLoader(name: String): ModLoader? {
            return if (name.equals("fabric", true)) ModLoader.FABRIC
            else if (name.equals("forge", true)) ModLoader.FORGE
            else if (name.equals("quilt", true)) ModLoader.QUILT
            else if (name.equals("neoforge", true)) ModLoader.NEOFORGE
            else null
        }

        fun addModLoaderToList(list: MutableCollection<ModLoader>, name: String) {
            getModLoader(name)?.let { list.add(it) }
        }
    }
}