package com.movtery.pojavzh.feature

import android.content.Context
import android.util.Base64
import android.util.Log
import com.movtery.pojavzh.ui.subassembly.about.SponsorItemBean
import com.movtery.pojavzh.ui.subassembly.about.SponsorMeta
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.http.CallUtils
import com.movtery.pojavzh.utils.http.CallUtils.CallbackListener
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects

object CheckSponsor {
    private var sponsorData: ArrayList<SponsorItemBean>? = null
    private var isChecking = false

    @JvmStatic
    fun getSponsorData(): List<SponsorItemBean>? {
        return sponsorData
    }

    @JvmStatic
    fun check(context: Context, listener: CheckListener) {
        if (isChecking) {
            listener.onFailure()
            return
        }
        isChecking = true

        sponsorData?.let {
            listener.onSuccessful(sponsorData)
            isChecking = false
            return
        }

        val token = context.getString(R.string.zh_api_token)
        CallUtils(object : CallbackListener {
            override fun onFailure(call: Call?, e: IOException?) {
                listener.onFailure()
                isChecking = false
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response?) {
                if (!response!!.isSuccessful) {
                    throw IOException("Unexpected code $response")
                } else {
                    try {
                        Objects.requireNonNull(response.body())
                        val responseBody = response.body()!!.string()

                        val originJson = JSONObject(responseBody)
                        val rawBase64 = originJson.getString("content")
                        //base64解码，因为这里读取的是一个经过Base64加密后的文本
                        val decodedBytes = Base64.decode(rawBase64, Base64.DEFAULT)
                        val rawJson = String(decodedBytes, StandardCharsets.UTF_8)

                        val sponsorMeta =
                            Tools.GLOBAL_GSON.fromJson(rawJson, SponsorMeta::class.java)
                        if (sponsorMeta.sponsors.isEmpty()) {
                            listener.onFailure()
                            return
                        }
                        sponsorData = ArrayList()
                        for (sponsor in sponsorMeta.sponsors) {
                            sponsorData?.add(
                                SponsorItemBean(
                                    sponsor.name,
                                    sponsor.time,
                                    sponsor.amount
                                )
                            )
                        }
                        listener.onSuccessful(sponsorData)
                    } catch (e: Exception) {
                        Log.e("Load Sponsor Data", e.toString())
                        listener.onFailure()
                    }
                }
                isChecking = false
            }
        }, ZHTools.URL_GITHUB_HOME + "sponsor.json", if (token == "DUMMY") null else token).start()
    }

    interface CheckListener {
        fun onFailure()

        fun onSuccessful(data: List<SponsorItemBean>?)
    }
}
