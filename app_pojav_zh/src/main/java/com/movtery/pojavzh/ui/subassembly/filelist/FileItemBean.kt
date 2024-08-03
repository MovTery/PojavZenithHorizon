package com.movtery.pojavzh.ui.subassembly.filelist

import android.graphics.drawable.Drawable
import com.movtery.pojavzh.utils.stringutils.SortStrings.compareChar
import java.io.File

class FileItemBean : Comparable<FileItemBean?> {
    @JvmField
    var image: Drawable? = null
    @JvmField
    var file: File? = null
    @JvmField
    var name: String? = null
    @JvmField
    var isHighlighted: Boolean = false
    @JvmField
    var isCanCheck: Boolean = true

    constructor()

    constructor(image: Drawable?, file: File?, name: String?) {
        this.image = image
        this.file = file
        this.name = name
    }

    override fun compareTo(other: FileItemBean?): Int {
        if (other == null) {
            throw NullPointerException("Cannot compare to null.")
        }

        val thisName = if ((this.file != null)) file!!.name else name!!
        val otherName = if ((other.file != null)) other.file!!.name else other.name!!

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
