package com.movtery.zalithlauncher.ui.fragment.settings.wrapper

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.movtery.zalithlauncher.setting.Settings
import net.kdt.pojavlaunch.R

@SuppressLint("UseSwitchCompatOrMaterialCode")
class ListSettingsWrapper(
    val context: Context,
    val key: String,
    val defaultValue: String,
    val mainView: View,
    val titleView: TextView,
    val valueView: TextView,
    val entries: Array<String>,
    val entryValues: Array<String>
) : AbstractSettingsWrapper(mainView) {

    constructor(
        context: Context,
        key: String,
        defaultValue: String,
        mainView: View,
        titleView: TextView,
        valueView: TextView,
        itemsId: Int,
        itemValuesId: Int
    ) : this(
        context, key, defaultValue, mainView, titleView, valueView,
        context.resources.getStringArray(itemsId),
        context.resources.getStringArray(itemValuesId)
    )

    init {
        updateListViewValue()
        mainView.setOnClickListener { createAListDialog() }
    }

    private fun createAListDialog() {
        val index = entryValues.indexOf(Settings.Manager.getString(key, defaultValue))
        AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle(titleView.text)
            .setSingleChoiceItems(entries, index) { dialog, which ->
                if (which != index) {
                    val selectedValue = entryValues[which]
                    Settings.Manager.put(key, selectedValue).save()
                    updateListViewValue()
                    checkShowRebootDialog(context)
                }
                dialog.dismiss()
            }
            .setPositiveButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateListViewValue() {
        val value = Settings.Manager.getString(key, defaultValue)
        val index = entryValues.indexOf(value).takeIf { it in entryValues.indices } ?: run {
            Settings.Manager.put(key, defaultValue).save()
            0
        }
        valueView.text = entries[index]
    }
}