package com.movtery.pojavzh.ui.fragment.settings.view

import android.content.Context
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R

class Utils {
    companion object {
        fun checkShowRebootDialog(context: Context) {
            TipDialog.Builder(context)
                .setMessage(R.string.zh_setting_reboot_tip)
                .setConfirmClickListener { ZHTools.killApp() }
                .buildDialog()
        }
    }
}