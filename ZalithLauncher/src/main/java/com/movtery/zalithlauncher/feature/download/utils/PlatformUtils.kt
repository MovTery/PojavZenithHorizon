package com.movtery.zalithlauncher.feature.download.utils

import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler

class PlatformUtils {
    companion object {
        fun createCurseForgeApi() = ApiHandler(
            "https://api.curseforge.com/v1",
            PojavApplication.getResString(R.string.curseforge_api_key)
        )
    }
}