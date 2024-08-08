package com.movtery.pojavzh.utils

import android.content.Context
import net.kdt.pojavlaunch.Tools
import java.io.File
import java.io.IOException

class CopyDefaultFromAssets {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun copyFromAssets(context: Context?) {
            //默认控制布局
            if (checkDirectoryEmpty(PathAndUrlManager.DIR_CTRLMAP_PATH)) {
                Tools.copyAssetFile(context, "default.json", PathAndUrlManager.DIR_CTRLMAP_PATH, false)
            }
        }

        private fun checkDirectoryEmpty(dir: String?): Boolean {
            val controlDir = dir?.let { File(it) }
            val files = controlDir?.listFiles()
            return files?.isEmpty() ?: true
        }
    }
}
