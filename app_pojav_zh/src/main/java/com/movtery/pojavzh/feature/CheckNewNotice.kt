package com.movtery.pojavzh.feature

import android.content.Context
import android.util.Base64
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.http.CallUtils
import com.movtery.pojavzh.utils.http.CallUtils.CallbackListener
import net.kdt.pojavlaunch.R
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects

class CheckNewNotice {
    companion object {
        @JvmStatic
        var noticeInfo: NoticeInfo? = null
        private var isChecking = false

        @JvmStatic
        fun checkNewNotice(context: Context, listener: CheckListener) {
            if (isChecking) {
                return
            }
            isChecking = true

            noticeInfo?.let {
                listener.onSuccessful(noticeInfo)
                isChecking = false
                return
            }

            val token = context.getString(R.string.zh_private_api_token)
            CallUtils(object : CallbackListener {
                override fun onFailure(call: Call?, e: IOException?) {
                    isChecking = false
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response?) {
                    if (!response!!.isSuccessful) {
                        Logging.e("CheckNewNotice", "Unexpected code ${response.code()}")
                    } else {
                        runCatching {
                            Objects.requireNonNull(response.body())
                            val responseBody = response.body()!!.string()

                            val originJson = JSONObject(responseBody)
                            val rawBase64 = originJson.getString("content")
                            //base64解码，因为这里读取的是一个经过Base64加密后的文本
                            val decodedBytes = Base64.decode(rawBase64, Base64.DEFAULT)
                            val rawJson = String(decodedBytes, StandardCharsets.UTF_8)

                            val noticeJson = JSONObject(rawJson)

                            //获取通知消息
                            val language = ZHTools.getSystemLanguage()
                            val rawTitle: String
                            val rawSubstance: String
                            when (language) {
                                "zh_cn" -> {
                                    rawTitle = noticeJson.getString("title_zh_cn")
                                    rawSubstance = noticeJson.getString("substance_zh_cn")
                                }

                                "zh_tw" -> {
                                    rawTitle = noticeJson.getString("title_zh_tw")
                                    rawSubstance = noticeJson.getString("substance_zh_tw")
                                }

                                else -> {
                                    rawTitle = noticeJson.getString("title_en_us")
                                    rawSubstance = noticeJson.getString("substance_en_us")
                                }
                            }
                            val rawDate = noticeJson.getString("date")
                            val numbering = noticeJson.getInt("numbering")

                            noticeInfo = NoticeInfo(rawTitle, rawSubstance, rawDate, numbering)
                            listener.onSuccessful(noticeInfo)
                        }.getOrElse { e ->
                            Logging.e("Check New Notice", e.toString())
                        }
                    }
                    isChecking = false
                }
            }, PathAndUrlManager.URL_GITHUB_HOME + "notice.json", if (token == "DUMMY") null else token).start()
        }
    }

    interface CheckListener {
        fun onSuccessful(noticeInfo: NoticeInfo?)
    }

    class NoticeInfo(
        @JvmField val rawTitle: String,
        @JvmField val substance: String,
        @JvmField val rawDate: String,
        @JvmField val numbering: Int
    )
}
