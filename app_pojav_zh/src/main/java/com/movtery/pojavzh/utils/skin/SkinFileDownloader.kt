package com.movtery.pojavzh.utils.skin

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.utils.DownloadUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class SkinFileDownloader {
    companion object {
        private val GSON = Gson()
        private val client = OkHttpClient()

        @Throws(Exception::class)
        @JvmStatic
        fun microsoft(skinFile: File, uuid: String) {
            val profileJson = DownloadUtils.downloadString("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
            val profileObject = GSON.fromJson(profileJson, JsonObject::class.java)
            val properties = profileObject.get("properties").asJsonArray
            val rawValue = properties.get(0).asJsonObject.get("value").asString

            val value = StringUtils.decodeBase64(rawValue)

            val valueObject = GSON.fromJson(value, JsonObject::class.java)
            val skinUrl = valueObject.get("textures").asJsonObject.get("SKIN").asJsonObject.get("url").asString

            downloadSkin(skinUrl, skinFile)
        }

        private fun downloadSkin(url: String, skinFile: File) {
            skinFile.parentFile?.apply {
                if (!exists()) mkdirs()
            }

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("Unexpected code $response")
                }

                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(skinFile).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }
        }
    }
}