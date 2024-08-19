package com.movtery.pojavzh.feature.mod

import com.movtery.pojavzh.utils.file.FileTools.Companion.renameFile
import java.io.File

class ModUtils {
    companion object {
        const val JAR_FILE_SUFFIX: String = ".jar"
        const val DISABLE_JAR_FILE_SUFFIX: String = "$JAR_FILE_SUFFIX.disabled"
        private const val MCIM_MIRROR: String = "mod.mcimirror.top"
        private val MODRINTH = arrayListOf("staging-api.modrinth.com", "api.modrinth.com", "cdn.modrinth.com")
        private val CURSEFORGE = arrayListOf("api.curseforge.com", "edge.forgecdn.net", "mediafilez.forgecdn.net", "media.forgecdn.net")

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
        fun replaceMirrorUrl(baseUrl: String): String {
            val urls = mapOf(
                //Modrinth
                MODRINTH[0] to { url: String -> url.replace(MODRINTH[0], "$MCIM_MIRROR/modrinth") },
                MODRINTH[1] to { url: String -> url.replace(MODRINTH[1], "$MCIM_MIRROR/modrinth") },
                MODRINTH[2] to { url: String -> url.replace(MODRINTH[2], MCIM_MIRROR) },
                //CurseForge
                CURSEFORGE[0] to { url: String -> url.replace(CURSEFORGE[0], "$MCIM_MIRROR/curseforge") },
                CURSEFORGE[1] to { url: String -> url.replace(CURSEFORGE[1], MCIM_MIRROR) },
                CURSEFORGE[2] to { url: String -> url.replace(CURSEFORGE[2], MCIM_MIRROR) },
                CURSEFORGE[3] to { url: String -> url.replace(CURSEFORGE[3], MCIM_MIRROR) }
            )

            return urls.entries.firstOrNull { baseUrl.contains(it.key) }?.value?.invoke(baseUrl)?: baseUrl
        }
    }
}