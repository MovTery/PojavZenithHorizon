package com.movtery.pojavzh.ui.fragment

import androidx.fragment.app.Fragment
import com.movtery.anim.AnimPlayer
import com.movtery.pojavzh.utils.anim.SlideAnimation
import net.kdt.pojavlaunch.prefs.LauncherPreferences

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    private var animPlayer: AnimPlayer = AnimPlayer()

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onStart() {
        super.onStart()
        playAnimation { slideIn(it) }
    }

    fun slideOut() {
        playAnimation { slideOut(it) }
    }

    private fun playAnimation(animationAction: (AnimPlayer) -> Unit) {
        if (LauncherPreferences.PREF_ANIMATION) {
            animPlayer.clearEntries()
            animPlayer.apply {
                animationAction(this)
                start()
            }
        }
    }
}
