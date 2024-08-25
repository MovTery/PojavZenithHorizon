package com.movtery.pojavzh.feature.mod.translate

import com.movtery.pojavzh.feature.log.Logging
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools

object ModPackTranslateManager {
    private val infos: MutableList<ModTranslateInfo> = ArrayList()

    init {
        runCatching {
            val input = PojavApplication.getContext().assets.open("data_modpack.txt")
            Utils.identificationData(input, infos)
        }.getOrElse { e ->
            Logging.e("ModTranslateManager", Tools.printToString(e))
        }
    }

    /**
     * 通过原始名称得到一个准确或还算准确的中文翻译名
     */
    fun searchToChinese(origin: String): String? {
        if (infos.isEmpty()) return null
        return Utils.searchBestMatch(infos, origin, matchBy = { it.originName }, returnBy = { it.chineseName })
    }

    /**
     * 通过中文名称得到一个准确或还算准确的原始名
     */
    fun searchToOrigin(chinese: String): String? {
        if (infos.isEmpty()) return null
        return Utils.searchBestMatch(infos, chinese, matchBy = { it.chineseName }, returnBy = { it.originName })
    }
}
