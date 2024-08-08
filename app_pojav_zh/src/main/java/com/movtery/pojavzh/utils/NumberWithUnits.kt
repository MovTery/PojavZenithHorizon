package com.movtery.pojavzh.utils

import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class NumberWithUnits {
    companion object {
        private val UNITS_EN = arrayOf("", "K", "M") //英文单位：千、百万
        private val UNITS_ZH = arrayOf(
            "",
            PojavApplication.getResString(R.string.zh_wan),
            PojavApplication.getResString(R.string.zh_yi)
        ) //中文单位:万、亿

        @JvmStatic
        fun formatNumberWithUnit(number: Long, isEnglish: Boolean): String {
            return if (isEnglish) {
                formatNumberWithUnitEnglish(number)
            } else {
                formatNumberWithUnitChinese(number)
            }
        }

        private fun formatNumberWithUnitChinese(number: Long): String {
            return formatNumber(number, 10000, UNITS_ZH)
        }

        private fun formatNumberWithUnitEnglish(number: Long): String {
            return formatNumber(number, 1000, UNITS_EN)
        }

        private fun formatNumber(number: Long, stage: Int, units: Array<String>): String {
            var bigDecimal = BigDecimal(number)
            var unitIndex = 0

            while (bigDecimal >= BigDecimal.valueOf(stage.toLong()) && unitIndex < units.size - 1) {
                bigDecimal = bigDecimal.divide(BigDecimal.valueOf(stage.toLong()), 2, RoundingMode.DOWN)
                unitIndex++
            }

            //检查是否为空的单位，如果是，那么就不做格式化，直接返回原始值
            if (units[unitIndex].isEmpty()) {
                return number.toString()
            } else {
                val df = DecimalFormat("#.00")
                val formattedNumber = df.format(bigDecimal.setScale(2, RoundingMode.DOWN).toDouble())
                return StringUtils.insertSpace(formattedNumber, units[unitIndex])
            }
        }
    }
}
