package com.movtery.pojavzh.utils

import android.content.Context
import android.widget.Toast
import com.movtery.pojavzh.utils.file.FileTools
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import org.apache.commons.io.FileUtils
import java.io.File

class CleanUpCache {
    companion object {
        private var isCleaning = false

        @JvmStatic
        fun start(context: Context) {
            if (isCleaning) return
            isCleaning = true

            var totalSize: Long = 0
            var fileCount = 0
            try {
                PojavApplication.sExecutorService.execute {
                    val list = PathAndUrlManager.DIR_CACHE.listFiles()?.let {
                        PathAndUrlManager.DIR_APP_CACHE.listFiles()?.let { it1 ->
                            getList(it, it1)
                        }
                    }

                    PathAndUrlManager.FILE_VERSION_LIST.let {
                        val file = File(it)
                        if (file.exists()) list?.add(file)
                    }

                    list?.let{
                        for (file in list) {
                            ++fileCount
                            totalSize += FileUtils.sizeOf(file)
                            FileUtils.deleteQuietly(file)
                        }
                    }

                    Tools.runOnUiThread {
                        if (fileCount != 0) {
                            Toast.makeText(context,
                                context.getString(R.string.clear_up_cache_clean_up, FileTools.formatFileSize(totalSize)),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context,
                                context.getString(R.string.clear_up_cache_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } finally {
                isCleaning = false
            }
        }

        private fun getList(vararg filesArray: Array<File>): MutableList<File> {
            val filesList: MutableList<File> = ArrayList()
            for (fileArray in filesArray) {
                filesList.addAll(listOf(*fileArray))
            }

            return filesList
        }
    }
}
