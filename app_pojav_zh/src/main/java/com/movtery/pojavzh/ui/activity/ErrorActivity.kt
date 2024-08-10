package com.movtery.pojavzh.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.file.FileTools.Companion.getLatestFile
import com.movtery.pojavzh.utils.file.FileTools.Companion.shareFile
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.BaseActivity
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import java.io.File

class ErrorActivity : BaseActivity() {
    private var mErrorText: TextView? = null
    private var mTitleText: TextView? = null
    private var mConfirmButton: Button? = null
    private var mRestartButton: Button? = null
    private var mCopyButton: Button? = null
    private var mShareButton: Button? = null
    private var mShareLogButton: Button? = null
    private var mShareCrashReportButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        bindValues()

        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }

        mConfirmButton?.setOnClickListener { finish() }
        mRestartButton?.setOnClickListener {
            startActivity(Intent(this@ErrorActivity, LauncherActivity::class.java))
        }

        if (extras.getBoolean(BUNDLE_IS_ERROR, true)) {
            showError(extras)
        } else {
            //如果不是应用崩溃，那么这个页面就不允许截图
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showCrash(extras)
        }
    }

    private fun showCrash(extras: Bundle) {
        val code = extras.getInt(BUNDLE_CODE, 0)
        if (code == 0) {
            finish()
            return
        }

        findViewById<View>(R.id.zh_error_buttons).visibility = View.GONE
        mTitleText?.setText(R.string.zh_wrong_tip)

        val crashReportFile = getLatestFile(extras.getString(BUNDLE_CRASH_REPORTS_PATH), 15)
        val logFile = File(PathAndUrlManager.DIR_GAME_HOME, "latestlog.txt")

        mErrorText?.text = getString(R.string.zh_game_exit_message, code)
        mErrorText?.textSize = 14f
        mShareCrashReportButton?.visibility =
            if ((crashReportFile != null && crashReportFile.exists())) View.VISIBLE else View.GONE
        mShareLogButton?.visibility = if (logFile.exists()) View.VISIBLE else View.GONE

        if (crashReportFile != null) mShareCrashReportButton?.setOnClickListener {
            shareFile(
                this, crashReportFile.name, crashReportFile.absolutePath
            )
        }
        mShareLogButton?.setOnClickListener { Tools.shareLog(this) }
    }

    private fun showError(extras: Bundle) {
        findViewById<View>(R.id.zh_crash_buttons).visibility = View.GONE

        val throwable = extras.getSerializable(BUNDLE_THROWABLE) as Throwable?
        val stackTrace = if (throwable != null) Tools.printToString(throwable) else "<null>"
        val strSavePath = extras.getString(BUNDLE_SAVE_PATH)
        val errorText = "$strSavePath :\r\n\r\n$stackTrace"

        mErrorText?.text = errorText
        mCopyButton?.setOnClickListener { StringUtils.copyText("error", stackTrace, this@ErrorActivity) }
        strSavePath?.let{
            val crashFile = File(strSavePath)
            mShareButton?.setOnClickListener {
                shareFile(this, crashFile.name, crashFile.absolutePath)
            }
        }
    }

    private fun bindValues() {
        mErrorText = findViewById(R.id.zh_error_text)
        mTitleText = findViewById(R.id.zh_error_title)
        mConfirmButton = findViewById(R.id.zh_error_confirm)
        mRestartButton = findViewById(R.id.zh_error_restart)
        mCopyButton = findViewById(R.id.zh_error_copy)
        mShareButton = findViewById(R.id.zh_error_share)

        mShareLogButton = findViewById(R.id.zh_crash_share_log)
        mShareCrashReportButton = findViewById(R.id.zh_crash_share_crash_report)
    }

    companion object {
        private const val BUNDLE_IS_ERROR = "is_error"
        private const val BUNDLE_CODE = "code"
        private const val BUNDLE_CRASH_REPORTS_PATH = "crash_reports_path"
        private const val BUNDLE_THROWABLE = "throwable"
        private const val BUNDLE_SAVE_PATH = "save_path"

        @JvmStatic
        fun showError(ctx: Context, savePath: String?, th: Throwable?) {
            val intent = Intent(ctx, ErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(BUNDLE_THROWABLE, th)
            intent.putExtra(BUNDLE_SAVE_PATH, savePath)
            intent.putExtra(BUNDLE_IS_ERROR, true)
            ctx.startActivity(intent)
        }

        @JvmStatic
        @JvmOverloads
        fun showExitMessage(
            ctx: Context,
            code: Int,
            crashReportsPath: String? = File(ZHTools.getGameDirPath(LauncherProfiles.getCurrentProfile().gameDir), "crash-reports").absolutePath
        ) {
            val intent = Intent(ctx, ErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(BUNDLE_CODE, code)
            intent.putExtra(BUNDLE_IS_ERROR, false)
            intent.putExtra(BUNDLE_CRASH_REPORTS_PATH, crashReportsPath)
            ctx.startActivity(intent)
        }
    }
}
