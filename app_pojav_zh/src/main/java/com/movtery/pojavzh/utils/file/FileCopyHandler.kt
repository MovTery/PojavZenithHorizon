package com.movtery.pojavzh.utils.file

import android.content.Context
import com.movtery.pojavzh.utils.file.FileTools.getFileNameWithoutExtension
import net.kdt.pojavlaunch.PojavApplication
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class FileCopyHandler(
    mContext: Context,
    private val mPasteType: PasteFile.PasteType,
    private val mSelectedFiles: List<File>,
    private val mRoot: File,
    private val mTarget: File,
    private val mFileExtensionGetter: FileExtensionGetter?,
    private val onEndRunnable: Runnable?
) : FileHandler(mContext), FileSearchProgress {
    private val foundFiles: MutableMap<File, File> = HashMap()
    private val totalFileSize = AtomicLong(0)
    private val fileSize = AtomicLong(0)
    private val fileCount = AtomicLong(0)

    fun start() {
        super.start(this)
    }

    private fun addFile(file: File) {
        fileCount.addAndGet(1)
        fileSize.addAndGet(FileUtils.sizeOf(file))
        //当前文件 - 目标文件
        foundFiles [file] = getNewDestination(file, getTargetFile(file), mFileExtensionGetter?.onGet(file))
    }

    private fun addDirectory(file: File) {
        if (file.isFile) addFile(file)
        else if (file.isDirectory) {
            val files = file.listFiles()
            files?.let {
                if (files.isEmpty()) addFile(file)
                else files.forEach {
                    if (it.isFile) addFile(it)
                    else if (it.isDirectory) addDirectory(it)
                }
            }
        }
    }

    private fun getTargetFile(file: File): File {
        return File(file.absolutePath.replace(mRoot.absolutePath, mTarget.absolutePath).replace(file.name, ""))
    }

    //如果目标地点已存在同名文件，就将目标文件的文件名加上数字标识，防止文件被覆盖
    private fun getNewDestination(sourceFile: File, targetDir: File, fileExtension: String?): File {
        var extension: String? = fileExtension
        var destFile = File(targetDir, sourceFile.name)
        if (destFile.exists()) {
            val fileNameWithoutExt = getFileNameWithoutExtension(sourceFile.name, extension)
            if (extension == null) {
                val dotIndex = sourceFile.name.lastIndexOf('.')
                extension = if (dotIndex == -1) "" else sourceFile.name.substring(dotIndex)
            }
            var proposedFileName: String
            var counter = 1
            while (destFile.exists()) {
                proposedFileName = "$fileNameWithoutExt ($counter)$extension"
                destFile = File(targetDir, proposedFileName)
                counter++
            }
        }
        return destFile
    }

    override fun searchFilesToProcess() {
        mSelectedFiles.forEach {
            if (it.isFile) addFile(it)
            else if (it.isDirectory) addDirectory(it)
        }
        totalFileSize.set(fileSize.get())
    }

    override fun processFile() {
        foundFiles.forEach { (currentFile, targetFile) ->
            targetFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
            when (mPasteType) {
                PasteFile.PasteType.COPY -> FileTools.copyFile(currentFile, targetFile)
                else -> FileTools.moveFile(currentFile, targetFile)
            }

            fileSize.addAndGet(-FileUtils.sizeOf(currentFile))
            fileCount.getAndDecrement()
        }
    }

    override fun getCurrentFileCount(): Long {
        return fileCount.get()
    }

    override fun getTotalSize(): Long {
        return totalFileSize.get()
    }

    override fun getPendingSize(): Long {
        return fileSize.get()
    }

    override fun onEnd() {
        PojavApplication.sExecutorService.execute(onEndRunnable)
    }

    interface FileExtensionGetter {
        fun onGet(file: File?): String?
    }
}