package com.movtery.anim.animations.slide

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class SlideInDownAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        val distance = target.top + target.height
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(target, "translationY", -distance.toFloat(), 0f))
    }
}