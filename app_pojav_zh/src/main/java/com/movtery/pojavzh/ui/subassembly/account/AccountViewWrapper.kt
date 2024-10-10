package com.movtery.pojavzh.ui.subassembly.account

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.movtery.pojavzh.event.single.SelectAuthMethodEvent
import com.movtery.pojavzh.feature.accounts.AccountsManager
import com.movtery.pojavzh.ui.dialog.AccountsDialog
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.value.MinecraftAccount
import org.greenrobot.eventbus.EventBus
import java.io.File

class AccountViewWrapper(val mainView: View) {
    private val mContext: Context = mainView.context
    private val mUserIconView: ImageView = mainView.findViewById(R.id.user_icon)
    private val mUserNameView: TextView = mainView.findViewById(R.id.user_name)

    init {
        mainView.setOnClickListener {
            currentAccount ?: run {
                EventBus.getDefault().post(SelectAuthMethodEvent())
                return@setOnClickListener
            }
            AccountsDialog(mContext) { this.refreshAccountInfo() }.show()
        }
    }

    fun refreshAccountInfo() {
        val account = currentAccount
        account ?: run {
            mUserIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add))
            mUserNameView.setText(R.string.main_add_account)
            return
        }

        var drawable: Drawable? = null
        if (account.isMicrosoft) {
            val iconFile = File(PathAndUrlManager.DIR_USER_ICON, account.username + ".png")
            if (iconFile.exists()) {
                drawable = Drawable.createFromPath(iconFile.absolutePath)
            }
        }
        mUserIconView.setImageDrawable(
            drawable ?: ContextCompat.getDrawable(mContext, R.drawable.ic_head_steve)
        )
        mUserNameView.text = account.username
    }

    private val currentAccount: MinecraftAccount?
        get() = AccountsManager.getInstance().currentAccount
}
