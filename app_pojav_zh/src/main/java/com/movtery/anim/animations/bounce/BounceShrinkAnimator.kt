package com.movtery.anim.animations.bounce

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class BounceShrinkAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 1f, 0.85f, 0.5f, 0f),
            ObjectAnimator.ofFloat(target, "scaleX", 1f, 1.03f, 0.6f, 0f),
            ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.03f, 0.6f, 0f))
    }
}