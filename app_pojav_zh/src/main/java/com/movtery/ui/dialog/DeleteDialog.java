package com.movtery.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteDialog extends TipDialog.Builder {
    private final Context context;
    private final Runnable runnable;
    private final File mFile;
    private final boolean isFolder;

    public DeleteDialog(@NonNull Context context, Runnable runnable, File file) {
        super(context);
        this.context = context;
        this.runnable = runnable;
        this.mFile = file;

        this.setCancelable(false);

        isFolder = mFile.isDirectory();
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        setTitle(isFolder ?
                context.getString(R.string.zh_file_delete_dir) :
                context.getString(R.string.zh_file_tips));
        setMessage(isFolder ?
                context.getString(R.string.zh_file_delete_dir_message) :
                context.getString(R.string.zh_file_delete));
        setConfirm(context.getString(R.string.global_delete));

        setConfirmClickListener(() -> {
            try {
                if (isFolder) {
                    FileUtils.deleteDirectory(mFile);
                } else {
                    FileUtils.deleteQuietly(mFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
        });
    }

    public void show() {
        buildDialog();
    }
}
