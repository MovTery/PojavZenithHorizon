package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.UpdateLauncher;

import net.kdt.pojavlaunch.R;

public class UpdateSourceDialog extends FullScreenDialog {
    private final String tagName;
    private final long fileSize;

    public UpdateSourceDialog(@NonNull Context context, String tagName, long fileSize) {
        super(context);

        this.tagName = tagName;
        this.fileSize = fileSize;

        this.setCancelable(true);
        this.setContentView(R.layout.dialog_update_source);

        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        Button mGithubRelease = findViewById(R.id.zh_update_github_release);
        Button mGhproxy = findViewById(R.id.zh_update_ghproxy);

        mGithubRelease.setOnClickListener(view -> {
            runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show());
            UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), tagName, fileSize, UpdateLauncher.UpdateSource.GITHUB_RELEASE);
            updateLauncher.start();
            UpdateSourceDialog.this.dismiss();
        });
        mGhproxy.setOnClickListener(view -> {
            runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_update_downloading_tip, getContext().getString(R.string.zh_update_update_source_ghproxy)), Toast.LENGTH_SHORT).show());
            UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), tagName, fileSize, UpdateLauncher.UpdateSource.GHPROXY);
            updateLauncher.start();
            UpdateSourceDialog.this.dismiss();
        });
    }
}
