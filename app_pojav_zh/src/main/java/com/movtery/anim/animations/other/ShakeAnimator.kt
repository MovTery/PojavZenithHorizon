package com.movtery.anim.animations.other

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.movtery.anim.animations.BaseAnimator

class ShakeAnimator: BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(target, "translationX", 0f, 15f, -15f, 10f, -10f, 7f, -7f, 4f, -4f, 0f))
    }
}