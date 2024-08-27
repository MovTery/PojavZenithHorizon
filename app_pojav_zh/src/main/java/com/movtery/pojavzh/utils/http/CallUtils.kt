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
    url: String,
    private val token: String?
) {
    private val tokenInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.newBuilder()
        token?.let { requestWithToken.header("Authorization", "token $token") }

        chain.proceed(requestWithToken.build())
    }

    val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)
        .build()

    private val newCall: Call = client.newCall(PathAndUrlManager.createRequestBuilder(url).build())

    fun enqueue() {
        newCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure(call)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                listener.onResponse(call, response)
            }
        })
    }

    fun execute() {
        try {
            val response = newCall.execute()

            if (response.isSuccessful) {
                listener.onResponse(newCall, response)
            } else {
                listener.onFailure(newCall)
            }
        } catch (e: IOException) {
            listener.onFailure(newCall)
        }
    }

    interface CallbackListener {
        fun onFailure(call: Call?)

        @Throws(IOException::class)
        fun onResponse(call: Call?, response: Response?)
    }
}
