package com.movtery.zalithlauncher.feature.download.item

/**
 * 用于记录搜索结果
 */
class SearchResult {
    var previousCount: Int = 0
    var totalResultCount: Int = 0
    val infoItems: MutableList<InfoItem> = ArrayList()
    var isLastPage: Boolean = false

    override fun toString(): String {
        return "SearchResult(previousCount=$previousCount, totalResultCount=$totalResultCount, infoItems=$infoItems, isLastPage=$isLastPage)"
    }
}