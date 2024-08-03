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
        return YoYo.with(techniques)
            .duration(duration)
            .playOn(view)
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, onStart: YoYo.AnimatorCallback, onEnd: YoYo.AnimatorCallback): YoYo.YoYoString? {
        return setViewAnim(view, techniques, LauncherPreferences.PREF_ANIMATION_SPEED.toLong(), onStart, onEnd)
    }

    @JvmStatic
    fun setViewAnim(view: View, techniques: Techniques, duration: Long, onStart: YoYo.AnimatorCallback, onEnd: YoYo.AnimatorCallback): YoYo.YoYoString? {
        return YoYo.with(techniques)
            .duration(duration)
            .onStart(onStart)
            .onEnd(onEnd)
            .playOn(view)
    }

    @JvmStatic
    fun slideInAnim(fragmentWithAnim: FragmentWithAnim) {
        if (LauncherPreferences.PREF_ANIMATION) fragmentWithAnim.slideIn()
    }
}