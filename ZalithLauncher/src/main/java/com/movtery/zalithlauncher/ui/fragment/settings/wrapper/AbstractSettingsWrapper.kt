package com.movtery.zalithlauncher.ui.fragment.settings.wrapper

import android.content.Context
import android.view.View
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import com.movtery.zalithlauncher.utils.ZHTools
import net.kdt.pojavlaunch.R

abstract class AbstractSettingsWrapper(
    private val mainView: View
) {
    private var isRequiresReboot = false

    fun setRequiresReboot(): AbstractSettingsWrapper {
        isRequiresReboot = true
        return this
    }

    fun checkShowRebootDialog(context: Context) {
        if (isRequiresReboot) {
            TipDialog.Builder(context)
                .setMessage(R.string.setting_reboot_tip)
                .setConfirmClickListener { ZHTools.killProcess() }
                .buildDialog()
        }
    }

    fun setGone() {
        mainView.visibility = View.GONE
    }
}