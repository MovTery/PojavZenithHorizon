package com.movtery.anim.animations.slide

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.movtery.anim.animations.BaseAnimator

class SlideOutRightAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        val parent = target.parent as ViewGroup
        val distance = parent.width - target.left
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(target, "translationX", 0f, distance.toFloat()))
    }
}