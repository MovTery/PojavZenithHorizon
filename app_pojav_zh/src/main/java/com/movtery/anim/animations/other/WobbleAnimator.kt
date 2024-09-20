package com.movtery.anim.animations.other

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class WobbleAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        val width = target.width.toFloat()
        val one = (width / 100.0).toFloat()
        return arrayOf(
            ObjectAnimator.ofFloat(target, "translationX", 0 * one, -25 * one, 20 * one, -15 * one, 10 * one, -5 * one, 0 * one, 0f),
            ObjectAnimator.ofFloat(target, "rotation", 0f, -5f, 3f, -3f, 2f, -1f, 0f))
    }
}