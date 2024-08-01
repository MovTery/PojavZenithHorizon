package com.movtery.pojavzh.ui.fragment

import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.utils.anim.SlideAnimation

abstract class FragmentWithAnim : Fragment, SlideAnimation {
    var yoYos: Array<YoYoString>? = null

    constructor()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onResume() {
        super.onResume()
        //如果恢复视图时结束动画仍在运行，则停止它们，并重新调用slideIn方法
        yoYos?.let {
            var isRunning = false
            for (yoYo in yoYos!!) {
                if (yoYo.isRunning) {
                    isRunning = true
                    yoYo.stop()
                }
                if (isRunning) {
                    slideIn()
                }
            }
        }
    }
}
