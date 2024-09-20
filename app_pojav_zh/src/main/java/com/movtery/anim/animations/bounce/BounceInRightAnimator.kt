package com.movtery.anim.animations.bounce

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class BounceInRightAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 1f, 1f, 1f),
            ObjectAnimator.ofFloat(target, "translationX", (target.measuredWidth + target.width).toFloat(), -30f, 10f, 0f))
    }
}