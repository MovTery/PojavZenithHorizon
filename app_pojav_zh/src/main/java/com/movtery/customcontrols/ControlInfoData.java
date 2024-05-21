package com.movtery.customcontrols;

public class ControlInfoData implements Comparable<ControlInfoData> {
    public String fileName;
    public String name = "null";
    public String version = "null";
    public String author = "null";
    public String desc = "null";

    public ControlInfoData() {
    }

    @Override
    public int compareTo(ControlInfoData o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null.");
        }

        String thisName = (this.fileName != null) ? this.fileName : this.name;
        String otherName = (o.fileName != null) ? o.fileName : o.name;

        return compareChar(thisName, otherName);
    }

    private int compareChar(String first, String second) {
        int firstLength = first.length();
        int secondLength = second.length();

        //遍历两个字符串的字符
        for (int i = 0; i < Math.min(firstLength, secondLength); i++) {
            char firstChar = Character.toLowerCase(first.charAt(i));
            char secondChar = Character.toLowerCase(second.charAt(i));

            int compare = Character.compare(firstChar, secondChar);
            if (compare != 0) {
                return compare;
            }
        }

        return Integer.compare(firstLength, secondLength);
    }
}
