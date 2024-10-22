package com.movtery.zalithlauncher.feature.download.install

import com.movtery.zalithlauncher.feature.log.Logging
import net.kdt.pojavlaunch.utils.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

class UnpackWorldZipHelper {
    companion object {
        fun unpackFile(zipFile: File, targetPath: File) {
            val path = extractLevelPath(zipFile) ?: return
            Logging.i("UnpackWorldZipHelper", "Found the level of the level.data file: $path")
            ZipFile(zipFile).use {
                val fileName = zipFile.name.removeSuffix(".${zipFile.extension}")
                ZipUtils.zipExtract(ZipFile(zipFile), path, File(targetPath, fileName))
                Logging.i("UnpackWorldZipHelper", "Decompression is complete")
            }
            FileUtils.deleteQuietly(zipFile)
        }

        /**
         * 读取zip文件，并找到level.data文件所在的路径
         * @param file 压缩包文件
         */
        private fun extractLevelPath(file: File): String? {
            if (!file.exists() || !file.isFile) {
                return null
            }

            if (!file.name.endsWith(".zip", ignoreCase = true)) {
                return null
            }

            ZipFile(file).use { zip ->
                val entries = zip.entries().asSequence() //转换为序列，方便过滤
                val levelDatEntry = entries.find { it.name.endsWith("level.dat", ignoreCase = true) }
                if (levelDatEntry == null) {
                    return null
                }
                val path = levelDatEntry.name
                val levelPath = path.substringBeforeLast("/")
                return levelPath
            }
        }
    }
}