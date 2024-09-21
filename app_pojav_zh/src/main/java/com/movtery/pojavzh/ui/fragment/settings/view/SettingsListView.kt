package com.movtery.pojavzh.ui.fragment.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.movtery.pojavzh.ui.layout.AnimRelativeLayout
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

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
        checkEntries()

        mDefaultValue =
            if (mEntryValues.isNotEmpty()) attributes.getString(R.styleable.SettingsListView_listViewDefaultValue)
                ?: mEntryValues[0]
            else throw IllegalArgumentException("Entry values cannot be empty.")

        attributes.recycle()

        mLayout.setOnClickListener { createAListDialog() }

        updateListViewValue()
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

        checkEntries()

        updateListViewValue()
        return this
    }

    private fun checkEntries() {
        if (mEntries.size == mEntryValues.size) return
        throw IllegalArgumentException("Array lengths are inconsistent.")
    }

    private fun createAListDialog() {
        val index = mEntryValues.indexOf(DEFAULT_PREF.getString(mKey, mDefaultValue))
        AlertDialog.Builder(context)
            .setTitle(mTitleTextView.text)
            .setSingleChoiceItems(mEntries, index) { dialog, which ->
                if (which != index) {
                    val selectedValue = mEntryValues[which]
                    DEFAULT_PREF.edit().putString(mKey, selectedValue).apply()
                    updateListViewValue()
                    if (mRequiresReboot) Utils.checkShowRebootDialog(context)
                }
                dialog.dismiss()
            }
            .setPositiveButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateListViewValue() {
        if (mEntries.isNotEmpty() && mEntryValues.isNotEmpty()) {
            val value = DEFAULT_PREF.getString(mKey, mDefaultValue)
            val index = mEntryValues.indexOf(value).takeIf { it in mEntryValues.indices } ?: run {
                DEFAULT_PREF.edit().putString(mKey, mDefaultValue).apply()
                0
            }
            mValueTextView.text = mEntries[index]
        }
    }
}