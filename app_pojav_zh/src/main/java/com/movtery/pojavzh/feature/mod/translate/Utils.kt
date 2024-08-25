package com.movtery.pojavzh.feature.mod.translate

import net.kdt.pojavlaunch.Tools
import java.io.InputStream
import kotlin.math.abs

class Utils {
    companion object {
        fun identificationData(input: InputStream, infos: MutableList<ModTranslateInfo>) {
            val data = Tools.read(input)

            val lines = data.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                val parts = line.split(";;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size == 3) {
                    val info = ModTranslateInfo(parts[0].trim(), parts[1].trim(), parts[2].trim())
                    infos.add(info)
                }
            }
        }

        fun searchBestMatch(infos: MutableList<ModTranslateInfo>, input: String, matchBy: (ModTranslateInfo) -> String, returnBy: (ModTranslateInfo) -> String): String? {
            val normalizedInput = input.trim().lowercase()
            //完全匹配
            infos.firstOrNull { matchBy(it).equals(normalizedInput, ignoreCase = true) }?.let { return returnBy(it) }
            //包含
            val candidates = infos.filter { matchBy(it).contains(normalizedInput, ignoreCase = true) }

            if (candidates.size == 1) return returnBy(candidates[0])
            return candidates.maxByOrNull { calculateScore(matchBy(it), normalizedInput) }?.let { returnBy(it) }
        }

        //最最最最最简单的积分算法 1.长度越接近，分数越高 2.完全匹配的词越多，分数越高
        private fun calculateScore(candidate: String, input: String): Int {
            val candidateWords = candidate.lowercase().split("\\s+".toRegex())
            val inputWords = input.lowercase().split("\\s+".toRegex())

            var score = 0

            score += 100 - abs(candidate.length - input.length) // 长度相近性得分
            score += candidateWords.count { it in inputWords } * 50//词汇匹配得分

            return score
        }
    }
}