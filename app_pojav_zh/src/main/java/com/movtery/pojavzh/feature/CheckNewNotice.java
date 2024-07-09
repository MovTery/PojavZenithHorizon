package com.movtery.pojavzh.feature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckNewNotice {
    private static NoticeInfo noticeInfo = null;
    private static boolean isChecking = false;

    public static NoticeInfo getNoticeInfo() {
        return noticeInfo;
    }

    public static void checkNewNotice(Context context, CheckListener listener) {
        if (isChecking) {
            return;
        }
        isChecking = true;

        if (noticeInfo != null) {
            listener.onSuccessful(noticeInfo);
            isChecking = false;
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request.Builder url = new Request.Builder()
                .url(ZHTools.URL_GITHUB_HOME + "notice.json");
        if (!context.getString(R.string.zh_api_token).equals("DUMMY")) {
            url.header("Authorization", "token " + context.getString(R.string.zh_api_token));
        }
        Request request = url.build();
        System.out.println("checking");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isChecking = false;
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    Objects.requireNonNull(response.body());
                    String responseBody = response.body().string();
                    try {
                        JSONObject originJson = new JSONObject(responseBody);
                        String rawBase64 = originJson.getString("content");
                        //base64解码，因为这里读取的是一个经过Base64加密后的文本
                        byte[] decodedBytes = Base64.decode(rawBase64, Base64.DEFAULT);
                        String rawJson = new String(decodedBytes, StandardCharsets.UTF_8);

                        JSONObject noticeJson = new JSONObject(rawJson);

                        //获取通知消息
                        String language = ZHTools.getSystemLanguage();
                        String rawTitle;
                        String rawSubstance;
                        switch (language) {
                            case "zh_cn":
                                rawTitle = noticeJson.getString("title_zh_cn");
                                rawSubstance = noticeJson.getString("substance_zh_cn");
                                break;
                            case "zh_tw":
                                rawTitle = noticeJson.getString("title_zh_tw");
                                rawSubstance = noticeJson.getString("substance_zh_tw");
                                break;
                            default:
                                rawTitle = noticeJson.getString("title_en_us");
                                rawSubstance = noticeJson.getString("substance_en_us");
                        }
                        String rawDate = noticeJson.getString("date");
                        int numbering = noticeJson.getInt("numbering");

                        noticeInfo = new NoticeInfo(rawTitle, rawSubstance, rawDate, numbering);
                        listener.onSuccessful(noticeInfo);
                    } catch (Exception e) {
                        Log.e("Check New Notice", e.toString());
                    }
                }
                isChecking = false;
            }
        });
    }

    public interface CheckListener {
        void onSuccessful(NoticeInfo noticeInfo);
    }

    public static class NoticeInfo {
        private final String rawTitle, substance, rawDate;
        private final int numbering;

        public NoticeInfo(String rawTitle, String substance, String rawDate, int numbering) {
            this.rawTitle = rawTitle;
            this.substance = substance;
            this.rawDate = rawDate;
            this.numbering = numbering;
        }

        public String getRawTitle() {
            return rawTitle;
        }

        public String getSubstance() {
            return substance;
        }

        public String getRawDate() {
            return rawDate;
        }

        public int getNumbering() {
            return numbering;
        }
    }
}
