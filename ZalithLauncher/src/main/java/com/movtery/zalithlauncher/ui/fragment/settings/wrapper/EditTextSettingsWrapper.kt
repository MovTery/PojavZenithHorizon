package com.movtery.zalithlauncher.ui.fragment.settings.wrapper

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP or Gravity.START
            setOnEditorActionListener { _, actionId, event ->
                //处理“确定”按钮的点击事件
                actionId == EditorInfo.IME_ACTION_DONE || event.action == KeyEvent.ACTION_DOWN
            }
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    // 处理焦点丢失的情况，如收起软键盘
                    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
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