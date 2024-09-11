package com.movtery.pojavzh.ui.fragment.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

abstract class AbstractSettingsFragment(layoutId: Int) : Fragment(layoutId),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onResume() {
        super.onResume()
        DEFAULT_PREF.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LauncherPreferences.loadPreferences(requireContext())
    }

    fun bindCategory(mainView: View) = SettingsCategoryItem(mainView)

    fun bindView(
        category: SettingsCategoryItem?,
        parentView: View,
        titleView: Int,
        summaryView: Int
    ): SettingsViewItem {
        val item =
            SettingsViewItem(null, parentView).setTitleView(parentView.findViewById(titleView))
                .setSummaryView(parentView.findViewById(summaryView))
        bindViewInCategory(category, item)
        return item
    }

    fun bindListView(
        category: SettingsCategoryItem?,
        key: String,
        parentView: View,
        titleView: Int,
        summaryView: Int?,
        valueView: Int
    ): SettingsViewItem {
        val item =
            SettingsViewItem(key, parentView).setTitleView(parentView.findViewById(titleView))
                .setValueView(parentView.findViewById(valueView))
        summaryView?.let { item.setSummaryView(parentView.findViewById(it)) }
        bindViewInCategory(category, item)
        return item
    }

    fun bindSeekBarView(
        category: SettingsCategoryItem?,
        key: String,
        value: Int,
        suffix: String,
        parentView: View,
        titleView: Int,
        summaryView: Int,
        seekBarView: Int,
        seekBarValueView: Int
    ): SettingsViewItem {
        val item =
            SettingsViewItem(key, parentView).setTitleView(parentView.findViewById(titleView))
                .setSummaryView(parentView.findViewById(summaryView))
                .setSeekBarView(parentView.findViewById(seekBarView))
                .setSeekBarValueView(parentView.findViewById(seekBarValueView))
                .setSeekBarValueSuffix(suffix)
        item.getSeekBarView().progress = value
        setSeekBarValueTextView(item.getSeekBarValueView(), item.getSeekBarView().progress, suffix)
        bindViewInCategory(category, item)
        return item
    }

    fun bindSwitchView(
        category: SettingsCategoryItem?,
        key: String,
        value: Boolean,
        parentView: View,
        titleView: Int,
        summaryView: Int,
        switch: Int
    ): SettingsViewItem {
        val item =
            SettingsViewItem(key, parentView).setTitleView(parentView.findViewById(titleView))
                .setSummaryView(parentView.findViewById(summaryView))
                .setSwitchView(parentView.findViewById(switch))
        item.getSwitchView().isChecked = value
        bindViewInCategory(category, item)
        return item
    }

    private fun bindViewInCategory(category: SettingsCategoryItem?, item: SettingsViewItem) {
        category?.addSubView(item)
    }

    @SuppressLint("StringFormatInvalid")
    fun initSeekBarView(item: SettingsViewItem) {
        val seekBarView = item.getSeekBarView()
        seekBarView.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                DEFAULT_PREF.edit().putInt(item.key, progress).apply()
                setSeekBarValueTextView(
                    item.getSeekBarValueView(),
                    progress,
                    item.getSeekBarValueSuffix()
                )
                checkShowRebootDialog(item)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.apply {
                    setSeekBarValueTextView(
                        item.getSeekBarValueView(),
                        progress,
                        item.getSeekBarValueSuffix()
                    )
                }
            }
        })
        item.mainView.setOnClickListener {
            val builder = EditTextDialog.Builder(context)
                .setEditText(seekBarView.progress.toString())
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setTitle(item.getTitleView().text.toString())
                .setMessage(item.getSummaryView().text.toString())
            builder.setConfirmListener { editBox: EditText ->
                val string = editBox.text.toString()
                val requireContext = requireContext()

                if (string.isEmpty()) {
                    editBox.error = requireContext.getString(R.string.global_error_field_empty)
                    return@setConfirmListener false
                }

                val value: Int
                try {
                    value = string.toInt()
                } catch (e: NumberFormatException) {
                    e("Custom Seek Bar", e.toString())

                    editBox.error = requireContext.getString(R.string.zh_input_invalid)
                    return@setConfirmListener false
                }

                if (value < seekBarView.min) {
                    val minValue =
                        String.format("%s %s", seekBarView.min, item.getSeekBarValueSuffix())
                    editBox.error = requireContext.getString(R.string.zh_input_too_small, minValue)
                    return@setConfirmListener false
                }
                if (value > seekBarView.max) {
                    val maxValue =
                        String.format("%s %s", seekBarView.max, item.getSeekBarValueSuffix())
                    editBox.error = requireContext.getString(R.string.zh_input_too_big, maxValue)
                    return@setConfirmListener false
                }

                seekBarView.progress = value
                true
            }.buildDialog()
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun initSwitchView(item: SettingsViewItem) {
        val switchView = item.getSwitchView()
        switchView.setOnCheckedChangeListener { _, b ->
            DEFAULT_PREF.edit().putBoolean(item.key, b).apply()
            checkShowRebootDialog(item)
        }
        item.mainView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }

    fun initListView(
        item: SettingsViewItem,
        defaultValue: String,
        itemsId: Int,
        itemValuesId: Int
    ) {
        val entries = requireContext().resources.getStringArray(itemsId)
        val entryValues = requireContext().resources.getStringArray(itemValuesId)
        initListView(item, defaultValue, entries, entryValues)
    }

    fun initListView(
        item: SettingsViewItem,
        defaultValue: String,
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        updateListViewValue(item, defaultValue, entries, entryValues)
        item.mainView.setOnClickListener {
            createAListDialog(item, defaultValue, entries, entryValues)
        }
    }

    fun setSeekBarValueTextView(textView: TextView, progress: Int, suffix: String) {
        val text = "$progress $suffix"
        textView.text = text
    }

    private fun createAListDialog(
        item: SettingsViewItem,
        defaultValue: String,
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        val index = entryValues.indexOf(DEFAULT_PREF.getString(item.key, defaultValue))
        var selectedItemIndex = index

        AlertDialog.Builder(requireContext())
            .setTitle(item.getTitleView().text)
            .setSingleChoiceItems(entries, selectedItemIndex) { _, which ->
                selectedItemIndex = which
            }
            .setPositiveButton(R.string.zh_confirm) { _, _ ->
                if (selectedItemIndex != index) {
                    val selectedValue = entryValues[selectedItemIndex]
                    DEFAULT_PREF.edit().putString(item.key, selectedValue).apply()
                    updateListViewValue(item, defaultValue, entries, entryValues)
                    checkShowRebootDialog(item)
                }
            }
            .show()
    }

    private fun updateListViewValue(
        item: SettingsViewItem,
        defaultValue: String,
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        val value = DEFAULT_PREF.getString(item.key, defaultValue)
        item.getValueView().text = entries[entryValues.indexOf(value)]
    }

    private fun checkShowRebootDialog(item: SettingsViewItem) {
        if (item.isRequiresReboot()) {
            TipDialog.Builder(requireContext())
                .setMessage(R.string.zh_setting_reboot_tip)
                .setConfirmClickListener { ZHTools.killApp() }
                .buildDialog()
        }
    }
}