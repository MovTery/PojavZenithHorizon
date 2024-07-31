package com.movtery.pojavzh.feature.customprofilepath

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.gameHome
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfileItem
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File
import java.io.FileWriter
import java.io.IOException

object ProfilePathManager {
    private val defaultPath: String = Tools.DIR_GAME_HOME

    @JvmStatic
    fun setCurrentPathId(id: String?) {
        LauncherPreferences.DEFAULT_PREF.edit().putString("launcherProfile", id).apply()
    }

    @JvmStatic
    val currentPath: String
        get() {
            //通过选中的id来获取当前路径
            val id = LauncherPreferences.DEFAULT_PREF.getString("launcherProfile", "default")
            if (id == "default") {
                return defaultPath
            }

            if (ZHTools.FILE_PROFILE_PATH.exists()) {
                try {
                    val read = Tools.read(ZHTools.FILE_PROFILE_PATH)
                    val jsonObject = JsonParser.parseString(read).asJsonObject
                    if (jsonObject.has(id)) {
                        val profilePathJsonObject =
                            Gson().fromJson(jsonObject[id], ProfilePathJsonObject::class.java)
                        return profilePathJsonObject.path
                    }
                } catch (e: IOException) {
                    Log.e("Read Profile", e.toString())
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
            FileWriter(ZHTools.FILE_PROFILE_PATH).use { fileWriter ->
                Gson().toJson(jsonObject, fileWriter)
            }
        } catch (e: IOException) {
            Log.e("Write Profile", e.toString())
        }
    }
}
