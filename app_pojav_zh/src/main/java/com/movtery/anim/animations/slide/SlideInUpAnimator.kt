package com.movtery.anim.animations.slide

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.movtery.anim.animations.BaseAnimator

class SlideInUpAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        val parent = target.parent as ViewGroup
        val distance = parent.height - target.top
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(target, "translationY", distance.toFloat(), 0f))
    }
}