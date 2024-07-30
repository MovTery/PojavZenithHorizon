package com.movtery.pojavzh.utils.file

import java.io.InputStream
import java.security.MessageDigest

object FileUtils {
    fun getFileHashMD5(inputStream: InputStream): String? {
        try {
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                md.update(buffer, 0, bytesRead)
            }
            val hash = md.digest()

            //将哈希值转换为十六进制字符串
            val hexString = StringBuilder()
            for (b in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }

            return hexString.toString()
        } catch (e: Exception) {
            return null
        }
    }

    @JvmStatic
    fun getFileHashSHA1(inputStream: InputStream): String? {
        try {
            val md = MessageDigest.getInstance("SHA-1")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                md.update(buffer, 0, bytesRead)
            }
            val hash = md.digest()

            //将哈希值转换为十六进制字符串
            val hexString = StringBuilder()
            for (b in hash) {
                val hex = String.format("%02x", b)
                hexString.append(hex)
            }

            return hexString.toString()
        } catch (e: Exception) {
            return null
        }
    }
}
