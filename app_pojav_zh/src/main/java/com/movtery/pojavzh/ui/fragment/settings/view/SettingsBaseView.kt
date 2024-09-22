package com.movtery.pojavzh.ui.fragment.settings.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.movtery.pojavzh.ui.layout.AnimRelativeLayout
import net.kdt.pojavlaunch.R

@SuppressLint("Recycle", "StringFormatInvalid", "UseSwitchCompatOrMaterialCode")
class SettingsBaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): AnimRelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val mLayout: View
    private val mTitleTextView: TextView
    private val mSummaryTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_item_base, this, true)

        mLayout = findViewById(R.id.layout)
        mTitleTextView = findViewById(R.id.title)
        mSummaryTextView = findViewById(R.id.summary)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsBaseView)

        val titleString = attributes.getString(R.styleable.SettingsBaseView_baseViewTitle)
        titleString?.apply { mTitleTextView.text = this }

        val summaryString = attributes.getString(R.styleable.SettingsBaseView_baseViewSummary)
        summaryString?.apply { mSummaryTextView.text = this }
        summaryString ?: apply { mSummaryTextView.visibility = View.GONE }
    }

    fun setTitle(text: String?): SettingsBaseView {
        mTitleTextView.text = text
        return this
    }

    fun setSummary(text: String?): SettingsBaseView {
        mSummaryTextView.text = text
        return this
    }

    fun setOnItemClickListener(listener: OnClickListener): SettingsBaseView {
        mLayout.setOnClickListener(listener)
        return this
    }
}