package com.movtery.pojavzh.utils.http;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CallUtils {
    private final CallbackListener listener;
    private final String url;
    private final String token;

    public CallUtils(CallbackListener listener, @NonNull String url, String token) {
        this.listener = listener;
        this.url = url;
        this.token = token;
    }

    public void start() {
        OkHttpClient client = new OkHttpClient();
        Request.Builder url = new Request.Builder().url(this.url);
        if (token != null) {
            url.addHeader("Token", token);
        }
        Request request = url.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onResponse(call, response);
            }
        });
    }

    public interface CallbackListener {
        void onFailure(Call call, IOException e);
        void onResponse(Call call, Response response) throws IOException;
    }
}
