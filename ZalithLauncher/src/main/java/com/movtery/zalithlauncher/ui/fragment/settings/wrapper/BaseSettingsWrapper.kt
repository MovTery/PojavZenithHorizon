package com.movtery.zalithlauncher.ui.fragment.settings.wrapper

import android.annotation.SuppressLint
import android.content.Context
import android.view.View

@SuppressLint("UseSwitchCompatOrMaterialCode")
class BaseSettingsWrapper(
    val context: Context,
    val mainView: View,
    listener: OnViewClickListener
) : AbstractSettingsWrapper(mainView) {

    init {
        mainView.setOnClickListener {
            listener.onClick()
        }
    }
}