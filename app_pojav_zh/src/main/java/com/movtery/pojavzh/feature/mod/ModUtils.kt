package com.movtery.pojavzh.feature.mod

import com.movtery.pojavzh.utils.file.FileTools.Companion.renameFile
import java.io.File

class ModUtils {
    companion object {
        const val JAR_FILE_SUFFIX: String = ".jar"
        const val DISABLE_JAR_FILE_SUFFIX: String = "$JAR_FILE_SUFFIX.disabled"

        @JvmStatic
        fun disableMod(file: File?) {
            val fileName = file!!.name
            val fileParent = file.parent
            val newFile = File(fileParent, "$fileName.disabled")
            renameFile(file, newFile)
        }

        @JvmStatic
        fun enableMod(file: File?) {
            val fileName = file!!.name
            val fileParent = file.parent
            var newFileName = fileName.substring(0, fileName.lastIndexOf(DISABLE_JAR_FILE_SUFFIX))
            if (!fileName.endsWith(JAR_FILE_SUFFIX)) newFileName += JAR_FILE_SUFFIX //如果没有.jar结尾，那么默认加上.jar后缀

            val newFile = File(fileParent, newFileName)
            renameFile(file, newFile)
        }

        @JvmStatic
        fun replaceDownloadUrl(baseUrl: String): String {
            val urls = mapOf(
                //Modrinth
                "staging-api.modrinth.com" to { url: String -> url.replace("staging-api.modrinth.com", "mod.mcimirror.top/modrinth") },
                "api.modrinth.com" to { url: String -> url.replace("api.modrinth.com", "mod.mcimirror.top/modrinth") },
                "cdn.modrinth.com" to { url: String -> url.replace("cdn.modrinth.com", "mod.mcimirror.top") },
                //CurseForge
                "api.curseforge.com" to { url: String -> url.replace("api.curseforge.com", "mod.mcimirror.top/curseforge") },
                "edge.forgecdn.net" to { url: String -> url.replace("edge.forgecdn.net", "mod.mcimirror.top") },
                "mediafilez.forgecdn.net" to { url: String -> url.replace("mediafilez.forgecdn.net", "mod.mcimirror.top") }
            )

            return urls.entries.firstOrNull { baseUrl.contains(it.key) }?.value?.invoke(baseUrl)?: baseUrl
        }
    }
}