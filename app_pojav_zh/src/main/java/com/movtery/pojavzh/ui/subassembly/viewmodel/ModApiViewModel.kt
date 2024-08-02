package com.movtery.pojavzh.ui.subassembly.viewmodel

import androidx.lifecycle.ViewModel
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

class ModApiViewModel(
    var modApi: ModpackApi,
    var modItem: ModItem,
    var isModpack: Boolean = false,
    var modsPath: String
) : ViewModel()
