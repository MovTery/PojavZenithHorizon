package com.movtery.pojavzh.feature.accounts

import android.content.Context
import com.movtery.pojavzh.event.value.OtherLoginEvent
import com.movtery.pojavzh.feature.login.AuthResult
import com.movtery.pojavzh.feature.login.OtherLoginApi
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin
import net.kdt.pojavlaunch.value.MinecraftAccount
import org.greenrobot.eventbus.EventBus
import java.util.Objects

class AccountUtils {
    companion object {
        @JvmStatic
        fun microsoftLogin(account: MinecraftAccount) {
            val accountsManager = AccountsManager.getInstance()

            // Perform login only if needed
            MicrosoftBackgroundLogin(true, account.msaRefreshToken)
                .performLogin(
                    accountsManager.progressListener,
                    accountsManager.doneListener,
                    accountsManager.errorListener
                )
        }

        @JvmStatic
        fun otherLogin(context: Context, account: MinecraftAccount) {
            val errorListener = AccountsManager.getInstance().errorListener

            OtherLoginApi.setBaseUrl(account.baseUrl)
            PojavApplication.sExecutorService.execute {
                runCatching {
                    OtherLoginApi.refresh(context, account, false, object : OtherLoginApi.Listener {
                        override fun onSuccess(authResult: AuthResult) {
                            account.accessToken = authResult.accessToken
                            EventBus.getDefault().post(OtherLoginEvent(account))
                        }

                        override fun onFailed(error: String) {
                            errorListener.onLoginError(Throwable(error))
                        }
                    })
                }.getOrElse { e -> errorListener.onLoginError(e) }
            }
        }

        @JvmStatic
        fun isOtherLoginAccount(account: MinecraftAccount): Boolean {
            return !Objects.isNull(account.baseUrl) && account.baseUrl != "0"
        }

        @JvmStatic
        fun isNoLoginRequired(account: MinecraftAccount?): Boolean {
            return account == null || account.isLocal
        }
    }
}
