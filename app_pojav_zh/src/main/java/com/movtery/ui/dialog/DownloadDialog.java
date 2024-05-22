package com.movtery.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class DownloadDialog extends Dialog {
    private TextView textView;
    private Button cancelButton;

    public DownloadDialog(@NonNull Context context) {
        super(context);
        this.setContentView(R.layout.dialog_download);
        this.setCancelable(false);

        init();
    }

    private void init() {
        this.textView = findViewById(R.id.zh_download_upload_textView);
        this.cancelButton = findViewById(R.id.zh_download_cancel_button);
    }

    public TextView getTextView() {
        return this.textView;
    }

    public Button getCancelButton() {
        return this.cancelButton;
    }
}
