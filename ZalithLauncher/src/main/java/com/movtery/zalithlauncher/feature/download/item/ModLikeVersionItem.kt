package com.movtery.zalithlauncher.feature.download.item

import com.movtery.zalithlauncher.feature.download.enums.ModLoader
import com.movtery.zalithlauncher.feature.download.enums.VersionType
import java.util.Date

/**
 * @param modloaders 该版本的 Mod 加载器信息
 */
open class ModLikeVersionItem(
    projectId: String,
    title: String,
    downloadCount: Long,
    uploadDate: Date,
    mcVersions: List<String>,
    versionType: VersionType,
    fileName: String,
    fileHash: String?,
    fileUrl: String,
    val modloaders: List<ModLoader>
) : VersionItem(
    projectId, title, downloadCount, uploadDate, mcVersions, versionType, fileName, fileHash, fileUrl
) {
    override fun toString(): String {
        return "ModVersionItem(" +
                "projectId='$projectId', " +
                "title='$title', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "mcVersions=$mcVersions, " +
                "versionType=$versionType, " +
                "fileName='$fileName', " +
                "fileHash='$fileHash', " +
                "fileUrl='$fileUrl', " +
                "modloaders=$modloaders" +
                ")"
    }
}