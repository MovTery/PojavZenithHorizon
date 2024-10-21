package com.movtery.pojavzh.feature.download.enums

import com.movtery.pojavzh.feature.download.platform.AbstractPlatformHelper
import com.movtery.pojavzh.feature.download.platform.curseforge.CurseForgeHelper
import com.movtery.pojavzh.feature.download.platform.modrinth.ModrinthHelper

enum class Platform(val pName: String, val helper: AbstractPlatformHelper) {
    MODRINTH("Modrinth", ModrinthHelper()),
    CURSEFORGE("CurseForge", CurseForgeHelper())
}