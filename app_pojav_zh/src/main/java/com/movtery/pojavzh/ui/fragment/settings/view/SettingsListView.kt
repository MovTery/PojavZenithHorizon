package com.movtery.pojavzh.ui.fragment.settings.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.movtery.pojavzh.ui.layout.AnimRelativeLayout
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

@SuppressLint("Recycle", "StringFormatInvalid", "UseSwitchCompatOrMaterialCode")
class SettingsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): AnimRelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val mKey: String
    private val mLayout: View
    private val mTitleTextView: TextView
    private val mSummaryTextView: TextView
    private val mValueTextView: TextView
    private val mRequiresReboot: Boolean
    private val mDefaultValue: String
    private var mEntries: Array<String>
    private var mEntryValues: Array<String>

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_item_list, this, true)

        mLayout = findViewById(R.id.layout)
        mTitleTextView = findViewById(R.id.title)
        mSummaryTextView = findViewById(R.id.summary)
        mValueTextView = findViewById(R.id.value)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsListView)

        val key = attributes.getString(R.styleable.SettingsListView_listViewKey)
        mKey = key ?: throw IllegalArgumentException("The 'key' attribute is required.")

        val titleString = attributes.getString(R.styleable.SettingsListView_listViewTitle)
        titleString?.apply { mTitleTextView.text = this }

        val summaryString = attributes.getString(R.styleable.SettingsListView_listViewSummary)
        summaryString?.apply { mSummaryTextView.text = this }
        summaryString ?: apply { mSummaryTextView.visibility = View.GONE }

        mRequiresReboot = attributes.getBoolean(R.styleable.SettingsListView_listViewRequiresReboot, false)

        val namesID = attributes.getResourceId(R.styleable.SettingsListView_listViewNames, 0)
        val valuesID = attributes.getResourceId(R.styleable.SettingsListView_listViewValues, 0)
        mEntries = if (namesID != 0) context.resources.getStringArray(namesID) else emptyArray()
        mEntryValues = if (valuesID != 0) context.resources.getStringArray(valuesID) else emptyArray()

        mDefaultValue = attributes.getString(R.styleable.SettingsListView_listViewDefaultValue) ?: mEntryValues[0]

        mLayout.setOnClickListener { createAListDialog(mEntries, mEntryValues) }

        updateListViewValue(mEntries, mEntryValues)
    }

    fun setTitle(text: String?): SettingsListView {
        mTitleTextView.text = text
        return this
    }

    fun setSummary(text: String?): SettingsListView {
        mSummaryTextView.text = text
        return this
    }

    fun setEntries(names: Array<String>, values: Array<String>): SettingsListView {
        mEntries = names
        mEntryValues = values

        updateListViewValue(mEntries, mEntryValues)
        return this
    }

    private fun createAListDialog(
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        val index = entryValues.indexOf(DEFAULT_PREF.getString(mKey, mDefaultValue))
        AlertDialog.Builder(context)
            .setTitle(mTitleTextView.text)
            .setSingleChoiceItems(entries, index) { dialog, which ->
                if (which != index) {
                    val selectedValue = entryValues[which]
                    DEFAULT_PREF.edit().putString(mKey, selectedValue).apply()
                    updateListViewValue(entries, entryValues)
                    if (mRequiresReboot) Utils.checkShowRebootDialog(context)
                }
                dialog.dismiss()
            }
            .setPositiveButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateListViewValue(
        entries: Array<String>,
        entryValues: Array<String>
    ) {
        if (entries.isNotEmpty() && entryValues.isNotEmpty()) {
            val value = DEFAULT_PREF.getString(mKey, mDefaultValue)
            val index = entryValues.indexOf(value).takeIf { it in entryValues.indices } ?: run {
                DEFAULT_PREF.edit().putString(mKey, mDefaultValue).apply()
                0
            }
            mValueTextView.text = entries[index]
        }
    }
}