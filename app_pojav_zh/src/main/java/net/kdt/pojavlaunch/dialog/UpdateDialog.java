package net.kdt.pojavlaunch.dialog;


import static net.kdt.pojavlaunch.PojavZHTools.markdownToHtml;
import static net.kdt.pojavlaunch.PojavZHTools.updateLauncher;

import android.app.Dialog;
import android.content.Context;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class UpdateDialog extends Dialog {
    private final String versionName;
    private final String createdTime;
    private final String description;

    public UpdateDialog(@NonNull Context context, UpdateInformation updateInformation) {
        super(context);
        this.versionName = updateInformation.versionName;
        this.createdTime = updateInformation.createdTime;
        this.description = updateInformation.description;

        this.setCancelable(false);
        setContentView(R.layout.dialog_update);
        init();
    }
    private void init() {
        TextView mVersionName = findViewById(R.id.zh_update_version_name);
        TextView mCreatedTime = findViewById(R.id.zh_update_time);
        WebView mDescription = findViewById(R.id.zh_update_description);

        mVersionName.setText(this.versionName);
        mCreatedTime.setText(this.createdTime);

        String descriptionHtml = markdownToHtml(this.description);
        mDescription.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        Button mUpdateButton = findViewById(R.id.zh_update_update_button);
        Button mCancelButton = findViewById(R.id.zh_update_cancel_button);

        mUpdateButton.setOnClickListener(view -> {
            this.dismiss();
            updateLauncher(getContext());
        });
        mCancelButton.setOnClickListener(view -> this.dismiss());
    }

    public static class UpdateInformation {
        public String versionName;
        public String createdTime;
        public String description;
        public void information(@NonNull String versionName, @NonNull String createdTime, @NonNull String description) {
            this.versionName = versionName;
            this.createdTime = createdTime;
            this.description = description;
        }
    }
}
