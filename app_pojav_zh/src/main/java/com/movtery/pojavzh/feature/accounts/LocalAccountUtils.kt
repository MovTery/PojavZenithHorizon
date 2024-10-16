package com.movtery.pojavzh.feature.accounts

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import com.movtery.pojavzh.setting.Settings
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

class LocalAccountUtils {
    companion object {
        @JvmStatic
        fun checkUsageAllowed(listener: CheckResultListener) {
            if (AccountsManager.getInstance().haveMicrosoftAccount()) {
                listener.onUsageAllowed()
            } else {
                listener.onUsageDenied()
            }
        }

        @JvmStatic
        @SuppressLint("InflateParams")
        fun openDialog(
            activity: Activity,
            confirmClickListener: TipDialog.OnConfirmClickListener?,
            message: String?,
            confirm: Int
        ) {
            //不再提醒
            val checkBox = CheckBox(activity)
            checkBox.setText(R.string.generic_no_more_reminders)
            checkBox.setOnCheckedChangeListener { _: Button?, isChecked: Boolean ->
                Settings.Manager
                    .put("localAccountReminders", !isChecked)
                    .save()
            }

            TipDialog.Builder(activity)
                .setTitle(R.string.generic_warning)
                .setMessage(message)
                .addView(checkBox)
                .setConfirmClickListener(confirmClickListener)
                .setConfirm(confirm)
                .setCancelClickListener { Tools.openURL(activity, PathAndUrlManager.URL_MINECRAFT) }
                .setCancel(R.string.account_purchase_minecraft_account)
                .buildDialog()
        }
    }

    interface CheckResultListener {
        fun onUsageAllowed()
        fun onUsageDenied()
    }
}
