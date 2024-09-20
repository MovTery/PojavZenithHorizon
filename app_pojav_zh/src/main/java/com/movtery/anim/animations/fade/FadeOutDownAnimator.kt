package com.movtery.anim.animations.fade

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class FadeOutDownAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            ObjectAnimator.ofFloat(target, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(target, "translationY", 0f, (target.height / 4).toFloat()))
    }
}