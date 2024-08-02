package com.movtery.pojavzh.ui.subassembly.viewmodel

import androidx.lifecycle.ViewModel
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

class ModApiViewModel : ViewModel() {
    var modApi: ModpackApi? = null
    var modItem: ModItem? = null
    var isModpack: Boolean = false
    var modsPath: String? = null
}
