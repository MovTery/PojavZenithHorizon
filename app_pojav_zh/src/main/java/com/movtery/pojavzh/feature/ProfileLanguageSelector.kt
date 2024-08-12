package com.movtery.pojavzh.feature

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.Companion.versionsHome
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils
import com.movtery.pojavzh.utils.MCVersionRegex
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.MCOptionUtils
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import org.jackhuang.hmcl.util.versioning.VersionNumber
import org.jackhuang.hmcl.util.versioning.VersionRange


class ProfileLanguageSelector {
    companion object {
        private fun getOlderLanguage(lang: String): String {
            val underscoreIndex = lang.indexOf('_')
            return if (underscoreIndex != -1) {
                //只将下划线后面的字符转换为大写
                val builder = StringBuilder(lang.substring(0, underscoreIndex + 1))
                builder.append(lang.substring(underscoreIndex + 1).uppercase())
                builder.toString()
            } else lang
        }

        private fun getLanguage(versionName: String, rawLang: String): String {
            val lang = if (rawLang == "system") ZHTools.getSystemLanguage() else rawLang

            val version: String = runCatching {
                val versionObject = Tools.GLOBAL_GSON.fromJson(
                    Tools.read("$versionsHome/$versionName/$versionName.json"),
                    JMinecraftVersionList.Version::class.java
                )
                versionObject.id
            }.getOrElse { e ->
                Logging.e("ProfileLanguageSelector", "Failed to read version data : \n ${Tools.printToString(e)}")
                "1.11"
            }

            val versionId = VersionNumber.asVersion(version).canonical
            Logging.i("ProfileLanguageSelector", "Version Id : $versionId")

            return when {
                versionId.contains('-') -> otherVersion(versionId, lang) // Mod加载器或者其他
                versionId.contains('.') -> {
                    if (isOlderVersionRelease(versionId)) getOlderLanguage(lang) // 1.10 -
                    else lang
                }
                MCVersionRegex.SNAPSHOT_REGEX.matcher(versionId).matches() -> { // 快照版本 "24w09a" "16w20a"
                    if (isOlderVersionSnapshot(versionId)) getOlderLanguage(lang)
                    else lang
                }
                else -> lang
            }
        }

        private fun otherVersion(versionId: String, lang: String): String {
            // 定义关键词，以及相应的版本名称处理逻辑
            val suffixes = mapOf(
                // "1.20.4-OptiFine_HD_U_I7_pre3"   -> "1.20.4"
                "-OptiFine" to { id: String -> id.substringBefore('-') },
                // "1.20.2-forge-48.1.0"            -> "1.20.2"
                "-forge-" to { id: String -> id.substringBefore('-') },
                // "neoforge-21.1.8"                -> "1.21.1"
                "neoforge-" to { id: String -> NeoForgeUtils.formatGameVersion(id.removePrefix("neoforge-")) },
                // "fabric-loader-0.15.7-1.20.4"    -> "1.20.4"
                "fabric-loader-" to { id: String -> id.substringAfterLast('-') },
                // "quilt-loader-0.23.1-1.20.4"     -> "1.20.4"
                "quilt-loader-" to { id: String -> id.substringAfterLast('-') }
            )

            val versionName = suffixes.entries
                .firstOrNull { versionId.contains(it.key, true) }
                ?.value?.invoke(versionId)
                ?: versionId //其他情况，命名规则千变万化，故不做检测

            Logging.i("ProfileLanguageSelector", "Version Name: $versionName")

            return if (isOlderVersionRelease(versionName)) getOlderLanguage(lang) else lang
        }

        private fun isOlderVersionRelease(versionName: String): Boolean {
            return VersionRange.atMost(VersionNumber.asVersion("1.10.2")).contains(VersionNumber.asVersion(versionName))
        }

        private fun isOlderVersionSnapshot(versionName: String): Boolean {
            return VersionRange.atMost(VersionNumber.asVersion("16w32a")).contains(VersionNumber.asVersion(versionName))
        }

        @JvmStatic
        fun setGameLanguage(minecraftProfile: MinecraftProfile, overridden: Boolean) {
            if (MCOptionUtils.containsKey("lang") && !overridden) return
            val language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_GAME_LANGUAGE)
            Logging.i("ProfileLanguageSelector", "The game language has been set to: $language")
            MCOptionUtils.set("lang", language)
        }
    }
}
