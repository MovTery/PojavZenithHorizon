package com.movtery.pojavzh.ui.fragment

import androidx.fragment.app.Fragment
import com.movtery.pojavzh.utils.anim.OnSlideOutListener
import com.movtery.pojavzh.utils.anim.SlideAnimation

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun slideOut(listener: OnSlideOutListener) {
        listener.onEnd()
    }
}
