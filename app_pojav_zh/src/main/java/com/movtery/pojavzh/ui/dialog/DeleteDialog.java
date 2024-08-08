package com.movtery.pojavzh.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.utils.file.FileDeletionHandler;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.List;

public class DeleteDialog extends TipDialog.Builder {
    private final Context context;
    private final Runnable runnable;

    public DeleteDialog(@NonNull Context context, Runnable runnable, List<File> files) {
        super(context);
        this.context = context;
        this.runnable = runnable;
        init(files);
    }

    private void init(List<File> files) {
        this.setCancelable(false);

        boolean singleFile = files.size() == 1;
        File file = files.get(0);
        boolean isFolder = file.isDirectory();
        setTitle(singleFile ? (isFolder ?
                R.string.zh_file_delete_dir :
                R.string.zh_file_tips) : R.string.zh_file_delete_multiple_items_title);
        setMessage(singleFile ? (isFolder ?
                R.string.zh_file_delete_dir_message :
                R.string.zh_file_delete) : R.string.zh_file_delete_multiple_items_message);
        setConfirm(R.string.global_delete);

        setConfirmClickListener(() -> new FileDeletionHandler(context, files, runnable).start());
    }

    public void show() {
        buildDialog();
    }
}
