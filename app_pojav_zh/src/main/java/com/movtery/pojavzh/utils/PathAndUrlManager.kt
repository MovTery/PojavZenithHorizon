package com.movtery.pojavzh.utils

import android.content.Context
import android.os.Environment
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.Companion.gameHome
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import net.kdt.pojavlaunch.Tools
import java.io.File

object PathAndUrlManager {
    const val URL_GITHUB_RELEASE: String = "https://api.github.com/repos/MovTery/PojavZenithHorizon/releases/latest"
    const val URL_GITHUB_HOME: String = "https://api.github.com/repos/MovTery/PZH-InfoRetrieval/contents/"
    const val URL_GITHUB_POJAVLAUNCHER: String = "https://github.com/PojavLauncherTeam/PojavLauncher"
    const val URL_MINECRAFT: String = "https://www.minecraft.net/"
    const val URL_SUPPORT: String = "https://afdian.com/a/MovTery"
    const val URL_HOME: String = "https://github.com/MovTery/PojavZenithHorizon"

    //PojavLauncher
    @JvmField var DIR_NATIVE_LIB: String? = null
    @JvmField var DIR_DATA: String? = null //Initialized later to get context
    @JvmField var DIR_CACHE: File? = null
    @JvmField var DIR_MULTIRT_HOME: String? = null
    @JvmField var DIR_GAME_HOME: String = Environment.getExternalStorageDirectory().absolutePath + "/games/PojavZenithHorizon"
    @JvmField var DIR_CTRLMAP_PATH: String? = null

    @JvmField var DIR_GAME_DEFAULT: String? = null
    @JvmField var DIR_CUSTOM_MOUSE: String? = null
    @JvmField var DIR_LOGIN: String? = null
    @JvmField var DIR_BACKGROUND: File? = null
    @JvmField var DIR_APP_CACHE: File? = null
    @JvmField var DIR_USER_ICON: File? = null

    @JvmField var FILE_PROFILE_PATH: File? = null
    @JvmField var FILE_CTRLDEF_FILE: String? = null

    @JvmStatic
    fun initContextConstants(context: Context) {
        DIR_NATIVE_LIB = context.applicationInfo.nativeLibraryDir
        DIR_DATA = context.filesDir.getParent()
        DIR_CACHE = context.cacheDir
        DIR_MULTIRT_HOME = "$DIR_DATA/runtimes"
        DIR_GAME_HOME = Tools.getPojavStorageRoot(context).absolutePath
        DIR_CTRLMAP_PATH = "$DIR_GAME_HOME/controlmap"

        FILE_PROFILE_PATH = File(DIR_DATA, "/profile_path.json")
        FILE_CTRLDEF_FILE = "$DIR_GAME_HOME/controlmap/default.json"

        DIR_GAME_DEFAULT = "$gameHome/instance/default"
        DIR_CUSTOM_MOUSE = "$DIR_GAME_HOME/mouse"
        DIR_LOGIN = "$DIR_GAME_HOME/login"
        DIR_BACKGROUND = File("$DIR_GAME_HOME/background")
        DIR_APP_CACHE = context.externalCacheDir
        DIR_USER_ICON = File(DIR_CACHE, "/user_icon")

        createDefaultPath(DIR_BACKGROUND)
    }

    private fun createDefaultPath(path: File?) {
        if (!path!!.exists()) {
            mkdirs(path)
        }
    }
}