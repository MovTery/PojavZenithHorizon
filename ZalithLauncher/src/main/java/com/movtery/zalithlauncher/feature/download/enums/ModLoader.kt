package com.movtery.zalithlauncher.feature.download.enums

enum class ModLoader(val type: Int, val loaderName: String, val curseforgeId: String, val modrinthName: String) {
    ALL(-1, "", "", ""),
    FORGE(0, "Forge", "1", "forge"),
    NEOFORGE(1, "NeoForge", "6", "neoforge"),
    FABRIC(2, "Fabric", "4", "fabric"),
    QUILT(3, "Quilt", "5", "quilt")
}