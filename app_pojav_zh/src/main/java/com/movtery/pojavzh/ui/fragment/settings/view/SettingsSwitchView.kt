package com.movtery.pojavzh.ui.fragment.settings.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import com.movtery.pojavzh.ui.fragment.settings.view.Utils.Companion.checkShowRebootDialog
import com.movtery.pojavzh.ui.layout.AnimRelativeLayout
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SettingsSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): AnimRelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val mKey: String
    private val mLayout: View
    private val mTitleTextView: TextView
    private val mSummaryTextView: TextView
    private val mSwitch: Switch
    private val mListeners: MutableList<CompoundButton.OnCheckedChangeListener> = ArrayList()

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_item_switch, this, true)

        mLayout = findViewById(R.id.layout)
        mTitleTextView = findViewById(R.id.title)
        mSummaryTextView = findViewById(R.id.summary)
        mSwitch = findViewById(R.id.switchView)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsSwitchView)

        val key = attributes.getString(R.styleable.SettingsSwitchView_switchViewKey)
        mKey = key ?: throw IllegalArgumentException("The 'key' attribute is required.")

        val titleString = attributes.getString(R.styleable.SettingsSwitchView_switchViewTitle)
        titleString?.apply { mTitleTextView.text = this }

        val summaryString = attributes.getString(R.styleable.SettingsSwitchView_switchViewSummary)
        summaryString?.apply { mSummaryTextView.text = this }
        summaryString ?: apply { mSummaryTextView.visibility = View.GONE }

        val check = attributes.getBoolean(R.styleable.SettingsSwitchView_switchViewSwitchCheck, false)
        mSwitch.isChecked = DEFAULT_PREF.getBoolean(mKey, check)

        val requiresReboot = attributes.getBoolean(R.styleable.SettingsSwitchView_switchViewRequiresReboot, false)

        attributes.recycle()

        mSwitch.setOnCheckedChangeListener { v, b ->
            DEFAULT_PREF.edit().putBoolean(mKey, b).apply()
            if (requiresReboot) checkShowRebootDialog(context)
            mListeners.forEach { it.onCheckedChanged(v, b) }
        }

        mLayout.setOnClickListener {
            mSwitch.isChecked = !mSwitch.isChecked
        }
    }

    fun setTitle(text: String?): SettingsSwitchView {
        mTitleTextView.text = text
        return this
    }

    fun setSummary(text: String?): SettingsSwitchView {
        mSummaryTextView.text = text
        return this
    }

    fun addOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener): SettingsSwitchView {
        mListeners.add(listener)
        return this
    }
}