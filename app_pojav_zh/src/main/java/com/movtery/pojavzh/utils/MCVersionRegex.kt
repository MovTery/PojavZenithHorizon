package com.movtery.pojavzh.utils

import java.util.regex.Pattern

object MCVersionRegex {
    @JvmStatic val RELEASE_REGEX: Pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+$|^\\d+\\.\\d+$")
    @JvmStatic val SNAPSHOT_REGEX: Pattern = Pattern.compile("^\\d+[a-zA-Z]\\d+[a-zA-Z]$")
}