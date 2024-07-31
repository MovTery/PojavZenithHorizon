package com.movtery.pojavzh.ui.subassembly.viewmodel

import androidx.lifecycle.ViewModel
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

class ModApiViewModel(
    @JvmField
    var modApi: ModpackApi,
    @JvmField
    var modItem: ModItem,
    @JvmField
    var isModpack: Boolean,
    @JvmField
    var modsPath: String
): ViewModel()
