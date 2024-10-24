package com.movtery.zalithlauncher.ui.fragment.settings.wrapper

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.movtery.zalithlauncher.setting.Settings

class EditTextSettingsWrapper(
    private val key: String,
    val value: String?,
    val mainView: View,
    editText: EditText
) : AbstractSettingsWrapper(mainView) {
    private var listener: OnTextChangedListener? = null

    init {
        editText.apply {
            setText(value)
            inputType = InputType.TYPE_CLASS_TEXT
            gravity = Gravity.TOP or Gravity.START
            setOnEditorActionListener { _, _, _ ->
                clearFocus()
                false
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    Settings.Manager.put(key, s).save()
                    listener?.onChanged(s.toString())
                }
            })
        }
    }

    fun setOnTextChangedListener(listener: OnTextChangedListener) {
        this.listener = listener
    }

    fun interface OnTextChangedListener {
        fun onChanged(text: String)
    }
}