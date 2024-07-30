package com.movtery.pojavzh.utils

import android.content.Context
import android.widget.Toast
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import org.apache.commons.io.FileUtils
import java.io.File

object CleanUpCache {
    private var isCleaning = false

    @JvmStatic
    fun start(context: Context) {
        if (isCleaning) return
        isCleaning = true

        var totalSize: Long = 0
        var fileCount = 0
        try {
            val list = Tools.DIR_CACHE.listFiles()?.let {
                ZHTools.DIR_APP_CACHE.listFiles()?.let { it1 ->
                    getList(it, it1)
                }
            }

            if (list != null) {
                for (file in list) {
                    if (file.name == "user_icon") continue

                    ++fileCount

                    totalSize += if (file.isDirectory) {
                        FileUtils.sizeOfDirectory(file)
                    } else {
                        FileUtils.sizeOf(file)
                    }

                    FileUtils.deleteQuietly(file)
                }
            }

            val finalFileCount = fileCount
            val finalTotalSize = totalSize
            Tools.runOnUiThread {
                if (finalFileCount != 0) {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.zh_clear_up_cache_clean_up,
                            ZHTools.formatFileSize(finalTotalSize)
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.zh_clear_up_cache_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } finally {
            isCleaning = false
        }
    }

    private fun getList(vararg filesArray: Array<File>): List<File> {
        val filesList: MutableList<File> = ArrayList()
        for (fileArray in filesArray) {
            filesList.addAll(listOf(*fileArray))
        }

        return filesList
    }
}
