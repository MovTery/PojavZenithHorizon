package com.movtery.pojavzh.ui.activity;

import static net.kdt.pojavlaunch.Tools.shareLog;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.file.FileTools;

import net.kdt.pojavlaunch.BaseActivity;
import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.io.File;

public class ErrorActivity extends BaseActivity {
    private static final String BUNDLE_IS_ERROR = "is_error";
    private static final String BUNDLE_CODE = "code";
    private static final String BUNDLE_CRASH_REPORTS_PATH = "crash_reports_path";
    private static final String BUNDLE_THROWABLE = "throwable";
    private static final String BUNDLE_SAVE_PATH = "save_path";

    private TextView mErrorText, mTitleText;
    private Button mConfirmButton, mRestartButton, mCopyButton, mShareButton;
    private Button mShareLogButton, mShareCrashReportButton;

    public static void showError(Context ctx, String savePath, Throwable th) {
        Intent intent = new Intent(ctx, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BUNDLE_THROWABLE, th);
        intent.putExtra(BUNDLE_SAVE_PATH, savePath);
        intent.putExtra(BUNDLE_IS_ERROR, true);
        ctx.startActivity(intent);
    }

    public static void showExitMessage(Context ctx, int code) {
        showExitMessage(ctx, code, new File(ZHTools.getGameDirPath(getCurrentProfile().gameDir), "crash-reports").getAbsolutePath());
    }

    public static void showExitMessage(Context ctx, int code, String crashReportsPath) {
        Intent intent = new Intent(ctx, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BUNDLE_CODE, code);
        intent.putExtra(BUNDLE_IS_ERROR, false);
        intent.putExtra(BUNDLE_CRASH_REPORTS_PATH, crashReportsPath);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        bindValues();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        mConfirmButton.setOnClickListener(v -> finish());
        mRestartButton.setOnClickListener(v -> startActivity(new Intent(ErrorActivity.this, LauncherActivity.class)));

        if (extras.getBoolean(BUNDLE_IS_ERROR, true)) {
            showError(extras);
        } else {
            //如果不是应用崩溃，那么这个页面就不允许截图
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            showCrash(extras);
        }
    }

    private void showCrash(Bundle extras) {
        int code = extras.getInt(BUNDLE_CODE, 0);
        if (code == 0) {
            finish();
            return;
        }

        findViewById(R.id.zh_error_buttons).setVisibility(View.GONE);
        mTitleText.setText(R.string.zh_wrong_tip);

        File crashReportFile = FileTools.getLatestFile(extras.getString(BUNDLE_CRASH_REPORTS_PATH), 15);
        File logFile = new File(Tools.DIR_GAME_HOME, "latestlog.txt");

        mErrorText.setText(getString(R.string.zh_game_exit_message, code));
        mErrorText.setTextSize(14f);
        mShareCrashReportButton.setVisibility((crashReportFile != null && crashReportFile.exists()) ? View.VISIBLE : View.GONE);
        mShareLogButton.setVisibility(logFile.exists() ? View.VISIBLE : View.GONE);

        if (crashReportFile != null)
            mShareCrashReportButton.setOnClickListener(view -> FileTools.shareFile(this, crashReportFile.getName(), crashReportFile.getAbsolutePath()));
        mShareLogButton.setOnClickListener(view -> shareLog(this));
    }

    private void showError(Bundle extras) {
        findViewById(R.id.zh_crash_buttons).setVisibility(View.GONE);

        Throwable throwable = (Throwable) extras.getSerializable(BUNDLE_THROWABLE);
        final String stackTrace = throwable != null ? Tools.printToString(throwable) : "<null>";
        String strSavePath = extras.getString(BUNDLE_SAVE_PATH);
        String errorText = strSavePath + " :\r\n\r\n" + stackTrace;

        mErrorText.setText(errorText);
        mCopyButton.setOnClickListener(v -> {
            ClipboardManager mgr = (ClipboardManager) ErrorActivity.this.getSystemService(CLIPBOARD_SERVICE);
            mgr.setPrimaryClip(ClipData.newPlainText("error", stackTrace));
        });
        File crashFile = new File(strSavePath);
        mShareButton.setOnClickListener(v -> FileTools.shareFile(this, crashFile.getName(), crashFile.getAbsolutePath()));
    }

    private void bindValues() {
        mErrorText = findViewById(R.id.zh_error_text);
        mTitleText = findViewById(R.id.zh_error_title);
        mConfirmButton = findViewById(R.id.zh_error_confirm);
        mRestartButton = findViewById(R.id.zh_error_restart);
        mCopyButton = findViewById(R.id.zh_error_copy);
        mShareButton = findViewById(R.id.zh_error_share);

        mShareLogButton = findViewById(R.id.zh_crash_share_log);
        mShareCrashReportButton = findViewById(R.id.zh_crash_share_crash_report);
    }
}
