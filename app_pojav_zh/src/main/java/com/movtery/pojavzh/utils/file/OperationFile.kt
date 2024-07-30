package com.movtery.pojavzh.utils.file

import android.content.Context
import com.movtery.pojavzh.ui.dialog.ProgressDialog
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

class OperationFile(
    private val context: Context,
    private val runnable: Runnable?,
    private val operationFileFunction: OperationFileFunction
) {
    private var currentTask: Future<*>? = null
    private var timer: Timer? = null

    fun operationFile(selectedFiles: List<File>) {
        Tools.runOnUiThread {
            val dialog = ProgressDialog(
                context
            ) {
                cancelTask()
                true
            }
            dialog.updateText(context.getString(R.string.zh_file_operation_file, "0B", "0B", 0))

            val totalFileSize = AtomicLong(0)
            val fileSize = AtomicLong(0)
            val fileCount = AtomicLong(0)
            currentTask = PojavApplication.sExecutorService.submit {
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {
                        Tools.runOnUiThread {
                            dialog.updateText(
                                context.getString(
                                    R.string.zh_file_operation_file,
                                    FileTools.formatFileSize(fileSize.get()),
                                    FileTools.formatFileSize(totalFileSize.get()),
                                    fileCount.get()
                                )
                            )
                        }
                    }
                }, 0, 80)

                Tools.runOnUiThread { dialog.show() }

                val preDeleteFiles: MutableList<File> = ArrayList()
                selectedFiles.forEach(Consumer cancel_1@{ selectedFile: File ->
                    if (currentTask!!.isCancelled) {
                        return@cancel_1
                    }
                    fileSize.addAndGet(FileUtils.sizeOf(selectedFile))

                    if (selectedFile.isDirectory) {
                        val allFiles = FileUtils.listFiles(selectedFile, null, true)
                        allFiles.forEach(Consumer cancel_2@{ file1: File ->
                            if (currentTask!!.isCancelled) {
                                return@cancel_2
                            }
                            fileCount.addAndGet(1)
                            preDeleteFiles.add(file1)
                        })
                    }

                    fileCount.addAndGet(1)
                    preDeleteFiles.add(selectedFile)
                })
                totalFileSize.set(fileSize.get())

                preDeleteFiles.forEach(Consumer cancel_3@{ file: File? ->
                    if (currentTask!!.isCancelled) {
                        return@cancel_3
                    }
                    fileSize.addAndGet(-FileUtils.sizeOf(file))
                    fileCount.getAndDecrement()
                    operationFileFunction.operationFile(file)
                })
                Tools.runOnUiThread { dialog.dismiss() }
                timer!!.cancel()
                finish()
            }
        }
    }

    private fun cancelTask() {
        if (currentTask != null && !currentTask!!.isDone) {
            currentTask!!.cancel(true)
            if (timer != null) {
                timer!!.cancel()
            }
            finish()
        }
    }

    private fun finish() {
        if (runnable != null) {
            PojavApplication.sExecutorService.execute(runnable)
        }
    }

    interface OperationFileFunction {
        fun operationFile(file: File?)
    }
}
