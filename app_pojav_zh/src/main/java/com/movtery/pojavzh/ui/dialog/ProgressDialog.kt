package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.View
import android.view.Window
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.dialog.DraggableDialog.DialogInitializationListener
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import net.kdt.pojavlaunch.databinding.DialogProgressBinding

class ProgressDialog(context: Context, listener: OnCancelListener) : FullScreenDialog(context),
    DialogInitializationListener {
    private val binding = DialogProgressBinding.inflate(layoutInflater)

    init {
        this.setContentView(binding.root)
        this.setCancelable(false)
    }

    init {
        binding.progressBar.setMax(1000)
        binding.cancelButton.setOnClickListener {
            if (!listener.onClick()) return@setOnClickListener
            dismiss()
        }

        DraggableDialog.initDialog(this)
    }

    fun updateText(text: String?) {
        text?.apply { binding.textView.text = this }
    }

    fun updateRate(processingRate: Long) {
        if (processingRate > 0) binding.uploadRate.visibility = View.VISIBLE
        val formatFileSize = formatFileSize(processingRate)
        "$formatFileSize/s".also { binding.uploadRate.text = it }
    }

    fun updateProgress(progress: Double, total: Double) {
        val doubleValue = progress / total * 1000
        val intValue = doubleValue.toInt()

        binding.progressBar.apply {
            visibility = if (doubleValue > 0) View.VISIBLE else View.GONE
            setProgress(intValue, AllSettings.animation)
        }
    }

    override fun onInit(): Window? {
        return window
    }

    fun interface OnCancelListener {
        fun onClick(): Boolean
    }
}
