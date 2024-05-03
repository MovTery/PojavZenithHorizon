package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.PojavZHTools.markdownToHtml;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

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
    private static boolean isChecked = false;
    private static NoticeInfo noticeInfo = null;

    public static NoticeInfo checkNewNotice() {
        if (isChecked) return noticeInfo; //如果已经检查过了，那么直接返回这个对象

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(PojavZHTools.URL_GITHUB_HOME + "notice.json")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
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
                        int numbering = noticeJson.getInt("numbering");
                        if (!(numbering > DEFAULT_PREF.getInt("ignoreNotice", 0))) {
                            return; //关闭过的通知将被忽略
                        }

                        //获取通知消息
                        String language = PojavZHTools.getDefaultLanguage();
                        String rawTitle;
                        String rawSubstance;
                        if (language.equals("zh_cn")) {
                            rawTitle = noticeJson.getString("title_zh_cn");
                            rawSubstance = noticeJson.getString("substance_zh_cn");
                        } else {
                            rawTitle = noticeJson.getString("title_zh_tw");
                            rawSubstance = noticeJson.getString("substance_zh_tw");
                        }
                        String rawDate = noticeJson.getString("date");
                        String substance = markdownToHtml(rawSubstance);

                        noticeInfo = new NoticeInfo(numbering, rawTitle, substance, rawDate);
                    } catch (Exception e) {
                        Log.e("Check New Notice", e.toString());
                    }
                }
            }
        });

        isChecked = true;
        return noticeInfo;
    }

    public static class NoticeInfo {
        private final int numbering;
        private final String rawTitle, substance, rawDate;

        public NoticeInfo(int numbering, String rawTitle, String substance, String rawDate) {
            this.numbering = numbering;
            this.rawTitle = rawTitle;
            this.substance = substance;
            this.rawDate = rawDate;
        }

        public int getNumbering() {
            return numbering;
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
    }
}
