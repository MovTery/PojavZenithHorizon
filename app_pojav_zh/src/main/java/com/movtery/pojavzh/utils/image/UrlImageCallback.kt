package com.movtery.pojavzh.utils.image

import android.graphics.drawable.Drawable

interface UrlImageCallback {
    fun onImageLoaded(drawable: Drawable?, url: String)
    fun onImageCleared(placeholder: Drawable?, url: String)
}