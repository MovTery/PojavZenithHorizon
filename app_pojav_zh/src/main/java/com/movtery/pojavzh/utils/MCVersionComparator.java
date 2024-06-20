package com.movtery.pojavzh.utils;

import java.util.List;

public class MCVersionComparator {
    public static int versionCompare(String v1, String v2) {
        List<Integer> numbers1 = ZHTools.extractNumbers(v1);
        List<Integer> numbers2 = ZHTools.extractNumbers(v2);

        int length = Math.max(numbers1.size(), numbers2.size());
        for (int i = 0; i < length; i++) {
            int num1 = i < numbers1.size() ? numbers1.get(i) : 0;
            int num2 = i < numbers2.size() ? numbers2.get(i) : 0;
            int cmp = Integer.compare(num1, num2);
            if (cmp != 0) {
                return -cmp;
            }
        }
        return 0;
    }
}
