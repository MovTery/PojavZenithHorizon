package com.movtery.pojavzh.feature.mod.item

import com.movtery.pojavzh.feature.mod.ModCategory
import com.movtery.pojavzh.feature.mod.ModLoaderList
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModSource

open class ModItem(
    val apiSource: Int,
    val isModpack: Boolean,
    val id: String,
    val title: String,
    val subTitle: String?,
    val description: String,
    val downloadCount: Long,
    val categories: Set<ModCategory.Category>,
    val modloaders: Array<ModLoaderList.ModLoader>,
    val imageUrl: String?
) : ModSource() {
    override fun toString(): String {
        return "ModItem{" +
                "id='$id'" +
                ", title='$title'" +
                ", subTitle='$subTitle'" +
                ", description='$description'" +
                ", downloadCount=$downloadCount" +
                ", modloaders=${modloaders.contentToString()}" +
                ", imageUrl='$imageUrl'" +
                '}'
    }
}