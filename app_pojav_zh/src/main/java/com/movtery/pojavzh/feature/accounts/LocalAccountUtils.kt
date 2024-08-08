package com.movtery.pojavzh.feature.accounts

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Button
import android.widget.CheckBox
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

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
            val inflater = activity.layoutInflater
            val edit = LauncherPreferences.DEFAULT_PREF.edit()
            //不再提醒
            val checkBox = inflater.inflate(R.layout.item_check_box, null) as CheckBox
            checkBox.setText(R.string.zh_no_more_reminders)
            checkBox.setOnCheckedChangeListener { _: Button?, isChecked: Boolean ->
                edit.putBoolean(
                    "localAccountReminders",
                    !isChecked
                ).apply()
            }

            TipDialog.Builder(activity)
                .setTitle(R.string.zh_warning)
                .setMessage(message)
                .addView(checkBox)
                .setConfirmClickListener(confirmClickListener)
                .setConfirm(confirm)
                .setCancelClickListener { Tools.openURL(activity, PathAndUrlManager.URL_MINECRAFT) }
                .setCancel(R.string.zh_account_purchase_minecraft_account)
                .buildDialog()
        }
    }

    interface CheckResultListener {
        fun onUsageAllowed()
        fun onUsageDenied()
    }
}
