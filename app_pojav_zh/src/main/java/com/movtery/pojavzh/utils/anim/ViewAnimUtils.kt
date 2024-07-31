package com.movtery.pojavzh.utils.anim

import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import net.kdt.pojavlaunch.prefs.LauncherPreferences

object ViewAnimUtils {
    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques) {
        setViewAnim(view, techniques, LauncherPreferences.PREF_ANIMATION_SPEED.toLong())
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, duration: Long) {
        if (LauncherPreferences.PREF_ANIMATION) {
            YoYo.with(techniques)
                .duration(duration)
                .playOn(view)
        }
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, onStart: YoYo.AnimatorCallback, onEnd: YoYo.AnimatorCallback) {
        if (LauncherPreferences.PREF_ANIMATION) {
            YoYo.with(techniques)
                .duration(LauncherPreferences.PREF_ANIMATION_SPEED.toLong())
                .onStart(onStart)
                .onEnd(onEnd)
                .playOn(view)
        }
    }

    @JvmStatic
    fun slideInAnim(animation: SlideAnimation) {
        animation.slideIn()
    }
}