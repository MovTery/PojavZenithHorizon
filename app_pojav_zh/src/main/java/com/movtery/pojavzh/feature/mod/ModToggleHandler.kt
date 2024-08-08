package com.movtery.pojavzh.feature.mod

import android.content.Context
import com.movtery.pojavzh.utils.file.FileHandler
import com.movtery.pojavzh.utils.file.FileSearchProgress
import net.kdt.pojavlaunch.PojavApplication
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class ModToggleHandler(
    mContext: Context,
    private val mSelectedFiles: List<File>,
    private val onEndRunnable: Runnable?
) : FileHandler(mContext), FileSearchProgress {
    private val foundFiles = mutableListOf<File>()
    private val totalFileSize = AtomicLong(0)
    private val fileSize = AtomicLong(0)
    private val fileCount = AtomicLong(0)

    fun start() {
        super.start(this)
    }

    private fun addFile(file: File) {
        foundFiles.add(file)
        fileCount.addAndGet(1)
        fileSize.addAndGet(FileUtils.sizeOf(file))
    }

    override fun searchFilesToProcess() {
        mSelectedFiles.forEach {
            if (it.isFile) addFile(it)
        }
        totalFileSize.set(fileSize.get())
    }

    override fun processFile() {
        (foundFiles).forEach {
            fileSize.addAndGet(-FileUtils.sizeOf(it))
            fileCount.getAndDecrement()

            val fileName = it.name
            if (fileName.endsWith(ModUtils.JAR_FILE_SUFFIX)) {
                ModUtils.disableMod(it)
            } else if (fileName.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) {
                ModUtils.enableMod(it)
            }
        }
    }

    override fun getCurrentFileCount() = fileCount.get()

    override fun getTotalSize() = totalFileSize.get()

    override fun getPendingSize() = fileSize.get()

    override fun onEnd() {
        PojavApplication.sExecutorService.execute(onEndRunnable)
    }
}