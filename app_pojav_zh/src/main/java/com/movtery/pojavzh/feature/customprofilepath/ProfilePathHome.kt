package com.movtery.pojavzh.feature.customprofilepath

class ProfilePathHome {
    companion object {
        @JvmStatic
        val gameHome: String
            get() = ProfilePathManager.currentPath + "/.minecraft"

        @JvmStatic
        val versionsHome: String
            get() = "$gameHome/versions"

        @JvmStatic
        val librariesHome: String
            get() = "$gameHome/libraries"

        @JvmStatic
        val assetsHome: String
            get() = "$gameHome/assets"

        @JvmStatic
        val resourcesHome: String
            get() = "$gameHome/resources"
    }
}
