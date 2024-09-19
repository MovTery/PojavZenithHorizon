package com.movtery.pojavzh.utils.image

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.min

class ImageUtils {
    companion object {
        /***
         * 通过读取文件的头部信息来判断文件是否为图片
         * @param filePath 文件路径
         * @return 返回是否为图片
         */
        @JvmStatic
        fun isImage(filePath: File?): Boolean {
            try {
                FileInputStream(filePath).use { input ->
                    val header = ByteArray(4)
                    if (input.read(header, 0, 4) != -1) {
                        return (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()) ||  //JPEG
                                (header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()) ||  //PNG
                                (header[0] == 0x47.toByte() && header[1] == 0x49.toByte() && header[2] == 0x46.toByte()) ||  //GIF
                                (header[0] == 0x42.toByte() && header[1] == 0x4D.toByte()) ||  //BMP
                                ((header[0] == 0x49.toByte() && header[1] == 0x49.toByte() && header[2] == 0x2A.toByte() && header[3] == 0x00.toByte()) ||  //TIFF
                                        (header[0] == 0x4D.toByte() && header[1] == 0x4D.toByte() && header[2] == 0x00.toByte() && header[3] == 0x2A.toByte())) //TIFF
                    }
                }
            } catch (e: IOException) {
                return false
            }
            return false
        }

        /**
         * 通过计算图片的长款比例来计算缩放后的长款数据
         * @param imageWidth 原始图片的长
         * @param imageHeight 原始图片的宽
         * @param maxSize 需要限制在多大的空间
         * @return 返回一个缩放后的长宽数据对象
         */
        @JvmStatic
        fun resizeWithRatio(imageWidth: Int, imageHeight: Int, maxSize: Int): Dimension {
            val widthRatio = maxSize.toDouble() / imageWidth
            val heightRatio = maxSize.toDouble() / imageHeight

            //选择较小的缩放比例，确保长宽按比例缩小且不超过maxSize限制
            val ratio = min(widthRatio, heightRatio)
            val newWidth = (imageWidth * ratio).toInt()
            val newHeight = (imageHeight * ratio).toInt()

            return Dimension(newWidth, newHeight)
        }

        /**
         * 通过链接获取图片
         * @param url 有效的图片链接
         * @param callback 加载完成后，通过接口回调使用，同时返还传入时的url，以作识别
         */
        @JvmStatic
        fun loadDrawableFromUrl(context: Context, url: String, callback: UrlImageCallback) {
            Glide.with(context)
                .load(url)
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        callback.onImageLoaded(resource, url)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        callback.onImageCleared(placeholder, url)
                    }
                })
        }
    }
}
