package com.movtery.pojavzh.ui.dialog;

import static com.movtery.pojavzh.utils.stringutils.StringUtils.markdownToHtml;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.UpdateLauncher;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;

public class UpdateDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final String versionName;
    private final String tagName;
    private final String createdTime;
    private final long fileSize;
    private final String description;

    public UpdateDialog(@NonNull Context context, UpdateInformation updateInformation) {
        super(context);
        this.versionName = updateInformation.versionName;
        this.tagName = updateInformation.tagName;
        this.createdTime = updateInformation.createdTime;
        this.fileSize = updateInformation.fileSize;
        this.description = updateInformation.description;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_update);
        init();
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        TextView mVersionName = findViewById(R.id.zh_update_version_name);
        TextView mCreatedTime = findViewById(R.id.zh_update_time);
        TextView mFileSize = findViewById(R.id.zh_update_file_size);
        WebView mDescription = findViewById(R.id.zh_update_description);

        String version = StringUtils.insertSpace(getContext().getString(R.string.zh_update_dialog_version), this.versionName);
        String time = StringUtils.insertSpace(getContext().getString(R.string.zh_update_dialog_time), this.createdTime);
        String size = StringUtils.insertSpace(getContext().getString(R.string.zh_update_dialog_file_size), FileTools.formatFileSize(this.fileSize));

        mVersionName.setText(version);
        mCreatedTime.setText(time);
        mFileSize.setText(size);

        String descriptionHtml = markdownToHtml(this.description);

        ZHTools.getWebViewAfterProcessing(mDescription);

        mDescription.getSettings().setJavaScriptEnabled(true);
        mDescription.loadDataWithBaseURL(null, descriptionHtml, "text/html", "UTF-8", null);

        Button mUpdateButton = findViewById(R.id.zh_update_update_button);
        Button mCancelButton = findViewById(R.id.zh_update_cancel_button);
        Button mIgnoreButton = findViewById(R.id.zh_update_ignore_button);

        mUpdateButton.setOnClickListener(view -> {
            this.dismiss();
            if (ZHTools.areaChecks()) {
                runOnUiThread(() -> {
                    UpdateSourceDialog updateSourceDialog = new UpdateSourceDialog(getContext(), versionName, tagName, fileSize);
                    updateSourceDialog.show();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show());
                UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), versionName, tagName, fileSize, UpdateLauncher.UpdateSource.GITHUB_RELEASE);
                updateLauncher.start();
            }
        });
        mCancelButton.setOnClickListener(view -> this.dismiss());
        mIgnoreButton.setOnClickListener(view -> {
            DEFAULT_PREF.edit().putString("ignoreUpdate", this.versionName).apply();
            this.dismiss();
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public static class UpdateInformation {
        public String versionName;
        public String tagName;
        public String createdTime;
        public long fileSize;
        public String description;

        public void information(@NonNull String versionName, @NonNull String tagName, @NonNull String createdTime, long fileSize, @NonNull String description) {
            this.versionName = versionName;
            this.tagName = tagName;
            this.createdTime = createdTime;
            this.fileSize = fileSize;
            this.description = description;
        }
    }
}
