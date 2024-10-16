package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.UpdateLauncher;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogUpdateSourceBinding;

public class UpdateSourceDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogUpdateSourceBinding binding = DialogUpdateSourceBinding.inflate(getLayoutInflater());
    private final String versionName, tagName;
    private final long fileSize;

    public UpdateSourceDialog(@NonNull Context context, String versionName, String tagName, long fileSize) {
        super(context);

        this.versionName = versionName;
        this.tagName = tagName;
        this.fileSize = fileSize;

        this.setCancelable(true);
        this.setContentView(binding.getRoot());

        init();
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        binding.githubRelease.setOnClickListener(view -> {
            runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show());
            UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), versionName, tagName, fileSize, UpdateLauncher.UpdateSource.GITHUB_RELEASE);
            updateLauncher.start();
            UpdateSourceDialog.this.dismiss();
        });
        binding.ghproxy.setOnClickListener(view -> {
            runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.update_downloading_tip, getContext().getString(R.string.update_update_source_ghproxy)), Toast.LENGTH_SHORT).show());
            UpdateLauncher updateLauncher = new UpdateLauncher(getContext(), versionName, tagName, fileSize, UpdateLauncher.UpdateSource.GHPROXY);
            updateLauncher.start();
            UpdateSourceDialog.this.dismiss();
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
