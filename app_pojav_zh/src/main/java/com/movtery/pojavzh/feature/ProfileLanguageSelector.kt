package com.movtery.pojavzh.feature

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.versionsHome
import com.movtery.pojavzh.utils.MCVersionRegex
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.MCOptionUtils
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import java.io.IOException

object ProfileLanguageSelector {
    private fun getOlderLanguage(lang: String): String {
        val builder = StringBuilder(lang)
        val underscoreIndex = lang.indexOf('_')

        if (underscoreIndex != -1) {
            for (i in underscoreIndex until lang.length) {
                builder.setCharAt(i, lang[i].uppercaseChar())
            } //只将下划线后面的字符转换为大写
        }

        return builder.toString()
    }

    @Throws(NumberFormatException::class)
    private fun getVersion(versionId: String): Int {
        val firstDotIndex = versionId.indexOf('.')
        val secondDotIndex = versionId.indexOf('.', firstDotIndex + 1)

        val version = if (firstDotIndex != -1) { // 官方版本
            if (secondDotIndex == -1) versionId.substring(firstDotIndex + 1).toInt()
            else versionId.substring(firstDotIndex + 1, secondDotIndex).toInt()
        } else 12
        return version
    }

    private fun getLanguage(versionName: String?, rawLang: String?): String? {
        if (versionName == null || rawLang == null) return null
        var lang: String = rawLang
        if (rawLang == "system") lang = ZHTools.getSystemLanguage()

        val version: JMinecraftVersionList.Version
        try {
            version = Tools.GLOBAL_GSON.fromJson(
                Tools.read("$versionsHome/$versionName/$versionName.json"),
                JMinecraftVersionList.Version::class.java
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val versionId = version.id

        val pattern = MCVersionRegex.SNAPSHOT_REGEX
        val matcher = pattern.matcher(versionId)

        if (StringUtils.containsDot(versionId)) {
            try {
                val ver = getVersion(versionId)

                // 1.10 -
                if (ver < 11) {
                    return getOlderLanguage(lang)
                }

                return lang // ? & 1.0
            } catch (e: NumberFormatException) {
                return lang
            }
        } else if (matcher.matches()) { // 快照版本 "24w09a" "16w20a"
            try {
                val result = StringUtils.extractNumbers(versionId, 2)

                if (result[0] < 16) {
                    return getOlderLanguage(lang)
                } else if ((result[0] == 16) and (result[1] <= 32)) {
                    return getOlderLanguage(lang)
                }

                return lang
            } catch (e: NumberFormatException) {
                return lang
            }
        }

        return lang
    }

    @JvmStatic
    fun setGameLanguage(minecraftProfile: MinecraftProfile, overridden: Boolean) {
        if (MCOptionUtils.containsKey("lang")) {
            if (!overridden) return
        }
        val language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_GAME_LANGUAGE)
        MCOptionUtils.set("lang", language)
    }
}
