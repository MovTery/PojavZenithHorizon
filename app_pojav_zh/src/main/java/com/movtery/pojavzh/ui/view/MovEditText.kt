package com.movtery.pojavzh.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.daimajia.androidanimations.library.Techniques
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim

class MovEditText : AppCompatEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setError(error: CharSequence) {
        setErrorAnim()
        super.setError(error)
    }

    override fun setError(error: CharSequence, icon: Drawable) {
        setErrorAnim()
        super.setError(error, icon)
    }

    private fun setErrorAnim() {
        setViewAnim(this, Techniques.Shake)
    }
}
