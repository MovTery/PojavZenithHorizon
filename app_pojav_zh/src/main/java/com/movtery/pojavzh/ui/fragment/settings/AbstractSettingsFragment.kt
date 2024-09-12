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
import androidx.core.content.ContextCompat
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

    /**
     * 绑定类别标签
     * @param mainView 视图的根部视图
     */
    fun bindCategory(mainView: View) = SettingsCategoryWrapper(mainView)

    /**
     * 绑定为最基础的视图
     * @param category 绑定类别标签
     * @param mainView 视图的根部视图
     * @param titleView 标题视图
     * @param summaryView 描述视图
     */
    fun bindView(
        category: SettingsCategoryWrapper?,
        mainView: View,
        titleView: Int,
        summaryView: Int
    ): SettingsViewWrapper {
        val item =
            SettingsViewWrapper(null, mainView).setTitleView(mainView.findViewById(titleView))
                .setSummaryView(mainView.findViewById(summaryView))
        bindViewInCategory(category, item)
        return item
    }

    /**
     * 绑定为列表视图，点击可展示列表 Dialog
     * @param category 绑定类别标签
     * @param key 视图与所指的 Preference 的 Key
     * @param mainView 视图的根部视图
     * @param titleView 标题视图
     * @param summaryView 描述视图
     * @param valueView 展示选中值的视图
     */
    fun bindListView(
        category: SettingsCategoryWrapper?,
        key: String,
        mainView: View,
        titleView: Int,
        summaryView: Int?,
        valueView: Int
    ): SettingsViewWrapper {
        val item =
            SettingsViewWrapper(key, mainView).setTitleView(mainView.findViewById(titleView))
                .setValueView(mainView.findViewById(valueView))
        summaryView?.let { item.setSummaryView(mainView.findViewById(it)) }
        bindViewInCategory(category, item)
        return item
    }

    /**
     * 绑定为滑动条视图
     * @param category 绑定类别标签
     * @param key 视图与所指的 Preference 的 Key
     * @param value 设置的值
     * @param suffix 设置值的单位后缀
     * @param mainView 视图的根部视图
     * @param titleView 标题视图
     * @param summaryView 描述视图
     * @param seekBarView 滑动条视图
     * @param seekBarValueView 滑动条值展示的视图
     */
    fun bindSeekBarView(
        category: SettingsCategoryWrapper?,
        key: String,
        value: Int,
        suffix: String,
        mainView: View,
        titleView: Int,
        summaryView: Int,
        seekBarView: Int,
        seekBarValueView: Int
    ): SettingsViewWrapper {
        val item =
            SettingsViewWrapper(key, mainView).setTitleView(mainView.findViewById(titleView))
                .setSummaryView(mainView.findViewById(summaryView))
                .setSeekBarView(mainView.findViewById(seekBarView))
                .setSeekBarValueView(mainView.findViewById(seekBarValueView))
                .setSeekBarValueSuffix(suffix)
        item.getSeekBarView().progress = value
        setSeekBarValueTextView(item.getSeekBarValueView(), item.getSeekBarView().progress, suffix)
        bindViewInCategory(category, item)
        return item
    }

    /**
     * 绑定为开关选择视图
     * @param category 绑定类别标签
     * @param key 视图与所指的 Preference 的 Key
     * @param value 设置的值
     * @param mainView 视图的根部视图
     * @param titleView 标题视图
     * @param summaryView 描述视图
     * @param switch 开关视图
     */
    fun bindSwitchView(
        category: SettingsCategoryWrapper?,
        key: String,
        value: Boolean,
        mainView: View,
        titleView: Int,
        summaryView: Int,
        switch: Int
    ): SettingsViewWrapper {
        val item =
            SettingsViewWrapper(key, mainView).setTitleView(mainView.findViewById(titleView))
                .setSummaryView(mainView.findViewById(summaryView))
                .setSwitchView(mainView.findViewById(switch))
        item.getSwitchView().isChecked = value
        bindViewInCategory(category, item)
        return item
    }

    private fun bindViewInCategory(category: SettingsCategoryWrapper?, item: SettingsViewWrapper) {
        category?.addSubView(item)
    }

    /**
     * 快速初始化滑动条视图
     * @param item 设置视图的包装类
     */
    @SuppressLint("StringFormatInvalid")
    fun initSeekBarView(item: SettingsViewWrapper) {
        val seekBarView = item.getSeekBarView()
        val seekBarValueView = item.getSeekBarValueView()
        seekBarValueView.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_text)

        seekBarView.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                DEFAULT_PREF.edit().putInt(item.key, progress).apply()
                setSeekBarValueTextView(
                    seekBarValueView,
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
                        seekBarValueView,
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

    /**
     * 快速初始化开关视图
     * @param item 设置视图的包装类
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun initSwitchView(item: SettingsViewWrapper) {
        val switchView = item.getSwitchView()
        switchView.setOnCheckedChangeListener { _, b ->
            DEFAULT_PREF.edit().putBoolean(item.key, b).apply()
            checkShowRebootDialog(item)
        }
        item.mainView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }

    /**
     * 快速初始化列表展示视图
     * 此函数主要用于从 R.array 加载数据
     * @param item 设置视图的包装类
     * @param defaultValue Preference 的默认值
     * @param itemsId 所有要展示的数据的名称
     * @param itemValuesId 所有要展示的数据的id值
     */
    fun initListView(
        item: SettingsViewWrapper,
        defaultValue: String,
        itemsId: Int,
        itemValuesId: Int
    ) {
        val entries = requireContext().resources.getStringArray(itemsId)
        val entryValues = requireContext().resources.getStringArray(itemValuesId)
        initListView(item, defaultValue, entries, entryValues)
    }

    /**
     * 快速初始化列表展示视图
     * @param item 设置视图的包装类
     * @param defaultValue Preference 的默认值
     * @param entries 所有要展示的数据的名称
     * @param entryValues 所有要展示的数据的id值
     */
    fun initListView(
        item: SettingsViewWrapper,
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
        item: SettingsViewWrapper,
        defaultValue: String,
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        val index = entryValues.indexOf(DEFAULT_PREF.getString(item.key, defaultValue))
        AlertDialog.Builder(requireContext())
            .setTitle(item.getTitleView().text)
            .setSingleChoiceItems(entries, index) { dialog, which ->
                if (which != index) {
                    val selectedValue = entryValues[which]
                    DEFAULT_PREF.edit().putString(item.key, selectedValue).apply()
                    updateListViewValue(item, defaultValue, entries, entryValues)
                    checkShowRebootDialog(item)
                }
                dialog.dismiss()
            }
            .setPositiveButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateListViewValue(
        item: SettingsViewWrapper,
        defaultValue: String,
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        val value = DEFAULT_PREF.getString(item.key, defaultValue)
        item.getValueView().text = entries[entryValues.indexOf(value)]
    }

    private fun checkShowRebootDialog(item: SettingsViewWrapper) {
        if (item.isRequiresReboot()) {
            TipDialog.Builder(requireContext())
                .setMessage(R.string.zh_setting_reboot_tip)
                .setConfirmClickListener { ZHTools.killApp() }
                .buildDialog()
        }
    }
}