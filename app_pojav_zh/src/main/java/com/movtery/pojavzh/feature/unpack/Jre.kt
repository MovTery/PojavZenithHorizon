package com.movtery.pojavzh.feature.unpack

import net.kdt.pojavlaunch.R

enum class Jre(val majorVersion: Int, val jreName: String, val jrePath: String, val summary: Int) {
    JRE_8(8, "Internal-8", "components/jre-8", R.string.splash_screen_jre8),
    JRE_17(17, "Internal-17", "components/jre-17", R.string.splash_screen_jre17),
    JRE_21(21, "Internal-21", "components/jre-21", R.string.splash_screen_jre21)
}