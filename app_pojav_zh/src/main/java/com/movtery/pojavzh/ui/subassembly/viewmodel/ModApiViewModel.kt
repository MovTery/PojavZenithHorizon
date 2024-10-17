package com.movtery.pojavzh.ui.subassembly.viewmodel

import androidx.lifecycle.ViewModel
import com.movtery.pojavzh.feature.mod.item.ModItem
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi

class ModApiViewModel : ViewModel() {
    lateinit var modApi: ModpackApi
    lateinit var modItem: ModItem
    var isModpack: Boolean = false
    var modsPath: String? = null
}
