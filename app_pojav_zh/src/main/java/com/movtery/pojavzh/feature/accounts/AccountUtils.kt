package com.movtery.pojavzh.feature.accounts;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.login.AuthResult;
import com.movtery.pojavzh.feature.login.OtherLoginApi;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.IOException;
import java.util.Objects;

public class AccountUtils {
    public static void microsoftLogin(@NonNull MinecraftAccount account) {
        AccountsManager accountsManager = AccountsManager.getInstance();

        // Perform login only if needed
        new MicrosoftBackgroundLogin(true, account.msaRefreshToken)
                .performLogin(accountsManager.getProgressListener(), accountsManager.getDoneListener(), accountsManager.getErrorListener());
    }

    public static void otherLogin(@NonNull Context context, @NonNull MinecraftAccount account) {
        ErrorListener errorListener = AccountsManager.getInstance().getErrorListener();

        OtherLoginApi.getINSTANCE().setBaseUrl(account.baseUrl);
        PojavApplication.sExecutorService.execute(() -> {
            try {
                OtherLoginApi.getINSTANCE().login(context, account.account, account.password, new OtherLoginApi.Listener() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        account.expiresAt = ZHTools.getCurrentTimeMillis() + 30 * 60 * 1000;
                        account.accessToken = authResult.getAccessToken();
                        runOnUiThread(() -> ExtraCore.setValue(ZHExtraConstants.OTHER_LOGIN_TODO, account));
                    }

                    @Override
                    public void onFailed(String error) {
                        errorListener.onLoginError(new Throwable(error));
                    }
                });
            } catch (IOException e) {
                errorListener.onLoginError(e);
            }
        });
    }

    public static boolean isOtherLoginAccount(MinecraftAccount account) {
        return !Objects.isNull(account.baseUrl) && !account.baseUrl.equals("0");
    }

    public static boolean isNoLoginRequired(MinecraftAccount account) {
        return account == null || account.isLocal();
    }
}
