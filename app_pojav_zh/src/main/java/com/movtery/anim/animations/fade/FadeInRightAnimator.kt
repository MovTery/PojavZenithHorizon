package com.movtery.anim.animations.fade

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class FadeInRightAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(target, "translationX", (target.width / 4).toFloat(), 0f))
    }
}