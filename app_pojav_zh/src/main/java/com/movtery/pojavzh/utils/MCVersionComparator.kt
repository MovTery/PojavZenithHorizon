package com.movtery.pojavzh.utils

import com.movtery.pojavzh.utils.stringutils.StringUtils
import kotlin.math.max

class MCVersionComparator {
    companion object {
        @JvmStatic
        fun versionCompare(v1: String?, v2: String?): Int {
            val numbers1 = StringUtils.extractNumbers(v1)
            val numbers2 = StringUtils.extractNumbers(v2)

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
}
