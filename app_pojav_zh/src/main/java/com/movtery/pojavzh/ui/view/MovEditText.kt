package com.movtery.pojavzh.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.daimajia.androidanimations.library.Techniques
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim

class MovEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun setError(error: CharSequence?) {
        super.setError(error)
        error?.let { setErrorAnim() }
    }

    override fun setError(error: CharSequence?, icon: Drawable?) {
        super.setError(error, icon)
        error?.let { setErrorAnim() }
    }

    private fun setErrorAnim() {
        setViewAnim(this, Techniques.Shake)
    }
}
