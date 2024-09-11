package com.movtery.pojavzh.ui.fragment.settings

import android.annotation.SuppressLint
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SettingsViewItem(val key: String?, val mainView: View) {
    companion object {
        const val NULL_POINTER_MESSAGE: String =
            "Attempt to access an object that is either uninitialized or does not exist."
    }

    private var requiresReboot: Boolean = false
    private var titleView: TextView? = null
    private var summaryView: TextView? = null
    private var valueView: TextView? = null
    private var seekBarView: SeekBar? = null
    private var seekBarValueView: TextView? = null
    private var seekBarValueSuffix: String? = null
    private var switchView: Switch? = null

    fun isRequiresReboot() = requiresReboot
    fun getTitleView(): TextView = titleView ?: throw NullPointerException(NULL_POINTER_MESSAGE)
    fun getSummaryView(): TextView = summaryView ?: throw NullPointerException(NULL_POINTER_MESSAGE)
    fun getValueView(): TextView = valueView ?: throw NullPointerException(NULL_POINTER_MESSAGE)
    fun getSeekBarView(): SeekBar = seekBarView ?: throw NullPointerException(NULL_POINTER_MESSAGE)
    fun getSeekBarValueView(): TextView =
        seekBarValueView ?: throw NullPointerException(NULL_POINTER_MESSAGE)

    fun getSeekBarValueSuffix(): String =
        seekBarValueSuffix ?: throw NullPointerException(NULL_POINTER_MESSAGE)

    fun getSwitchView(): Switch = switchView ?: throw NullPointerException(NULL_POINTER_MESSAGE)

    fun setRequiresReboot(require: Boolean): SettingsViewItem {
        requiresReboot = require
        return this
    }

    fun setTitleView(textView: TextView): SettingsViewItem {
        titleView = textView
        return this
    }

    fun setSummaryView(textView: TextView): SettingsViewItem {
        summaryView = textView
        return this
    }

    fun setValueView(textView: TextView): SettingsViewItem {
        valueView = textView
        return this
    }

    fun setSeekBarView(seekBar: SeekBar): SettingsViewItem {
        seekBarView = seekBar
        return this
    }

    fun setSeekBarValueView(textView: TextView): SettingsViewItem {
        seekBarValueView = textView
        return this
    }

    fun setSeekBarValueSuffix(suffix: String): SettingsViewItem {
        seekBarValueSuffix = suffix
        return this
    }

    fun setSwitchView(switch: Switch): SettingsViewItem {
        switchView = switch
        return this
    }
}