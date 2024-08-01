package com.movtery.pojavzh.utils

import java.util.regex.Pattern

class MCVersionRegex {
    companion object{
        @JvmStatic val RELEASE_REGEX: Pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+$|^\\d+\\.\\d+$")
    }
}