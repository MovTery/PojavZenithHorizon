package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.dialog.DraggableDialog.DialogInitializationListener
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import net.kdt.pojavlaunch.R

class ProgressDialog(context: Context, listener: OnCancelListener) : FullScreenDialog(context),
    DialogInitializationListener {
    private var message: TextView? = null
    private var rate: TextView? = null
    private var progressBar: ProgressBar? = null

    init {
        this.setContentView(R.layout.dialog_progress)
        this.setCancelable(false)
    }

    init {
        this.message = findViewById(R.id.zh_download_upload_textView)
        this.rate = findViewById(R.id.zh_download_upload_rate)
        this.progressBar = findViewById(R.id.progressBar2)
        val cancelButton = findViewById<Button>(R.id.zh_download_cancel_button)

        progressBar?.setMax(1000)
        cancelButton.setOnClickListener {
            if (!listener.onClick()) return@setOnClickListener
            dismiss()
        }

        DraggableDialog.initDialog(this)
    }

    fun updateText(text: String?) {
        text?.apply { message?.text = this }
    }

    fun updateRate(processingRate: Long) {
        if (processingRate > 0) rate?.visibility = View.VISIBLE
        val formatFileSize = formatFileSize(processingRate)
        "$formatFileSize/s".also { rate?.text = it }
    }

    fun updateProgress(progress: Double, total: Double) {
        val doubleValue = progress / total * 1000
        val intValue = doubleValue.toInt()

        progressBar?.visibility = if (doubleValue > 0) View.VISIBLE else View.GONE
        progressBar?.setProgress(intValue, AllSettings.animation)
    }

    override fun onInit(): Window? {
        return window
    }

    fun interface OnCancelListener {
        fun onClick(): Boolean
    }
}
