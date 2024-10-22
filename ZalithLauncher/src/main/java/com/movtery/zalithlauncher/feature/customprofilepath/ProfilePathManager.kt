package com.movtery.zalithlauncher.feature.customprofilepath

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.feature.customprofilepath.ProfilePathHome.Companion.gameHome
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.Settings
import com.movtery.zalithlauncher.ui.subassembly.customprofilepath.ProfileItem
import com.movtery.zalithlauncher.utils.PathAndUrlManager
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ProfilePathManager {
    companion object {
        private val defaultPath: String = PathAndUrlManager.DIR_GAME_HOME

        @JvmStatic
        fun setCurrentPathId(id: String?) {
            Settings.Manager.put("launcherProfile", id).save()
        }

        @JvmStatic
        val currentPath: String
            get() {
                //通过选中的id来获取当前路径
                val id = AllSettings.launcherProfile
                if (id == "default") {
                    return defaultPath
                }

                PathAndUrlManager.FILE_PROFILE_PATH.apply {
                    if (exists()) {
                        runCatching {
                            val read = Tools.read(this)
                            val jsonObject = JsonParser.parseString(read).asJsonObject
                            if (jsonObject.has(id)) {
                                val profilePathJsonObject =
                                    Gson().fromJson(jsonObject[id], ProfilePathJsonObject::class.java)
                                return profilePathJsonObject.path
                            }
                        }.getOrElse { e -> Logging.e("Read Profile", e.toString()) }
                    }
                }

                return defaultPath
            }

        @JvmStatic
        val currentProfile: File
            get() {
                val file = File(gameHome, "launcher_profiles.json")
                if (!file.exists()) {
                    try {
                        Tools.copyAssetFile(PojavApplication.getContext(), "launcher_profiles.json", gameHome, false)
                    } catch (e: IOException) {
                        return File(defaultPath, "launcher_profiles.json")
                    }
                }
                return file
            }

        @JvmStatic
        fun save(items: List<ProfileItem>) {
            val jsonObject = JsonObject()

            for (item in items) {
                if (item.id == "default") continue

                val profilePathJsonObject = ProfilePathJsonObject(item.title, item.path)
                jsonObject.add(item.id, Gson().toJsonTree(profilePathJsonObject))
            }

            try {
                FileWriter(PathAndUrlManager.FILE_PROFILE_PATH).use { fileWriter ->
                    Gson().toJson(jsonObject, fileWriter)
                }
            } catch (e: IOException) {
                Logging.e("Write Profile", e.toString())
            }
        }
    }
}
