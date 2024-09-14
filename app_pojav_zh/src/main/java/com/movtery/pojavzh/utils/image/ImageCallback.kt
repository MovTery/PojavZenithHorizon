package com.movtery.pojavzh.utils.image

import android.graphics.drawable.Drawable

interface ImageCallback {
    fun onImageLoaded(drawable: Drawable?)
    fun onImageFailed(errorDrawable: Drawable?)
}