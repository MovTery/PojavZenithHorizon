package com.movtery.pojavzh.utils.anim

import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import net.kdt.pojavlaunch.prefs.LauncherPreferences

object ViewAnimUtils {
    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques): YoYo.YoYoString? {
        return setViewAnim(view, techniques, LauncherPreferences.PREF_ANIMATION_SPEED.toLong())
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, duration: Long): YoYo.YoYoString? {
        if (LauncherPreferences.PREF_ANIMATION) {
            return YoYo.with(techniques)
                .duration(duration)
                .playOn(view)
        }
        return null
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, onStart: YoYo.AnimatorCallback, onEnd: YoYo.AnimatorCallback): YoYo.YoYoString? {
        if (LauncherPreferences.PREF_ANIMATION) {
            return YoYo.with(techniques)
                .duration(LauncherPreferences.PREF_ANIMATION_SPEED.toLong())
                .onStart(onStart)
                .onEnd(onEnd)
                .playOn(view)
        }
        return null
    }

    @JvmStatic
    fun slideInAnim(fragmentWithAnim: FragmentWithAnim) {
        fragmentWithAnim.slideIn()
    }
}