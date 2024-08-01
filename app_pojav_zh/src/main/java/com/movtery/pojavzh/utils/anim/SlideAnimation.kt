package com.movtery.pojavzh.utils.anim

import com.daimajia.androidanimations.library.YoYo.YoYoString

interface SlideAnimation {
    fun slideIn(): Array<YoYoString?>?
    fun slideOut(): Array<YoYoString?>?
}