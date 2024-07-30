package com.movtery.pojavzh.ui.subassembly.downloadmod

object VersionType {
    @JvmStatic
    fun getVersionType(typeString: String?): VersionTypeEnum {
        return when (typeString) {
            "beta", "2" -> VersionTypeEnum.BETA
            "alpha", "3" -> VersionTypeEnum.ALPHA
            "release", "1" -> VersionTypeEnum.RELEASE
            else -> VersionTypeEnum.RELEASE
        }
    }

    enum class VersionTypeEnum {
        RELEASE, BETA, ALPHA
    }
}
