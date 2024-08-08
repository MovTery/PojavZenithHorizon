package com.movtery.pojavzh.feature

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.Companion.versionsHome
import com.movtery.pojavzh.utils.MCVersionRegex
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.MCOptionUtils
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile

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

        @Throws(NumberFormatException::class)
        private fun getVersion(versionId: String): Int {
            val versionParts = versionId.split('.')
            return when {
                versionParts.size > 1 -> versionParts[1].toInt()
                else -> 12
            }
        }

        private fun getLanguage(versionName: String?, rawLang: String?): String? {
            if (versionName == null || rawLang == null) return null
            val lang = if (rawLang == "system") ZHTools.getSystemLanguage() else rawLang

            val version = runCatching {
                Tools.GLOBAL_GSON.fromJson(
                    Tools.read("$versionsHome/$versionName/$versionName.json"),
                    JMinecraftVersionList.Version::class.java
                )
            }.getOrElse { e ->
                throw RuntimeException(e)
            }

            val versionId = version.id

            return when {
                StringUtils.containsDot(versionId) -> { // 1.10 -
                    runCatching {
                        val ver = getVersion(versionId)
                        if (ver < 11) getOlderLanguage(lang) else lang
                    }.getOrDefault(lang)
                }
                MCVersionRegex.SNAPSHOT_REGEX.matcher(versionId).matches() -> { // 快照版本 "24w09a" "16w20a"
                    runCatching {
                        val result = StringUtils.extractNumbers(versionId, 2)
                        when {
                            result[0] < 16 -> getOlderLanguage(lang)
                            result[0] == 16 && result[1] <= 32 -> getOlderLanguage(lang)
                            else -> lang
                        }
                    }.getOrDefault(lang)
                }
                else -> lang
            }
        }

        @JvmStatic
        fun setGameLanguage(minecraftProfile: MinecraftProfile, overridden: Boolean) {
            if (MCOptionUtils.containsKey("lang") && !overridden) return
            val language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_GAME_LANGUAGE)
            MCOptionUtils.set("lang", language)
        }
    }
}
