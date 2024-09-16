package com.movtery.pojavzh.feature.login;

import android.content.Context;

import com.google.gson.Gson;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import okhttp3.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class OtherLoginApi {
    private static OkHttpClient client;
    private static final OtherLoginApi INSTANCE = new OtherLoginApi();
    private String baseUrl;

    private OtherLoginApi() {
        client = new OkHttpClient();
    }

    public static OtherLoginApi getINSTANCE() {
        return INSTANCE;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
        System.out.println(this.baseUrl);
    }

    public void login(Context context, String userName, String password, Listener listener) throws IOException {
        if (Objects.isNull(baseUrl)) {
            listener.onFailed(context.getString(R.string.zh_other_login_baseurl_not_set));
            return;
        }
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(userName);
        authRequest.setPassword(password);
        AuthRequest.Agent agent = new AuthRequest.Agent();
        agent.setName(context.getString(R.string.zh_other_login_client));
        agent.setVersion(1.0);
        authRequest.setAgent(agent);
        authRequest.setRequestUser(true);
        authRequest.setClientToken(UUID.randomUUID().toString().toLowerCase(Locale.ROOT));
        String data = new Gson().toJson(authRequest);
        System.out.println(data);
        login(data, "/authserver/authenticate", listener);
    }

    public void refresh(Context context, MinecraftAccount account, boolean select, Listener listener) throws IOException {
        if (Objects.isNull(baseUrl)) {
            listener.onFailed(context.getString(R.string.zh_other_login_baseurl_not_set));
            return;
        }
        Refresh refresh = new Refresh();
        refresh.setClientToken(account.clientToken);
        refresh.setAccessToken(account.accessToken);
        if (select) {
            Refresh.SelectedProfile selectedProfile = new Refresh.SelectedProfile();
            selectedProfile.setName(account.username);
            selectedProfile.setId(account.profileId);
            refresh.setSelectedProfile(selectedProfile);
        }
        String data = new Gson().toJson(refresh);
        System.out.println(data);
        login(data, "/authserver/refresh", listener);
    }

    private void login(String data, String url, Listener listener) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), data);
        Call call = client.newCall(PathAndUrlManager.createRequestBuilder(baseUrl + url, body).build());
        callLogin(call, listener);
    }

    private void callLogin(Call call, Listener listener) throws IOException {
        Response response = call.execute();
        String res = response.body().string();
        System.out.println(res);
        if (response.code() == 200) {
            AuthResult result = new Gson().fromJson(res, AuthResult.class);
            listener.onSuccess(result);
        } else {
            listener.onFailed("error code：" + response.code() + "\n" + res);
        }
    }

    public String getServeInfo(String url) {
        try {
            Call call = client.newCall(PathAndUrlManager.createRequestBuilder(url).get().build());
            Response response = call.execute();
            String res = response.body().string();
            System.out.println(res);
            if (response.code() == 200) {
                return res;
            }
        } catch (Exception e) {
            Logging.e("test", e.toString());
        }
        return null;
    }

    public interface Listener {
        void onSuccess(AuthResult authResult);

        void onFailed(String error);
    }

}