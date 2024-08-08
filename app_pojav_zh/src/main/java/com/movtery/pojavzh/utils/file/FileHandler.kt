package com.movtery.pojavzh.utils.file

import android.content.Context
import com.movtery.pojavzh.ui.dialog.ProgressDialog
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Future

abstract class FileHandler(
    protected val context: Context,
) {
    private var currentTask: Future<*>? = null
    private var timer: Timer? = null

    protected fun start(progress: FileSearchProgress) {
        Tools.runOnUiThread {
            val dialog = ProgressDialog(context) {
                cancelTask()
                onEnd()
                true
            }
            dialog.updateText(context.getString(R.string.zh_file_operation_file, "0 B", "0 B", 0))

            currentTask = PojavApplication.sExecutorService.submit {
                Tools.runOnUiThread { dialog.show() }

                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        Tools.runOnUiThread {
                            val pendingSize = progress.getPendingSize()
                            val totalSize = progress.getTotalSize()
                            dialog.updateText(context.getString(
                                    R.string.zh_file_operation_file,
                                    FileTools.formatFileSize(pendingSize),
                                    FileTools.formatFileSize(totalSize),
                                    progress.getCurrentFileCount()))
                            dialog.updateProgress((totalSize - pendingSize).toDouble(), totalSize.toDouble())
                        }
                    }
                }, 0, 100)

                searchFilesToProcess()
                processFile()

                Tools.runOnUiThread { dialog.dismiss() }
                timer?.cancel()
                onEnd()
            }
        }
    }

    abstract fun searchFilesToProcess()

    abstract fun processFile()

    abstract fun onEnd()

    private fun cancelTask() {
        currentTask?.let {
            if (!currentTask!!.isDone) {
                currentTask?.cancel(true)
                timer?.let { timer?.cancel() }
            }
        }
    }
}