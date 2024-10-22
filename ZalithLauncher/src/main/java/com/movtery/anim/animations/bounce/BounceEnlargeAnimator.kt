package com.movtery.anim.animations.bounce

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class BounceEnlargeAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 0.8f, 1f, 1f),
            ObjectAnimator.ofFloat(target, "scaleX", 0.7f, 1.03f, 0.95f, 1f),
            ObjectAnimator.ofFloat(target, "scaleY", 0.7f, 1.03f, 0.95f, 1f))
    }
}