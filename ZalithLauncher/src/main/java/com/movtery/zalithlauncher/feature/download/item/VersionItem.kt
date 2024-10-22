package com.movtery.zalithlauncher.feature.download.item

import com.movtery.zalithlauncher.feature.download.enums.VersionType
import java.util.Date

/**
 * 版本信息类
 * @param projectId 该版本所在项目的唯一标识
 * @param title 该版本的标题
 * @param downloadCount 该版本的总下载量
 * @param uploadDate 该版本的上传日期
 * @param mcVersions 该版本的 MC版本
 * @param versionType 该版本的版本状态
 * @param fileName 该版本的文件名称
 * @param fileHash 该版本的文件HASH值
 * @param fileUrl 该版本的文件下载链接
 */
open class VersionItem(
    val projectId: String,
    val title: String,
    val downloadCount: Long,
    val uploadDate: Date,
    val mcVersions: List<String>,
    val versionType: VersionType,
    val fileName: String,
    val fileHash: String?,
    val fileUrl: String
) {
    override fun toString(): String {
        return "VersionItem(" +
                "projectId='$projectId', " +
                "title='$title', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "mcVersions=$mcVersions, " +
                "versionType=$versionType, " +
                "fileName='$fileName'" +
                "fileHash='$fileHash'" +
                "fileUrl='$fileUrl'" +
                ")"
    }
}