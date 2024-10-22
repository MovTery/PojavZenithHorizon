package com.movtery.zalithlauncher.feature.download.utils

import com.movtery.zalithlauncher.feature.download.enums.Sort

class SortUtils {
    companion object {
        fun getSortList(): List<Sort> {
            val list: MutableList<Sort> = ArrayList()
            Sort.entries.forEach { list.add(it) }
            return list
        }
    }
}