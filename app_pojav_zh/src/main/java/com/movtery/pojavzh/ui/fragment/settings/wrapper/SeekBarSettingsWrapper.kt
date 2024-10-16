package com.movtery.pojavzh.ui.fragment.settings.wrapper

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.movtery.pojavzh.feature.log.Logging.e
import com.movtery.pojavzh.setting.Settings
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import net.kdt.pojavlaunch.R

@SuppressLint("UseSwitchCompatOrMaterialCode", "StringFormatInvalid")
class SeekBarSettingsWrapper(
    val context: Context,
    val key: String,
    val value: Int,
    val mainView: View,
    val titleView: TextView,
    val summaryView: TextView,
    val valueView: TextView,
    val seekbarView: SeekBar,
    val suffix: String,
    onStartListener: OnStartInit?
) : AbstractSettingsWrapper(mainView) {
    private var listener: OnSeekBarProgressChangeListener? = null

    constructor(
        context: Context,
        key: String,
        value: Int,
        mainView: View,
        titleView: TextView,
        summaryView: TextView,
        valueView: TextView,
        seekbarView: SeekBar,
        suffix: String,
    ) : this(
        context,
        key,
        value,
        mainView,
        titleView,
        summaryView,
        valueView,
        seekbarView,
        suffix,
        null
    )

    init {
        onStartListener?.onStart(this)

        seekbarView.progress = value
        valueView.background = ContextCompat.getDrawable(context, R.drawable.background_text)
        setSeekBarValueTextView()

        seekbarView.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listener?.onChange(progress)
                Settings.Manager.put(key, progress).save()
                setSeekBarValueTextView()
                checkShowRebootDialog(context)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setSeekBarValueTextView()
            }
        })

        mainView.setOnClickListener {
            val builder = EditTextDialog.Builder(context)
                .setEditText(seekbarView.progress.toString())
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setTitle(titleView.text.toString())
                .setMessage(summaryView.text.toString())
            builder.setConfirmListener { editBox: EditText ->
                val string = editBox.text.toString()

                if (string.isEmpty()) {
                    editBox.error = context.getString(R.string.generic_error_field_empty)
                    return@setConfirmListener false
                }

                val value: Int
                try {
                    value = string.toInt()
                } catch (e: NumberFormatException) {
                    e("Custom Seek Bar", e.toString())

                    editBox.error = context.getString(R.string.generic_input_invalid)
                    return@setConfirmListener false
                }

                if (value < seekbarView.min) {
                    val minValue =
                        String.format("%s %s", seekbarView.min, suffix)
                    editBox.error = context.getString(R.string.generic_input_too_small, minValue)
                    return@setConfirmListener false
                }
                if (value > seekbarView.max) {
                    val maxValue =
                        String.format("%s %s", seekbarView.max, suffix)
                    editBox.error = context.getString(R.string.generic_input_too_big, maxValue)
                    return@setConfirmListener false
                }

                seekbarView.progress = value
                true
            }.buildDialog()
        }
    }

    fun setSeekBarValueTextView() {
        val text = "${seekbarView.progress} $suffix"
        valueView.text = text
    }

    fun setOnSeekBarProgressChangeListener(listener: OnSeekBarProgressChangeListener) {
        this.listener = listener
    }

    //部分场景需要在seekbar被包装前，完成一些操作，比如动态调整最大值或最小值
    fun interface OnStartInit {
        fun onStart(wrapper: SeekBarSettingsWrapper)
    }

    fun interface OnSeekBarProgressChangeListener {
        fun onChange(progress: Int)
    }
}