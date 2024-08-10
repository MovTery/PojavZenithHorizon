package com.movtery.pojavzh.ui.subassembly.downloadmod

import com.movtery.pojavzh.feature.mod.ModLoaderList
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType.VersionTypeEnum

class ModVersionItem(
    @JvmField val versionId: Array<String>,
    @JvmField val name: String,
    @JvmField val title: String,
    @JvmField val modloaders: Array<ModLoaderList.ModLoader>,
    @JvmField val modDependencies: List<ModDependencies>,
    @JvmField val versionType: VersionTypeEnum,
    @JvmField val versionHash: String,
    @JvmField val download: Int,
    @JvmField val downloadUrl: String
) {
    override fun toString(): String {
        return "ModVersionItem{" +
                "versionId=" + versionId.contentToString() +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", modloaders='" + modloaders + '\'' +
                ", versionHash='" + versionHash + '\'' +
                ", download=" + download +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", modDependencies=" + modDependencies +
                ", versionType=" + versionType +
                '}'
    }
}
