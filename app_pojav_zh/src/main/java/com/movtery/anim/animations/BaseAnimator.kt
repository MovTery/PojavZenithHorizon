package com.movtery.anim.animations

import android.animation.Animator
import android.view.View

abstract class BaseAnimator {
    abstract fun getAnimators(target: View): Array<Animator>
}