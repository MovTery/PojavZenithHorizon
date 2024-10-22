package com.movtery.zalithlauncher.feature.download.enums

import net.kdt.pojavlaunch.R

enum class Sort(val resNameID: Int, val curseforge: Int, val modrinth: String) {
    RELEVANT(R.string.download_ui_sort_by_relevant, 1, "relevance"),
    DOWNLOADS(R.string.download_ui_sort_by_total_downloads, 6, "downloads"),
    POPULARITY(R.string.download_ui_sort_by_popularity, 2, "follows"),
    NEWEST(R.string.download_ui_sort_by_recently_created, 11, "newest"),
    UPDATED(R.string.download_ui_sort_by_recently_updated, 3, "updated")
}