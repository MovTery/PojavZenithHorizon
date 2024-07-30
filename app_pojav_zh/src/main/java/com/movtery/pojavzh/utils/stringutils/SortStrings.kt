package com.movtery.pojavzh.utils.stringutils

import kotlin.math.min

object SortStrings {
    @JvmStatic
    fun compareChar(thisName: String, otherName: String): Int {
        val firstLength = thisName.length
        val secondLength = otherName.length

        //遍历两个字符串的字符
        for (i in 0 until min(firstLength.toDouble(), secondLength.toDouble()).toInt()) {
            val firstChar = thisName[i].lowercaseChar()
            val secondChar = otherName[i].lowercaseChar()

            val compare = firstChar.compareTo(secondChar)
            if (compare != 0) {
                return compare
            }
        }

        return firstLength.compareTo(secondLength)
    }
}
