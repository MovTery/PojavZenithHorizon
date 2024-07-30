package com.movtery.pojavzh.ui.subassembly.customcontrols

import kotlin.math.min

class ControlInfoData : Comparable<ControlInfoData?> {
    @JvmField
    var fileName: String? = null
    @JvmField
    var name: String = "null"
    @JvmField
    var version: String = "null"
    @JvmField
    var author: String = "null"
    @JvmField
    var desc: String = "null"

    override fun compareTo(other: ControlInfoData?): Int {
        if (other == null) {
            throw NullPointerException("Cannot compare to null.")
        }

        val thisName = if ((this.fileName != null)) this.fileName else this.name
        val otherName = if ((other.fileName != null)) other.fileName else other.name

        return compareChar(thisName, otherName)
    }

    private fun compareChar(first: String?, second: String?): Int {
        val firstLength = first!!.length
        val secondLength = second!!.length

        //遍历两个字符串的字符
        for (i in 0 until min(firstLength.toDouble(), secondLength.toDouble()).toInt()) {
            val firstChar = first[i].lowercaseChar()
            val secondChar = second[i].lowercaseChar()

            val compare = firstChar.compareTo(secondChar)
            if (compare != 0) {
                return compare
            }
        }

        return firstLength.compareTo(secondLength)
    }
}
