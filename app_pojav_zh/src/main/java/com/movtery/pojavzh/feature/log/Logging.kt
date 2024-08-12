package com.movtery.pojavzh.feature.log

import android.util.Log
import androidx.annotation.Keep
import com.movtery.pojavzh.utils.PathAndUrlManager.Companion.DIR_GAME_HOME
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.file.FileTools
import net.kdt.pojavlaunch.Tools
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Keep
object Logging {
    @JvmField val DIR_LAUNCHER_LOG: File = getLogDir()
    @JvmField val FILE_LAUNCHER_LOG: File = getLogFile()
    @JvmField var FILE_LATEST_LAUNCHER_LOG: File? = null

    private val executor = Executors.newSingleThreadExecutor()

    private fun getLogDir(): File {
        val dir = File(DIR_GAME_HOME, "launcher_log")
        if (!dir.exists()) FileTools.mkdirs(dir)
        return dir
    }

    private fun getLogFile(): File {
        val logPrefix = "log"
        val logSuffix = ".txt"
        val maxLogIndex = 5

        val logFiles = DIR_LAUNCHER_LOG.listFiles { file ->
            file.isFile && file.name.startsWith(logPrefix) && file.name.endsWith(logSuffix)
        } ?: emptyArray()

        if (logFiles.isEmpty()) {
            return File(DIR_LAUNCHER_LOG, "${logPrefix}1$logSuffix")
        }

        val latestFile = logFiles.maxByOrNull { it.lastModified() } ?: return File(DIR_LAUNCHER_LOG, "${logPrefix}1$logSuffix")
        FILE_LATEST_LAUNCHER_LOG = latestFile

        val latestIndex: Int = latestFile.name.removePrefix(logPrefix).removeSuffix(logSuffix).toIntOrNull() ?: 0
        val nextIndex = (latestIndex % maxLogIndex) + 1

        val nextLogFileName = "$logPrefix$nextIndex$logSuffix"
        val file = File(DIR_LAUNCHER_LOG, nextLogFileName)
        if (file.exists()) file.delete()
        return file
    }

    private fun writeToFile(log: String, tag: Tag, mark: String) {
        val date = Date(ZHTools.getCurrentTimeMillis())
        val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        val logString = "($timeString) [${tag.tagName}] <$mark> $log"

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
        Log.v(Tag.VERBOSE.tagName, verbose)
        writeToFile(verbose, Tag.VERBOSE, mark)
    }

    @JvmStatic
    fun v(mark: String, verbose: String, throwable: Throwable) {
        Log.v(Tag.VERBOSE.tagName, verbose, throwable)
        writeToFile("$verbose\n${Tools.printToString(throwable)}", Tag.VERBOSE, mark)
    }

    @JvmStatic
    fun d(mark: String, debug: String) {
        Log.d(Tag.DEBUG.tagName, debug)
        writeToFile(debug, Tag.DEBUG, mark)
    }

    @JvmStatic
    fun d(mark: String, debug: String, throwable: Throwable) {
        Log.d(Tag.DEBUG.tagName, debug, throwable)
        writeToFile("$debug\n${Tools.printToString(throwable)}", Tag.DEBUG, mark)
    }

    @JvmStatic
    fun i(mark: String, info: String) {
        Log.i(Tag.INFO.tagName, info)
        writeToFile(info, Tag.INFO, mark)
    }

    @JvmStatic
    fun i(mark: String, info: String, throwable: Throwable) {
        Log.i(Tag.INFO.tagName, info, throwable)
        writeToFile("$info\n${Tools.printToString(throwable)}", Tag.INFO, mark)
    }

    @JvmStatic
    fun w(mark: String, warn: String) {
        Log.w(Tag.WARN.tagName, warn)
        writeToFile(warn, Tag.WARN, mark)
    }

    @JvmStatic
    fun w(mark: String, warn: String, throwable: Throwable) {
        Log.w(Tag.WARN.tagName, warn, throwable)
        writeToFile("$warn\n${Tools.printToString(throwable)}", Tag.WARN, mark)
    }

    @JvmStatic
    fun e(mark: String, error: String) {
        Log.e(Tag.ERROR.tagName, error)
        writeToFile(error, Tag.ERROR, mark)
    }

    @JvmStatic
    fun e(mark: String, error: String, throwable: Throwable) {
        Log.e(Tag.ERROR.tagName, error, throwable)
        writeToFile("$error\n${Tools.printToString(throwable)}", Tag.ERROR, mark)
    }

    enum class Tag(val tagName: String) {
        VERBOSE("VERBOSE"),
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR")
    }
}