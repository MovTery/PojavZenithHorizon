package com.movtery.pojavzh.utils

import android.content.Context
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.gameHome
import com.movtery.pojavzh.utils.file.FileTools.mkdirs
import net.kdt.pojavlaunch.Tools
import java.io.File

object PathAndUrlManager {
    const val URL_GITHUB_RELEASE: String = "https://api.github.com/repos/MovTery/PojavZenithHorizon/releases/latest"
    const val URL_GITHUB_HOME: String = "https://api.github.com/repos/MovTery/PZH-InfoRetrieval/contents/"
    const val URL_GITHUB_POJAVLAUNCHER: String = "https://github.com/PojavLauncherTeam/PojavLauncher"
    const val URL_MINECRAFT: String = "https://www.minecraft.net/"
    const val URL_SUPPORT: String = "https://afdian.com/a/MovTery"

    @JvmField var DIR_GAME_DEFAULT: String? = null
    @JvmField var DIR_CUSTOM_MOUSE: String? = null
    @JvmField var DIR_LOGIN: String? = null
    @JvmField var DIR_BACKGROUND: File? = null
    @JvmField var DIR_APP_CACHE: File? = null
    @JvmField var DIR_USER_ICON: File? = null

    @JvmField var FILE_PROFILE_PATH: File? = null

    @JvmStatic
    fun initContextConstants(context: Context) {
        initDirectoryPath(context)
        initFilePath()

        createDefaultPath(DIR_BACKGROUND)
    }

    private fun initDirectoryPath(context: Context) {
        DIR_GAME_DEFAULT = "$gameHome/instance/default"
        DIR_CUSTOM_MOUSE = Tools.DIR_GAME_HOME + "/mouse"
        DIR_LOGIN = Tools.DIR_GAME_HOME + "/login"
        DIR_BACKGROUND = File(Tools.DIR_GAME_HOME + "/background")
        DIR_APP_CACHE = context.externalCacheDir
        DIR_USER_ICON = File(Tools.DIR_CACHE, "/user_icon")
    }

    private fun initFilePath() {
        FILE_PROFILE_PATH = File(Tools.DIR_DATA, "/profile_path.json")
    }

    private fun createDefaultPath(path: File?) {
        if (!path!!.exists()) {
            mkdirs(path)
        }
    }
}