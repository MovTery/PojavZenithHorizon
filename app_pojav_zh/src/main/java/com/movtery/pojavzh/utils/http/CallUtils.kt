package com.movtery.pojavzh.utils.http

import com.movtery.pojavzh.utils.PathAndUrlManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class CallUtils(
    private val listener: CallbackListener,
    private val url: String,
    private val token: String?
) {
    fun start() {
        val tokenInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val requestWithToken = originalRequest.newBuilder()
            token?.let { requestWithToken.header("Authorization", "token $token") }

            chain.proceed(requestWithToken.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .build()

        client.newCall(PathAndUrlManager.createRequestBuilder(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                listener.onResponse(call, response)
            }
        })
    }

    interface CallbackListener {
        fun onFailure(call: Call?, e: IOException?)

        @Throws(IOException::class)
        fun onResponse(call: Call?, response: Response?)
    }
}
