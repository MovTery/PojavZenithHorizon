package com.movtery.pojavzh.feature;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.movtery.pojavzh.ui.subassembly.about.SponsorItemBean;
import com.movtery.pojavzh.ui.subassembly.about.SponsorMeta;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.http.CallUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class CheckSponsor {
    private static List<SponsorItemBean> sponsorData = null;
    private static boolean isChecking = false;

    public static List<SponsorItemBean> getSponsorData() {
        return sponsorData;
    }

    public static void check(Context context, CheckListener listener) {
        if (isChecking) {
            listener.onFailure();
            return;
        }
        isChecking = true;

        if (sponsorData != null) {
            listener.onSuccessful(sponsorData);
            isChecking = false;
            return;
        }

        sponsorData = new ArrayList<>();

        String token = context.getString(R.string.zh_api_token);
        new CallUtils(new CallUtils.CallbackListener() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure();
                isChecking = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    try {
                        Objects.requireNonNull(response.body());
                        String responseBody = response.body().string();

                        JSONObject originJson = new JSONObject(responseBody);
                        String rawBase64 = originJson.getString("content");
                        //base64解码，因为这里读取的是一个经过Base64加密后的文本
                        byte[] decodedBytes = Base64.decode(rawBase64, Base64.DEFAULT);
                        String rawJson = new String(decodedBytes, StandardCharsets.UTF_8);

                        SponsorMeta sponsorMeta = Tools.GLOBAL_GSON.fromJson(rawJson, SponsorMeta.class);
                        if (sponsorMeta.sponsors.length == 0) {
                            listener.onFailure();
                            return;
                        }
                        for (SponsorMeta.Sponsor sponsor : sponsorMeta.sponsors) {
                            sponsorData.add(new SponsorItemBean(sponsor.name, sponsor.time, sponsor.amount));
                        }
                        listener.onSuccessful(sponsorData);
                    } catch (Exception e) {
                        Log.e("Load Sponsor Data", e.toString());
                        listener.onFailure();
                    }
                }
                isChecking = false;
            }
        }, ZHTools.URL_GITHUB_HOME + "sponsor.json", token.equals("DUMMY") ? null : token).start();
    }

    public interface CheckListener {
        void onFailure();

        void onSuccessful(List<SponsorItemBean> data);
    }
}
