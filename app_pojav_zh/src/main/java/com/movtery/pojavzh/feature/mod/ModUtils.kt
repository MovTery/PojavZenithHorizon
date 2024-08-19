package com.movtery.pojavzh.feature.mod

import com.movtery.pojavzh.utils.file.FileTools.Companion.renameFile
import java.io.File

class ModUtils {
    companion object {
        const val JAR_FILE_SUFFIX: String = ".jar"
        const val DISABLE_JAR_FILE_SUFFIX: String = "$JAR_FILE_SUFFIX.disabled"

        @JvmStatic
        fun disableMod(file: File?) {
            val fileName = file!!.name
            val fileParent = file.parent
            val newFile = File(fileParent, "$fileName.disabled")
            renameFile(file, newFile)
        }

        @JvmStatic
        fun enableMod(file: File?) {
            val fileName = file!!.name
            val fileParent = file.parent
            var newFileName = fileName.substring(0, fileName.lastIndexOf(DISABLE_JAR_FILE_SUFFIX))
            if (!fileName.endsWith(JAR_FILE_SUFFIX)) newFileName += JAR_FILE_SUFFIX //如果没有.jar结尾，那么默认加上.jar后缀

            val newFile = File(fileParent, newFileName)
            renameFile(file, newFile)
        }
    }
}