package com.movtery.pojavzh.ui.subassembly.settingsbutton

import android.widget.ImageButton
import androidx.core.content.ContextCompat
import net.kdt.pojavlaunch.R

class SettingsButtonWrapper(val button: ImageButton) {
    private var onTypeChangeListener: OnTypeChangeListener? = null
    private var buttonType: ButtonType? = ButtonType.SETTINGS

    fun setButtonType(type: ButtonType) {
        if (buttonType != type) {
            buttonType = type
            button.setImageDrawable(
                ContextCompat.getDrawable(button.context,
                    if (type == ButtonType.SETTINGS) R.drawable.ic_menu_settings else R.drawable.ic_menu_home
                )
            )
            onTypeChangeListener?.onChange(type)
        }
    }

    fun setOnTypeChangeListener(listener: OnTypeChangeListener) { onTypeChangeListener = listener }
}

enum class ButtonType {
    SETTINGS, HOME
}