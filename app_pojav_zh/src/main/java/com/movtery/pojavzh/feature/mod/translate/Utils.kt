package com.movtery.pojavzh.feature.mod.translate

import net.kdt.pojavlaunch.Tools
import java.io.InputStream
import org.apache.commons.text.similarity.FuzzyScore
import java.util.Locale

class Utils {
    companion object {
        fun identificationData(input: InputStream, infos: MutableList<TranslateInfoItem>) {
            val data = Tools.read(input)

            val lines = data.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                val parts = line.split(";;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size == 3) {
                    val info = TranslateInfoItem(parts[0].trim(), parts[1].trim(), parts[2].trim())
                    infos.add(info)
                }
            }
        }

        fun searchBestMatch(infos: MutableList<TranslateInfoItem>, input: String, matchBy: (TranslateInfoItem) -> String, returnBy: (TranslateInfoItem) -> String): String? {
            val normalizedInput = input.trim().lowercase()
            //完全匹配
            infos.firstOrNull { matchBy(it).equals(normalizedInput, ignoreCase = true) }?.let { return returnBy(it) }
            //包含
            val candidates = infos.filter { matchBy(it).contains(normalizedInput, ignoreCase = true) }

            if (candidates.size == 1) return returnBy(candidates[0])
            return candidates.maxByOrNull { calculateScore(matchBy(it), normalizedInput) }?.let { returnBy(it) }
        }

        //使用FuzzyScore计算相似度
        private fun calculateScore(candidate: String, input: String): Int {
            val fuzzyScore = FuzzyScore(Locale.getDefault())
            return fuzzyScore.fuzzyScore(candidate, input)
        }
    }
}