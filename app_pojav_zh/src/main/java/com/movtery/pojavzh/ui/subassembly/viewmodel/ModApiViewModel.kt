package com.movtery.pojavzh.ui.subassembly.viewmodel

import androidx.lifecycle.ViewModel
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

class ModApiViewModel : ViewModel() {
    @JvmField
    var modApi: ModpackApi? = null
    @JvmField
    var modItem: ModItem? = null
    @JvmField
    var isModpack: Boolean = false
    @JvmField
    var modsPath: String? = null
}
