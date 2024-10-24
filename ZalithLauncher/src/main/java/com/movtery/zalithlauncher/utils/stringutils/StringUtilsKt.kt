package com.movtery.zalithlauncher.utils.stringutils

class StringUtilsKt {
    companion object {
        fun getNonEmptyOrBlank(string: String?): String? {
            return string?.takeIf { it.isNotEmpty() && it.isNotBlank() }
        }
    }
}