package com.movtery.utils.stringutils;

import java.util.HashMap;
import java.util.Map;

public class StringFilter {
    /***
     * 这个方法将用于检查任意字符串是否包含一些字符，同时还检查了输入字符串内字符出现的个数，是否与检查字符串内包含的字符个数一致
     * @param input 输入字符串
     * @param examine 检查字符串，用于检查输入字符串内是否包含此字符串内的字符
     * @return 返回输入字符串是否包含检查字符串
     */
    public static boolean containsAllCharacters(String input, String examine) {
        //记录每个字符的出现次数
        Map<Character, Integer> examineCountMap = new HashMap<>();
        for (char c : examine.toCharArray()) {
            examineCountMap.put(c, examineCountMap.getOrDefault(c, 0) + 1);
        }
        Map<Character, Integer> inputCountMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            inputCountMap.put(c, inputCountMap.getOrDefault(c, 0) + 1);
        }


        for (Map.Entry<Character, Integer> entry : examineCountMap.entrySet()) {
            char c = entry.getKey();
            int examineOccurrences = entry.getValue();
            int inputOccurrences = inputCountMap.getOrDefault(c, 0);
            if (inputOccurrences < examineOccurrences) {
                return false;
            }
        }

        return true;
    }
}
