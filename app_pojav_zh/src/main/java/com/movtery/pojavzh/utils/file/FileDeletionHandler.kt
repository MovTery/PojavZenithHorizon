package com.movtery.pojavzh.utils.file

import android.content.Context
import net.kdt.pojavlaunch.PojavApplication
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class FileDeletionHandler(
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

    private fun addDirectory(directory: File) {
        if (directory.isFile) addFile(directory)
        else if (directory.isDirectory) {
            directory.listFiles()?.forEach {
                if (it.isFile) addFile(it)
                else if (it.isDirectory) addDirectory(it)
            }
        }
    }

    override fun searchFilesToProcess() {
        mSelectedFiles.forEach {
            if (it.isFile) addFile(it)
            else if (it.isDirectory) addDirectory(it)
        }
        totalFileSize.set(fileSize.get())
    }

    override fun processFile() {
        foundFiles.parallelStream().forEach {
            fileSize.addAndGet(-FileUtils.sizeOf(it))
            fileCount.getAndDecrement()
            FileUtils.deleteQuietly(it)
        }
        //剩下的都是空文件夹，直接删除
        mSelectedFiles.forEach { FileUtils.deleteQuietly(it) }
    }

    override fun getCurrentFileCount() = fileCount.get()

    override fun getTotalSize() = totalFileSize.get()

    override fun getPendingSize() = fileSize.get()

    override fun onEnd() {
        PojavApplication.sExecutorService.execute(onEndRunnable)
    }
}