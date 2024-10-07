package com.movtery.pojavzh.ui.fragment.settings.wrapper

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.CompoundButton
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
    private var listener: OnCheckedChangeListener? = null

    init {
        switchView.isChecked = value

        switchView.setOnCheckedChangeListener { buttonView, isChecked ->
            val switchChangeListener = object : OnSwitchSaveListener {
                override fun onSave() {
                    Settings.Manager.put(key, isChecked).save()
                    checkShowRebootDialog(context)
                }
            }
            listener?.onChange(buttonView, isChecked, switchChangeListener) ?: switchChangeListener.onSave()
        }
        mainView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        this.listener = listener
    }

    fun interface OnSwitchSaveListener {
        fun onSave()
    }

    fun interface OnCheckedChangeListener {
        fun onChange(buttonView: CompoundButton, isChecked: Boolean, listener: OnSwitchSaveListener)
    }
}