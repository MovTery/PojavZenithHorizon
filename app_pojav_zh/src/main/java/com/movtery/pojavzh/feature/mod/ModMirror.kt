package com.movtery.pojavzh.feature.mod

import com.movtery.pojavzh.setting.AllSettings

class ModMirror {
    companion object {
        private const val MCIM_MIRROR: String = "mod.mcimirror.top"
        private val MODRINTH = arrayListOf("staging-api.modrinth.com", "api.modrinth.com", "cdn.modrinth.com")
        private val CURSEFORGE = arrayListOf("api.curseforge.com", "edge.forgecdn.net", "mediafilez.forgecdn.net", "media.forgecdn.net")

        @JvmStatic
        fun replaceMirrorInfoUrl(baseUrl: String): String {
            return when (AllSettings.modInfoSource) {
                Source.ORIGINAL.name.lowercase() -> baseUrl
                Source.MCIM.name.lowercase() -> replaceMCIM(baseUrl)
                else -> baseUrl
            }
        }

        @JvmStatic
        fun replaceMirrorDownloadUrl(baseUrl: String): String {
            return when(AllSettings.modDownloadSource) {
                Source.ORIGINAL.name.lowercase() -> baseUrl
                Source.MCIM.name.lowercase() -> replaceMCIM(baseUrl)
                else -> baseUrl
            }
        }

        @JvmStatic
        fun isInfoMirrored(): Boolean {
            return when (AllSettings.modInfoSource) {
                Source.ORIGINAL.name.lowercase() -> false
                else -> true
            }
        }

        @JvmStatic
        fun isDownloadUrlMirrored(): Boolean {
            return when (AllSettings.modDownloadSource) {
                Source.ORIGINAL.name.lowercase() -> false
                else -> true
            }
        }

        private fun replaceMCIM(baseUrl: String): String {
            val urls = mapOf(
                MODRINTH[0] to { url: String -> url.replace(MODRINTH[0], "$MCIM_MIRROR/modrinth") },
                MODRINTH[1] to { url: String -> url.replace(MODRINTH[1], "$MCIM_MIRROR/modrinth") },
                MODRINTH[2] to { url: String -> url.replace(MODRINTH[2], MCIM_MIRROR) },
                CURSEFORGE[0] to { url: String -> url.replace(CURSEFORGE[0], "$MCIM_MIRROR/curseforge") },
                CURSEFORGE[1] to { url: String -> url.replace(CURSEFORGE[1], MCIM_MIRROR) },
                CURSEFORGE[2] to { url: String -> url.replace(CURSEFORGE[2], MCIM_MIRROR) },
                CURSEFORGE[3] to { url: String -> url.replace(CURSEFORGE[3], MCIM_MIRROR) }
            )

            return urls.entries.firstOrNull { baseUrl.contains(it.key) }?.value?.invoke(baseUrl)?: baseUrl
        }
    }

    enum class Source {
        ORIGINAL, MCIM
    }
}