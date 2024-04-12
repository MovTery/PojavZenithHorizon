package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.Tools.shareLog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class ShareLogDialog extends Dialog {
    private final File logFile;
    private final String message;

    public ShareLogDialog(@NonNull Context context, @NonNull File log) {
        super(context);

        this.message = context.getString(R.string.zh_main_share_log_tip) + (
                log.exists() ? log.getAbsolutePath() : context.getString(R.string.zh_file_does_not_exist));
        this.logFile = log;

        setContentView(R.layout.dialog_exit);
        setCancelable(false);
        init(context);
    }

    private void init(Context context) {
        TextView mTitle = findViewById(R.id.zh_exit_title);
        TextView mMessage = findViewById(R.id.zh_exit_message);
        Button mShareCrashReport = findViewById(R.id.zh_exit_share_crash_report);
        Button mShareLog = findViewById(R.id.zh_exit_share_log);
        Button mCancel = findViewById(R.id.zh_exit_cancel);

        mTitle.setText(context.getString(R.string.main_share_logs));
        mMessage.setText(this.message);
        mShareCrashReport.setVisibility(View.GONE);
        mShareLog.setVisibility((this.logFile.exists()) ? View.VISIBLE : View.GONE);
        mCancel.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(view -> ShareLogDialog.this.dismiss());
        if (this.logFile.exists()) mShareLog.setOnClickListener(view -> {
            shareLog(context);
            ShareLogDialog.this.dismiss();
        });
    }
}
