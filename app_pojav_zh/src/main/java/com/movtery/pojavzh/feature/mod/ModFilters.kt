package com.movtery.pojavzh.feature.mod

/**
 * Search filters, passed to APIs
 */
class ModFilters {
    var isModpack: Boolean = false
    var name: String? = null
    var mcVersion: String? = null
    var modloader: String? = null
    var sort: Int = 0
    var platform: ApiPlatform = ApiPlatform.BOTH
    var category: ModCategory.Category = ModCategory.Category.ALL

    enum class ApiPlatform {
        CURSEFORGE,
        MODRINTH,
        BOTH
    }
}