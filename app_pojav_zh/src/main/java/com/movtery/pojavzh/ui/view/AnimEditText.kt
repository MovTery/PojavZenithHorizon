package com.movtery.pojavzh.ui.view

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.daimajia.androidanimations.library.Techniques
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import net.kdt.pojavlaunch.R

class AnimEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        post {
            pivotX = width / 2f
            pivotY = height / 2f
            stateListAnimator = AnimatorInflater.loadStateListAnimator(context, R.xml.anim_scale)
        }
    }

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
