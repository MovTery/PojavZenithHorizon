package com.movtery.pojavzh.utils;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberWithUnits {
    private static final String[] UNITS_EN = {"", "K", "M"}; //英文单位：千、百万
    private static final String[] UNITS_ZH = {"", PojavApplication.getResString(R.string.zh_wan), PojavApplication.getResString(R.string.zh_yi)}; //中文单位:万、亿

    public static String formatNumberWithUnit(long number, boolean isEnglish) {
        if (isEnglish) {
            return formatNumberWithUnitEnglish(number);
        } else {
            return formatNumberWithUnitChinese(number);
        }
    }

    private static String formatNumberWithUnitChinese(long number) {
        return formatNumber(number, 10000, UNITS_ZH);
    }

    private static String formatNumberWithUnitEnglish(long number) {
        return formatNumber(number, 1000, UNITS_EN);
    }

    private static String formatNumber(long number, int stage, String[] units) {
        BigDecimal bigDecimal = new BigDecimal(number);
        int unitIndex = 0;

        while (bigDecimal.compareTo(BigDecimal.valueOf(stage)) >= 0 && unitIndex < units.length - 1) {
            bigDecimal = bigDecimal.divide(BigDecimal.valueOf(stage), 2, RoundingMode.DOWN);
            unitIndex++;
        }

        //检查是否为空的单位，如果是，那么就不做格式化，直接返回原始值
        if (units[unitIndex].isEmpty()) {
            return String.valueOf(number);
        } else {
            DecimalFormat df = new DecimalFormat("#.00");
            String formattedNumber = df.format(bigDecimal.setScale(2, RoundingMode.DOWN).doubleValue());
            return StringUtils.insertSpace(formattedNumber, units[unitIndex]);
        }
    }
}
