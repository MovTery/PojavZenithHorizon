package com.movtery.pojavzh.ui.fragment

import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener

class DownloadFabricFragment : DownloadFabricLikeFragment(FabriclikeUtils.FABRIC_UTILS, R.drawable.ic_fabric), ModloaderDownloadListener {
    companion object {
        const val TAG: String = "DownloadFabricFragment"
    }
}