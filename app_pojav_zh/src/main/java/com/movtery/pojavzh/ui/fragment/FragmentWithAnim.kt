package com.movtery.pojavzh.ui.fragment

import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.utils.anim.SlideAnimation

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    var yoYos: Array<YoYoString?>? = null

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onResume() {
        //如果恢复视图时结束动画仍在运行，则停止它们
        var isRunning = false
        yoYos?.let {
            for (yoYo in yoYos!!) {
                yoYo?.let {
                    if (yoYo.isStarted && yoYo.isRunning) {
                        if (!isRunning) isRunning = true
                        yoYo.stop(true)
                    }
                }
            }
        }
        if (isRunning) slideIn()
        super.onResume()
    }
}
