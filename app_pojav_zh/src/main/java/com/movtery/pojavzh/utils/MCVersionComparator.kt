package com.movtery.pojavzh.utils

import kotlin.math.max

object MCVersionComparator {
    @JvmStatic
    fun versionCompare(v1: String?, v2: String?): Int {
        val numbers1 = ZHTools.extractNumbers(v1)
        val numbers2 = ZHTools.extractNumbers(v2)

        val length = max(numbers1.size.toDouble(), numbers2.size.toDouble())
            .toInt()
        for (i in 0 until length) {
            val num1 = if (i < numbers1.size) numbers1[i] else 0
            val num2 = if (i < numbers2.size) numbers2[i] else 0
            val cmp = num1.compareTo(num2)
            if (cmp != 0) {
                return -cmp
            }
        }
        return 0
    }
}
