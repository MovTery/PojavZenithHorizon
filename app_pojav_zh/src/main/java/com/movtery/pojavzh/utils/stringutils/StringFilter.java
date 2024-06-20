package com.movtery.pojavzh.utils.stringutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFilter {
    /**
     * 检查输入字符串是否包含指定的子字符串。
     * @param input 输入字符串
     * @param substring 检查子字符串
     * @param caseSensitive 是否区分大小写
     * @return 如果输入字符串包含指定的子字符串，返回true；否则返回false
     */
    public static boolean containsSubstring(String input, String substring, boolean caseSensitive) {
        String adjustedInput = caseSensitive ? input : input.toLowerCase();
        String adjustedSubstring = caseSensitive ? substring : substring.toLowerCase();
        String regex = Pattern.quote(adjustedSubstring);
        Pattern compiledPattern = Pattern.compile(regex);
        Matcher matcher = compiledPattern.matcher(adjustedInput);
        return matcher.find();
    }
}
