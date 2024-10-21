package com.movtery.pojavzh.feature.download.utils

import com.movtery.pojavzh.feature.download.enums.Sort

class SortUtils {
    companion object {
        fun getSortList(): List<Sort> {
            val list: MutableList<Sort> = ArrayList()
            Sort.entries.forEach { list.add(it) }
            return list
        }
    }
}