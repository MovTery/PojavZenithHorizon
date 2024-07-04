package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class ProgressDialog extends FullScreenDialog {
    private TextView textView;
    private ProgressBar progressBar;

    public ProgressDialog(@NonNull Context context, @NonNull OnCancelListener listener) {
        super(context);
        this.setContentView(R.layout.dialog_progress);
        this.setCancelable(false);

        init(listener);
    }

    private void init(OnCancelListener listener) {
        this.textView = findViewById(R.id.zh_download_upload_textView);
        this.progressBar = findViewById(R.id.progressBar2);
        Button cancelButton = findViewById(R.id.zh_download_cancel_button);

        this.progressBar.setMax(1000);
        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                if (!listener.onClick()) return;
            }
            dismiss();
        });
    }

    public void updateText(String text) {
        if (text != null) this.textView.setText(text);
    }

    public void updateProgress(double progress, double total) {
        double doubleValue = progress / total * 1000;
        int intValue = (int) (doubleValue);

        if (doubleValue > 0) this.progressBar.setVisibility(View.VISIBLE);
        else this.progressBar.setVisibility(View.GONE);
        this.progressBar.setProgress(intValue, PREF_ANIMATION);
    }

    public interface OnCancelListener {
        boolean onClick();
    }
}
