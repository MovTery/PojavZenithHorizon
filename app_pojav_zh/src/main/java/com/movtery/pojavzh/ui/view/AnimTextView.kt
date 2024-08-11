package com.movtery.pojavzh.ui.view

import android.animation.AnimatorInflater
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import net.kdt.pojavlaunch.R

class AnimTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        stateListAnimator = AnimatorInflater.loadStateListAnimator(context, R.xml.anim_scale)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        post {
            pivotX = width / 2f
            pivotY = height / 2f
        }
    }
}