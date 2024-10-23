package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Window
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.movtery.zalithlauncher.ui.dialog.DraggableDialog.DialogInitializationListener
import net.kdt.pojavlaunch.databinding.DialogImagePreviewBinding
import java.io.File

class ViewImageDialog private constructor(
    context: Context,
    requestBuilder: RequestBuilder<Drawable>,
    title: String? = null
) :
    FullScreenDialog(context), DialogInitializationListener {
        private val binding = DialogImagePreviewBinding.inflate(layoutInflater)

        init {
            setContentView(binding.root)
            title?.let { binding.titleView.text = it }
            requestBuilder.into(binding.imageView)
            binding.closeButton.setOnClickListener { dismiss() }
        }

    override fun onInit(): Window {
        return this.window!!
    }

    class Builder(val context: Context) {
        private val rm: RequestManager = Glide.with(context)
        private lateinit var rb: RequestBuilder<Drawable>
        private var title: String? = null
        private var isImageSet: Boolean = false

        fun buildDialog(): ViewImageDialog {
            if (!isImageSet) throw IllegalStateException("Please set the image first!")
            val dialog = ViewImageDialog(this.context, rb, title)
            dialog.show()
            return dialog
        }

        fun setTitle(text: Int): Builder {
            return setTitle(context.getString(text))
        }

        fun setTitle(text: String): Builder {
            this.title = text
            return this
        }

        fun checkImage() {
            if (isImageSet) throw IllegalStateException("The image has already been set!")
            isImageSet = true
        }

        fun setImage(uri: Uri?): Builder {
            checkImage()
            rb = rm.load(uri)
            return this
        }

        fun setImage(bitmap: Bitmap?): Builder {
            checkImage()
            rb = rm.load(bitmap)
            return this
        }

        fun setImage(file: File?): Builder {
            checkImage()
            rb = rm.load(file)
            return this
        }

        fun setImage(model: Any?): Builder {
            checkImage()
            rb = rm.load(model)
            return this
        }

        fun setImage(string: String?): Builder {
            checkImage()
            rb = rm.load(string)
            return this
        }

        fun setImage(resourceId: Int?): Builder {
            checkImage()
            rb = rm.load(resourceId)
            return this
        }

        fun setImage(drawable: Drawable?): Builder {
            checkImage()
            rb = rm.load(drawable)
            return this
        }
    }
}