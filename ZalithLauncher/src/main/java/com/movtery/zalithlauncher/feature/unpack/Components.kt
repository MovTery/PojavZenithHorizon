package com.movtery.zalithlauncher.feature.unpack

import net.kdt.pojavlaunch.R

enum class Components(val component: String, val summary: Int?, val privateDirectory: Boolean) {
    OTHER_LOGIN("other_login", null, false),
    CACIOCAVALLO("caciocavallo", R.string.splash_screen_cacio, false),
    CACIOCAVALLO17("caciocavallo17", R.string.splash_screen_cacio, false),
    LWJGL3("lwjgl3", R.string.splash_screen_lwjgl, false),
    SECURITY("security", null, true),
    FORGE_INSTALLER("forge_installer", null, true),
}