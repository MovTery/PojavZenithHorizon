package com.movtery.utils.stringutils;

public class StringFilter {
    public static boolean containsAllCharacters(String input, String examine) {
        for (char c : examine.toCharArray()) {
            if (input.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }
}
