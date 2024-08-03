package com.movtery.pojavzh.ui.subassembly.account

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.movtery.pojavzh.feature.accounts.AccountsManager
import com.movtery.pojavzh.ui.dialog.AccountsDialog
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.value.MinecraftAccount
import java.io.File

class AccountView(val mainView: View) {
    private val mContext: Context = mainView.context
    private val mUserIconView: ImageView = mainView.findViewById(R.id.user_icon)
    private val mUserNameView: TextView = mainView.findViewById(R.id.user_name)

    init {
        mainView.setOnClickListener {
            setViewAnim(mainView, Techniques.Bounce)
            if (currentAccount == null) {
                ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true)
            } else {
                AccountsDialog(mContext) { this.refreshAccountInfo() }.show()
            }
        }
    }

    fun refreshAccountInfo() {
        val account = currentAccount
        if (account == null) {
            mUserIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add))
            mUserNameView.setText(R.string.main_add_account)
            return
        }

        var drawable: Drawable? = null
        if (account.isMicrosoft) {
            val iconFile = File(ZHTools.DIR_USER_ICON, account.username + ".png")
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
