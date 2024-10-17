package com.movtery.pojavzh.feature.mod.item

import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem

class ModDetail(
    item: ModItem,
    /* SHA 1 hashes, null if a hash is unavailable */
    var modVersionItems: MutableList<ModVersionItem>?
) : ModItem(
    item.apiSource,
    item.isModpack,
    item.id,
    item.title,
    item.subTitle,
    item.description,
    item.downloadCount,
    item.categories,
    item.modloaders,
    item.imageUrl
)
