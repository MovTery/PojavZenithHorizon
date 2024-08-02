package com.movtery.pojavzh.ui.fragment

import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener

class DownloadQuiltFragment : DownloadFabricLikeFragment(FabriclikeUtils.QUILT_UTILS, R.drawable.ic_quilt), ModloaderDownloadListener {
    companion object {
        const val TAG: String = "DownloadQuiltFragment"
    }
}