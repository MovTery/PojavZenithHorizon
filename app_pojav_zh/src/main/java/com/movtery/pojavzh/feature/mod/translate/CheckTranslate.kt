package com.movtery.pojavzh.feature.mod.translate

import android.content.Context
import android.util.Base64
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.http.CallUtils
import com.movtery.pojavzh.utils.http.CallUtils.CallbackListener
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.utils.FileUtils
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects

class CheckTranslate {
    companion object {
        private val modInfoFile: File = File(PathAndUrlManager.DIR_CACHE_STRING, "/${TranslateClassify.MOD.fileName}")
        private val modPackInfoFile: File = File(PathAndUrlManager.DIR_CACHE_STRING, "/${TranslateClassify.MODPACK.fileName}")
        private var isChecking = false

        @JvmStatic
        fun check(context: Context, classify: TranslateClassify, listener: CheckListener) {
            val infoFile = if (classify == TranslateClassify.MOD) modInfoFile else modPackInfoFile

            if (isChecking) {
                return
            }
            isChecking = true

            if (infoFile.exists()) {
                if (ZHTools.getCurrentTimeMillis() - infoFile.lastModified() < 8640_0000) {
                    isChecking = false
                    listener.onEnd(infoFile)
                    return
                }
            }

            val token = context.getString(R.string.private_api_token)
            CallUtils(object : CallbackListener {
                override fun onFailure(call: Call?) {
                    isChecking = false
                    listener.onEnd(null)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response?) {
                    if (!response!!.isSuccessful) {
                        Logging.e("CheckModTranslate", "Unexpected code ${response.code}")
                        listener.onEnd(null)
                    } else {
                        Logging.i("CheckModTranslate", "The data was updated successfully : ${classify.name}")
                        runCatching {
                            Objects.requireNonNull(response.body)
                            val responseBody = response.body!!.string()

                            val originText = JSONObject(responseBody)
                            val rawBase64 = originText.getString("content")
                            //base64解码，因为这里读取的是一个经过Base64加密后的文本
                            val decodedBytes = Base64.decode(rawBase64, Base64.DEFAULT)
                            val infoText = String(decodedBytes, StandardCharsets.UTF_8)

                            if (!infoFile.exists()) {
                                FileUtils.ensureParentDirectory(infoFile)
                                infoFile.createNewFile()
                            }
                            FileWriter(infoFile).use { fw -> fw.write(infoText) }

                            listener.onEnd(infoFile)
                        }.getOrElse { e ->
                            Logging.e("CheckModTranslate", Tools.printToString(e))
                            listener.onEnd(null)
                        }
                    }
                    isChecking = false
                }
            }, PathAndUrlManager.URL_GITHUB_HOME + classify.fileName, if (token == "DUMMY") null else token).execute()
        }
    }

    interface CheckListener {
        fun onEnd(infoFile: File?)
    }
}
