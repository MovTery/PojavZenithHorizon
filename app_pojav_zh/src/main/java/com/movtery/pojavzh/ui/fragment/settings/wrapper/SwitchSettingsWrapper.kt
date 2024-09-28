package com.movtery.pojavzh.ui.fragment.settings.wrapper

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Switch
import com.movtery.pojavzh.setting.Settings

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SwitchSettingsWrapper(
    private val context: Context,
    private val key: String,
    val value: Boolean,
    val mainView: View,
    val switchView: Switch
) : AbstractSettingsWrapper(mainView) {
    init {
        switchView.isChecked = value

        switchView.setOnCheckedChangeListener { _, isChecked ->
            Settings.Manager.put(key, isChecked).save()
            checkShowRebootDialog(context)
        }
        mainView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }
}