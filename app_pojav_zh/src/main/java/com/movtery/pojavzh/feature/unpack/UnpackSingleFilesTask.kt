package com.movtery.pojavzh.feature.unpack

import android.content.Context
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.Companion.gameHome
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.utils.CopyDefaultFromAssets.Companion.copyFromAssets
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.Tools

class UnpackSingleFilesTask(val context: Context) : AbstractUnpackTask() {
    override fun isNeedUnpack(): Boolean = true

    override fun run() {
        runCatching {
            copyFromAssets(context)
            Tools.copyAssetFile(context, "launcher_profiles.json", gameHome, false)
            Tools.copyAssetFile(context, "resolv.conf", PathAndUrlManager.DIR_DATA, false)
        }.getOrElse { e("AsyncAssetManager", "Failed to unpack critical components !") }
    }
}