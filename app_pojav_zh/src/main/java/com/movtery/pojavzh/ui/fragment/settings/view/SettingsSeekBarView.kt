package com.movtery.pojavzh.ui.fragment.settings.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.layout.AnimRelativeLayout
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

@SuppressLint("StringFormatInvalid")
class SettingsSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): AnimRelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val mKey: String
    private val mLayout: View
    private val mTitleTextView: TextView
    private val mSummaryTextView: TextView
    private val mInfoTextView: TextView
    private val mSeekBar: SeekBar
    private val mValueTextView: TextView
    private var mSuffix: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_item_seekbar, this, true)

        mLayout = findViewById(R.id.layout)
        mTitleTextView = findViewById(R.id.title)
        mSummaryTextView = findViewById(R.id.summary)
        mInfoTextView = findViewById(R.id.info)
        mSeekBar = findViewById(R.id.seekbar)
        mValueTextView = findViewById(R.id.value)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsSeekBarView)

        val key = attributes.getString(R.styleable.SettingsSeekBarView_seekbarViewKey)
        mKey = key ?: throw IllegalArgumentException("The 'key' attribute is required.")

        val titleString = attributes.getString(R.styleable.SettingsSeekBarView_seekbarViewTitle)
        titleString?.apply { mTitleTextView.text = this }

        val summaryString = attributes.getString(R.styleable.SettingsSeekBarView_seekbarViewSummary)
        summaryString?.apply { mSummaryTextView.text = this }
        summaryString ?: apply { mSummaryTextView.visibility = View.GONE }

        val suffixString = attributes.getString(R.styleable.SettingsSeekBarView_seekbarViewSuffix)
        suffixString?.apply { mSuffix = this }

        val defaultValue = attributes.getInt(R.styleable.SettingsSeekBarView_seekbarViewDefaultValue, 0)
        val max = attributes.getInt(R.styleable.SettingsSeekBarView_seekbarViewSeekBarMax, 100)
        val min = attributes.getInt(R.styleable.SettingsSeekBarView_seekbarViewSeekBarMin, 0)
        mSeekBar.apply {
            this.max = max
            this.min = min
            progress = DEFAULT_PREF.getInt(mKey, defaultValue)
            updateValueText()
        }

        attributes.recycle()

        mLayout.setOnClickListener {
            val builder = EditTextDialog.Builder(context)
                .setEditText(mSeekBar.progress.toString())
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setTitle(mTitleTextView.text.toString())
                .setMessage(mSummaryTextView.text.toString())
            builder.setConfirmListener { editBox: EditText ->
                val string = editBox.text.toString()

                if (string.isEmpty()) {
                    editBox.error = context.getString(R.string.global_error_field_empty)
                    return@setConfirmListener false
                }

                val value: Int
                try {
                    value = string.toInt()
                } catch (e: NumberFormatException) {
                    e("Custom Seek Bar", e.toString())

                    editBox.error = context.getString(R.string.zh_input_invalid)
                    return@setConfirmListener false
                }

                if (value < mSeekBar.min) {
                    val minValue =
                        String.format("%s %s", mSeekBar.min, mSuffix)
                    editBox.error = context.getString(R.string.zh_input_too_small, minValue)
                    return@setConfirmListener false
                }
                if (value > mSeekBar.max) {
                    val maxValue =
                        String.format("%s %s", mSeekBar.max, mSuffix)
                    editBox.error = context.getString(R.string.zh_input_too_big, maxValue)
                    return@setConfirmListener false
                }

                mSeekBar.progress = value
                true
            }.buildDialog()
        }

        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                DEFAULT_PREF.edit().putInt(mKey, progress).apply()
                updateValueText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateValueText() {
        mValueTextView.text = "${mSeekBar.progress} ${mSuffix.trim()}"
    }

    fun setTitle(text: String?): SettingsSeekBarView {
        mTitleTextView.text = text
        return this
    }

    fun setSummary(text: String?): SettingsSeekBarView {
        text?.let {
            mSummaryTextView.text = it
            mSummaryTextView.visibility = View.VISIBLE
        } ?: apply {
            mSummaryTextView.visibility = View.GONE
        }
        return this
    }

    fun setInfo(text: String?): SettingsSeekBarView {
        text?.let {
            mInfoTextView.text = it
            mInfoTextView.visibility = View.VISIBLE
        } ?: apply {
            mInfoTextView.visibility = View.GONE
        }
        return this
    }

    fun setProgress(progress: Int): SettingsSeekBarView {
        mSeekBar.progress = progress
        return this
    }

    fun getProgress() = mSeekBar.progress

    fun setMin(int: Int): SettingsSeekBarView {
        mSeekBar.min = int
        return this
    }

    fun setMax(int: Int): SettingsSeekBarView {
        mSeekBar.max = int
        return this
    }

    fun setSuffix(text: String): SettingsSeekBarView {
        mSuffix = text
        return this
    }
}