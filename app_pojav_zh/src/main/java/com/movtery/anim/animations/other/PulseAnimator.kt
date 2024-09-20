package com.movtery.anim.animations.other

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class PulseAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.1f, 1f),
            ObjectAnimator.ofFloat(target, "scaleX", 1f, 1.1f, 1f))
    }
}