package com.movtery.zalithlauncher.feature.download.item

import com.movtery.zalithlauncher.feature.download.enums.Category
import com.movtery.zalithlauncher.feature.download.enums.DependencyType
import com.movtery.zalithlauncher.feature.download.enums.ModLoader
import com.movtery.zalithlauncher.feature.download.enums.Platform
import java.util.Date

/**
 *
 */
open class DependenciesInfoItem(
    platform: Platform,
    projectId: String,
    author: Array<String>?,
    title: String,
    description: String,
    downloadCount: Long,
    uploadDate: Date,
    iconUrl: String?,
    category: List<Category>,
    modloaders: List<ModLoader>,
    val dependencyType: DependencyType
) : ModInfoItem (
    platform, projectId, author, title, description, downloadCount, uploadDate, iconUrl, category, modloaders
), Comparable<DependenciesInfoItem> {
    override fun toString(): String {
        return "InfoItem(" +
                "platform='$platform', " +
                "projectId='$projectId', " +
                "author=${author.contentToString()}, " +
                "title='$title', " +
                "description='$description', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "iconUrl='$iconUrl', " +
                "category=$category" +
                "modloaders=$modloaders" +
                "dependencyType=$dependencyType" +
                ")"
    }

    override fun compareTo(other: DependenciesInfoItem): Int {
        return dependencyType.compareTo(other.dependencyType)
    }
}