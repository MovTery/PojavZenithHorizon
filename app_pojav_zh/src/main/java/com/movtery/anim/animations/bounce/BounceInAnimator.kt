package com.movtery.anim.animations.bounce

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class BounceInAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 1f, 1f, 1f),
            ObjectAnimator.ofFloat(target, "scaleX", 0.3f, 1.05f, 0.9f, 1f),
            ObjectAnimator.ofFloat(target, "scaleY", 0.3f, 1.05f, 0.9f, 1f))
    }
}