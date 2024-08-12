package com.movtery.pojavzh.feature.log

import android.util.Log
import com.movtery.pojavzh.utils.PathAndUrlManager.Companion.DIR_LAUNCHER_LOG
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.Tools
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * 启动器日志记录，将软件日志及时写入本地文件存储
 */
object Logging {
    private val executor = Executors.newSingleThreadExecutor()
    private var FILE_LAUNCHER_LOG: File? = null

    init {
        FILE_LAUNCHER_LOG = getLogFile()
    }

    private fun getLogFile(): File {
        val logPrefix = "log"
        val logSuffix = ".txt"
        val maxLogIndex = 10

        val launcherLogDir = File(DIR_LAUNCHER_LOG!!)

        val logFiles = launcherLogDir.listFiles { file ->
            file.isFile && file.name.startsWith(logPrefix) && file.name.endsWith(logSuffix)
        } ?: emptyArray()

        if (logFiles.isEmpty()) {
            return File(launcherLogDir, "${logPrefix}1$logSuffix")
        }

        val latestFile = logFiles.maxByOrNull { it.lastModified() } ?: return File(launcherLogDir, "${logPrefix}1$logSuffix")
        val latestIndex: Int = latestFile.name.removePrefix(logPrefix).removeSuffix(logSuffix).toIntOrNull() ?: 0
        val nextIndex = (latestIndex % maxLogIndex) + 1

        val nextLogFileName = "$logPrefix$nextIndex$logSuffix"
        val file = File(launcherLogDir, nextLogFileName)
        if (file.exists()) file.delete()
        return file
    }

    private fun writeToFile(log: String, tag: Tag, mark: String) {
        val date = Date(ZHTools.getCurrentTimeMillis())
        val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        val logString = "($timeString) [${tag.name}] <$mark> $log"

        executor.execute {
            runCatching {
                BufferedWriter(FileWriter(FILE_LAUNCHER_LOG, true)).use { writer ->
                    writer.append(logString).append("\n")
                }
            }.getOrElse { e ->
                Log.e("Logging", "Failed to write log: ${Tools.printToString(e)}")
            }
        }
    }

    @JvmStatic
    fun v(mark: String, verbose: String) {
        Log.v(Tag.VERBOSE.name, verbose)
        writeToFile(verbose, Tag.VERBOSE, mark)
    }

    @JvmStatic
    fun v(mark: String, verbose: String, throwable: Throwable) {
        Log.v(Tag.VERBOSE.name, verbose, throwable)
        writeToFile("$verbose\n${Tools.printToString(throwable)}", Tag.VERBOSE, mark)
    }

    @JvmStatic
    fun d(mark: String, debug: String) {
        Log.d(Tag.DEBUG.name, debug)
        writeToFile(debug, Tag.DEBUG, mark)
    }

    @JvmStatic
    fun d(mark: String, debug: String, throwable: Throwable) {
        Log.d(Tag.DEBUG.name, debug, throwable)
        writeToFile("$debug\n${Tools.printToString(throwable)}", Tag.DEBUG, mark)
    }

    @JvmStatic
    fun i(mark: String, info: String) {
        Log.i(Tag.INFO.name, info)
        writeToFile(info, Tag.INFO, mark)
    }

    @JvmStatic
    fun i(mark: String, info: String, throwable: Throwable) {
        Log.i(Tag.INFO.name, info, throwable)
        writeToFile("$info\n${Tools.printToString(throwable)}", Tag.INFO, mark)
    }

    @JvmStatic
    fun w(mark: String, warn: String) {
        Log.w(Tag.WARN.name, warn)
        writeToFile(warn, Tag.WARN, mark)
    }

    @JvmStatic
    fun w(mark: String, warn: String, throwable: Throwable) {
        Log.w(Tag.WARN.name, warn, throwable)
        writeToFile("$warn\n${Tools.printToString(throwable)}", Tag.WARN, mark)
    }

    @JvmStatic
    fun e(mark: String, error: String) {
        Log.e(Tag.ERROR.name, error)
        writeToFile(error, Tag.ERROR, mark)
    }

    @JvmStatic
    fun e(mark: String, error: String, throwable: Throwable) {
        Log.e(Tag.ERROR.name, error, throwable)
        writeToFile("$error\n${Tools.printToString(throwable)}", Tag.ERROR, mark)
    }

    enum class Tag { VERBOSE, DEBUG, INFO, WARN, ERROR }
}