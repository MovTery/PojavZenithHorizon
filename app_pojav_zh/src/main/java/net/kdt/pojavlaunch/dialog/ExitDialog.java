package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.Tools.shareLog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

import java.io.File;

public class ExitDialog extends Dialog {
    private final File crashReport, logFile;
    private final int code;
    public ExitDialog(@NonNull Context context, int code, File crash, File log) {
        super(context);

        this.crashReport = crash;
        this.logFile = log;
        this.code = code;

        setContentView(R.layout.dialog_exit);
        setCancelable(false);
        init(context);
    }

    private void init(Context context) {
        TextView mMessage = findViewById(R.id.zh_exit_message);
        Button mShareCrashReport = findViewById(R.id.zh_exit_share_crash_report);
        Button mShareLog = findViewById(R.id.zh_exit_share_log);
        Button mCancel = findViewById(R.id.zh_exit_cancel);

        mMessage.setText(context.getString(R.string.mcn_exit_title, this.code));
        mShareCrashReport.setVisibility((this.crashReport != null && this.crashReport.exists()) ? View.VISIBLE : View.GONE);
        mShareLog.setVisibility((this.logFile != null && this.logFile.exists()) ? View.VISIBLE : View.GONE);
        mCancel.setVisibility(View.VISIBLE);

        if (this.crashReport != null) mShareCrashReport.setOnClickListener(view -> {
            PojavZHTools.shareFile(getContext(), ExitDialog.this.crashReport.getName(), ExitDialog.this.crashReport.getAbsolutePath());
            ExitDialog.this.dismiss();
        });
        if (this.logFile != null) mShareLog.setOnClickListener(view -> {
            shareLog(context);
            ExitDialog.this.dismiss();
        });
    }
}
