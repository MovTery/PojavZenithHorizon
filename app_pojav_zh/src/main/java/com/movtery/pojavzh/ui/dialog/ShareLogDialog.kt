package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.shareLog;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class ShareLogDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final File logFile;
    private final String message;

    public ShareLogDialog(@NonNull Context context, @NonNull File log) {
        super(context);

        this.message = StringUtils.insertSpace(context.getString(R.string.zh_main_share_log_tip), (log.exists() ? log.getAbsolutePath() : context.getString(R.string.zh_file_does_not_exist)));
        this.logFile = log;

        setContentView(R.layout.dialog_share_log);
        setCancelable(false);
        init(context);
        DraggableDialog.initDialog(this);
    }

    private void init(Context context) {
        TextView mMessage = findViewById(R.id.zh_share_log_message);
        Button mConfirm = findViewById(R.id.zh_share_log_confirm);
        Button mCancel = findViewById(R.id.zh_share_log_cancel);

        mMessage.setText(this.message);
        mConfirm.setVisibility((this.logFile.exists()) ? View.VISIBLE : View.GONE);
        mCancel.setVisibility(View.VISIBLE);

        mCancel.setOnClickListener(view -> ShareLogDialog.this.dismiss());
        if (this.logFile.exists()) mConfirm.setOnClickListener(view -> {
            shareLog(context);
            ShareLogDialog.this.dismiss();
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
