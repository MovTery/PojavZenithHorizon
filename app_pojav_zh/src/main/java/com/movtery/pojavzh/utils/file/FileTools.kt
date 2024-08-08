package com.movtery.pojavzh.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.EditText
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.dialog.EditTextDialog.ConfirmListener
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FilenameFilter
import java.io.InputStream
import java.security.MessageDigest

class FileTools {
    companion object {
        @JvmStatic
        fun mkdir(dir: File): Boolean {
            return dir.mkdir()
        }

        @JvmStatic
        fun mkdirs(dir: File): Boolean {
            return dir.mkdirs()
        }

        @JvmStatic
        fun copyFileInBackground(context: Context, fileUri: Uri?, rootPath: String?): File {
            val fileName = Tools.getFileName(context, fileUri)
            val outputFile = File(rootPath, fileName)
            runCatching {
                context.contentResolver.openInputStream(fileUri!!).use { inputStream ->
                    FileUtils.copyInputStreamToFile(inputStream, outputFile)
                }
            }.getOrElse { e ->
                throw RuntimeException(e)
            }

            return outputFile
        }

        @JvmStatic
        fun getLatestFile(folderPath: String?, modifyTime: Int): File? {
            if (folderPath == null) return null
            return getLatestFile(File(folderPath), modifyTime.toLong())
        }

        @JvmStatic
        fun getLatestFile(folder: File?, modifyTime: Long): File? {
            if (folder == null || !folder.isDirectory) {
                return null
            }

            val files = folder.listFiles(FilenameFilter { dir: File?, name: String ->
                !name.startsWith(
                    "."
                )
            })
            if (files == null || files.isEmpty()) {
                return null
            }

            val fileList: List<File> = listOf(*files)
            fileList.sortedWith(Comparator.comparingLong { obj: File -> obj.lastModified() }
                .reversed())

            if (modifyTime > 0) {
                val difference =
                    (ZHTools.getCurrentTimeMillis() - fileList[0].lastModified()) / 1000 //转换为秒
                if (difference >= modifyTime) {
                    return null
                }
            }

            return fileList[0]
        }

        @JvmStatic
        fun shareFile(context: Context, fileName: String?, filePath: String?) {
            val contentUri = DocumentsContract.buildDocumentUri(
                context.getString(R.string.storageProviderAuthorities),
                filePath
            )

            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.setType("text/plain")

            val sendIntent = Intent.createChooser(shareIntent, fileName)
            context.startActivity(sendIntent)
        }

        @JvmStatic
        @SuppressLint("UseCompatLoadingForDrawables")
        fun renameFileListener(context: Context, runnable: Runnable?, file: File, suffix: String) {
            val fileParent = file.parent
            val fileName = file.name

            EditTextDialog.Builder(context)
                .setTitle(R.string.zh_rename)
                .setEditText(getFileNameWithoutExtension(fileName, suffix))
                .setConfirmListener(ConfirmListener { editBox: EditText ->
                    val newName = editBox.text.toString().replace("/", "")
                    if (fileName == newName) {
                        return@ConfirmListener true
                    }

                    if (newName.isEmpty()) {
                        editBox.error = context.getString(R.string.zh_file_rename_empty)
                        return@ConfirmListener false
                    }

                    val newFile = File(fileParent, newName + suffix)
                    if (newFile.exists()) {
                        editBox.error = context.getString(R.string.zh_file_rename_exitis)
                        return@ConfirmListener false
                    }

                    val renamed = file.renameTo(newFile)
                    if (renamed) {
                        if (runnable != null) PojavApplication.sExecutorService.execute(runnable)
                    }
                    true
                }).buildDialog()
        }

        @JvmStatic
        @SuppressLint("UseCompatLoadingForDrawables")
        fun renameFileListener(context: Context, runnable: Runnable?, file: File) {
            val fileParent = file.parent
            val fileName = file.name

            EditTextDialog.Builder(context)
                .setTitle(R.string.zh_rename)
                .setEditText(fileName)
                .setConfirmListener(ConfirmListener { editBox: EditText ->
                    val newName = editBox.text.toString().replace("/", "")
                    if (fileName == newName) {
                        return@ConfirmListener true
                    }

                    if (newName.isEmpty()) {
                        editBox.error = context.getString(R.string.zh_file_rename_empty)
                        return@ConfirmListener false
                    }

                    val newFile = File(fileParent, newName)
                    if (newFile.exists()) {
                        editBox.error = context.getString(R.string.zh_file_rename_exitis)
                        return@ConfirmListener false
                    }

                    val renamed = renameFile(file, newFile)
                    if (renamed) {
                        if (runnable != null) PojavApplication.sExecutorService.execute(runnable)
                    }
                    true
                }).buildDialog()
        }

        @JvmStatic
        fun renameFile(origin: File, target: File): Boolean {
            return origin.renameTo(target)
        }

        @JvmStatic
        fun copyFile(file :File, target: File) {
            if (file.isFile) FileUtils.copyFile(file, target)
            else if (file.isDirectory) FileUtils.copyDirectory(file, target)
        }

        @JvmStatic
        fun moveFile(file :File, target: File) {
            if (file.isFile) FileUtils.moveFile(file, target)
            else if (file.isDirectory) FileUtils.moveDirectory(file, target)
        }

        @JvmStatic
        fun getFileNameWithoutExtension(fileName: String, fileExtension: String?): String {
            val dotIndex = if (fileExtension == null) {
                fileName.lastIndexOf('.')
            } else {
                fileName.lastIndexOf(fileExtension)
            }
            return if (dotIndex == -1) fileName else fileName.substring(0, dotIndex)
        }

        @JvmStatic
        @SuppressLint("DefaultLocale")
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"

            val units = arrayOf("B", "KB", "MB", "GB")
            var unitIndex = 0
            var value = bytes.toDouble()
            //循环获取合适的单位
            while (value >= 1024 && unitIndex < units.size - 1) {
                value /= 1024.0
                unitIndex++
            }
            return String.format("%.2f %s", value, units[unitIndex])
        }

        @JvmStatic
        fun getFileHashMD5(inputStream: InputStream): String {
            runCatching {
                val md = MessageDigest.getInstance("MD5")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    md.update(buffer, 0, bytesRead)
                }
                val hash = md.digest()

                //将哈希值转换为十六进制字符串
                val hexString = StringBuilder()
                for (b in hash) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }

                return hexString.toString()
            }.getOrElse { e ->
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun getFileHashSHA1(inputStream: InputStream): String {
            runCatching {
                val md = MessageDigest.getInstance("SHA-1")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    md.update(buffer, 0, bytesRead)
                }
                val hash = md.digest()

                //将哈希值转换为十六进制字符串
                val hexString = StringBuilder()
                for (b in hash) {
                    val hex = String.format("%02x", b)
                    hexString.append(hex)
                }

                return hexString.toString()
            }.getOrElse { e ->
                throw RuntimeException(e)
            }
        }
    }
}
