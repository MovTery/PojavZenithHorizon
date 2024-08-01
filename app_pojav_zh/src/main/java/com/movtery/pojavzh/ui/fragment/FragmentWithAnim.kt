package com.movtery.pojavzh.ui.fragment

import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.YoYo
import com.movtery.pojavzh.utils.anim.SlideAnimation

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    var yoYos: Array<YoYo.YoYoString>? = null

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)
}
