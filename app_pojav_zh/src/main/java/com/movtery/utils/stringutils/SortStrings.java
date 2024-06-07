package com.movtery.utils.stringutils;

public class SortStrings {
    public static int compareChar(String thisName, String otherName) {
        int firstLength = thisName.length();
        int secondLength = otherName.length();

        //遍历两个字符串的字符
        for (int i = 0; i < Math.min(firstLength, secondLength); i++) {
            char firstChar = Character.toLowerCase(thisName.charAt(i));
            char secondChar = Character.toLowerCase(otherName.charAt(i));

            int compare = Character.compare(firstChar, secondChar);
            if (compare != 0) {
                return compare;
            }
        }

        return Integer.compare(firstLength, secondLength);
    }
}
