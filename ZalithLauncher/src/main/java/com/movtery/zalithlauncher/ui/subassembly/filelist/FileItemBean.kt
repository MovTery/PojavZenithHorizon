package com.movtery.zalithlauncher.ui.subassembly.filelist

import android.graphics.drawable.Drawable
import com.movtery.zalithlauncher.utils.stringutils.SortStrings.Companion.compareChar
import java.io.File

class FileItemBean(
    @JvmField
    val name: String
) : Comparable<FileItemBean?> {
    @JvmField
    var image: Drawable? = null
    @JvmField
    var file: File? = null
    @JvmField
    var isHighlighted: Boolean = false
    @JvmField
    var isCanCheck: Boolean = true

    constructor(file: File) : this(file.name) {
        this.file = file
    }

    constructor(name: String, image: Drawable?) : this(name) {
        this.image = image
    }

    override fun compareTo(other: FileItemBean?): Int {
        other ?: run { throw NullPointerException("Cannot compare to null.") }

        val thisName = file?.name ?: name
        val otherName = other.file?.name ?: other.name

        //首先检查文件是否为目录
        if (this.file != null && file!!.isDirectory) {
            if (other.file != null && !other.file!!.isDirectory) {
                //目录排在文件前面
                return -1
            }
        } else if (other.file != null && other.file!!.isDirectory) {
            //文件排在目录后面
            return 1
        }

        return compareChar(thisName, otherName)
    }

    override fun toString(): String {
        return "FileItemBean{" +
                "file=" + file +
                ", name='" + name + '\'' +
                '}'
    }
}
