package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.View
import android.view.Window
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import net.kdt.pojavlaunch.databinding.DialogSeekbarBinding

class SeekbarDialog(
    context: Context,
    val title: String?,
    val message: String?,
    val suffix: String?,
    val value: Int,
    val min: Int,
    val max: Int,
    val previewTextGetter: PreviewTextContentGetter?,
    val listener: OnSeekBarProgressChangeListener?,
    val stopListener: OnSeekBarStopTrackingTouch?
) : FullScreenDialog(context), DraggableDialog.DialogInitializationListener {
    val binding = DialogSeekbarBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)

        title?.apply { binding.titleView.text = this } ?: let { binding.titleView.visibility = View.GONE }
        message?.apply { binding.messageView.text = this } ?: let { binding.scrollView.visibility = View.GONE }
        previewTextGetter?.let {
            binding.seekbarSomePreview.visibility = View.VISIBLE
            binding.seekbarSomePreview.text = it.onGet(value)
        }

        binding.seekbar.apply {
            min = this@SeekbarDialog.min
            max = this@SeekbarDialog.max

            progress = value

            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    listener?.onChange(progress)
                    previewTextGetter?.apply { binding.seekbarSomePreview.text = onGet(progress) }
                    updateValueText(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    stopListener?.onStop(progress)
                }
            })

            binding.remove.setOnClickListener {
                progress = (progress - 1).coerceAtLeast(min)
                stopListener?.onStop(progress)
            }
            binding.add.setOnClickListener {
                progress = (progress + 1).coerceAtMost(max)
                stopListener?.onStop(progress)
            }
        }

        binding.confirmButton.setOnClickListener { this.dismiss() }

        DraggableDialog.initDialog(this)

        updateValueText(value)
    }

    private fun updateValueText(progress: Int) {
        val valueText = "$progress ${suffix ?: ""}".trim()
        binding.seekbarValue.text = valueText
    }

    override fun onInit(): Window? {
        return window
    }

    class Builder(val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var suffix: String? = null
        private var value: Int = 0
        private var min: Int = 0
        private var max: Int = 0
        private var previewTextGetter: PreviewTextContentGetter? = null
        private var progressListener: OnSeekBarProgressChangeListener? = null
        private var stopListener: OnSeekBarStopTrackingTouch? = null

        fun buildDialog(): SeekbarDialog {
            val dialog = SeekbarDialog(
                context, title, message, suffix, value, min, max,
                previewTextGetter, progressListener, stopListener
            )
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

        fun setMessage(text: Int): Builder {
            return setMessage(context.getString(text))
        }

        fun setMessage(text: String): Builder {
            this.message = text
            return this
        }

        fun setSuffix(text: String): Builder {
            this.suffix = text
            return this
        }

        fun setValue(value: Int): Builder {
            this.value = value
            return this
        }

        fun setMin(min: Int): Builder {
            this.min = min
            return this
        }

        fun setMax(max: Int): Builder {
            this.max = max
            return this
        }

        fun setPreviewTextContentGetter(getter: PreviewTextContentGetter): Builder {
            this.previewTextGetter = getter
            return this
        }

        fun setOnSeekbarChangeListener(listener: OnSeekBarProgressChangeListener): Builder {
            this.progressListener = listener
            return this
        }

        fun setOnSeekbarStopTrackingTouch(listener: OnSeekBarStopTrackingTouch): Builder {
            this.stopListener = listener
            return this
        }
    }

    fun interface PreviewTextContentGetter {
        fun onGet(progress: Int): String
    }

    fun interface OnSeekBarProgressChangeListener {
        fun onChange(progress: Int)
    }

    fun interface OnSeekBarStopTrackingTouch {
        fun onStop(progress: Int)
    }
}