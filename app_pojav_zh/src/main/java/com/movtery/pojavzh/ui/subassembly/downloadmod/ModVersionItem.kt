package com.movtery.pojavzh.ui.subassembly.downloadmod

import com.movtery.pojavzh.feature.mod.ModLoaderList
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType.VersionTypeEnum
import java.util.Date

data class ModVersionItem(
    @JvmField val versionId: Array<String>,
    @JvmField val name: String,
    @JvmField val title: String,
    @JvmField val modloaders: Array<ModLoaderList.ModLoader>,
    @JvmField val modDependencies: List<ModDependencies>,
    @JvmField val versionType: VersionTypeEnum,
    @JvmField val versionHash: String?,
    @JvmField val download: Int,
    @JvmField val downloadUrl: String,
    @JvmField val fileDate: Date
)
