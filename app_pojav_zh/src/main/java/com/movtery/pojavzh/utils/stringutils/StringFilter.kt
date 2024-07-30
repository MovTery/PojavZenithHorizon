package com.movtery.pojavzh.utils.stringutils

import java.util.Locale
import java.util.regex.Pattern

object StringFilter {
    /**
     * 检查输入字符串是否包含指定的子字符串。
     * @param input 输入字符串
     * @param substring 检查子字符串
     * @param caseSensitive 是否区分大小写
     * @return 如果输入字符串包含指定的子字符串，返回true；否则返回false
     */
    @JvmStatic
    fun containsSubstring(input: String, substring: String, caseSensitive: Boolean): Boolean {
        val adjustedInput = if (caseSensitive) input else input.lowercase(Locale.getDefault())
        val adjustedSubstring = if (caseSensitive) substring else substring.lowercase(Locale.getDefault())
        val regex = Pattern.quote(adjustedSubstring)
        val compiledPattern = Pattern.compile(regex)
        val matcher = compiledPattern.matcher(adjustedInput)
        return matcher.find()
    }
}
