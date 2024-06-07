package com.movtery.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class DeleteDialog extends TipDialog.Builder {
    private Context context;
    private Runnable runnable;

    public DeleteDialog(@NonNull Context context, Runnable runnable, List<File> file) {
        super(context);
        init(context, runnable);
        multiSelectMode(file);
    }

    public DeleteDialog(@NonNull Context context, Runnable runnable, File file) {
        super(context);
        init(context, runnable);
        singleChoiceMode(file);
    }

    private void init(Context context, Runnable runnable) {
        this.context = context;
        this.runnable = runnable;
        this.setCancelable(false);
    }

    private void multiSelectMode(List<File> files) {
        setTitle(R.string.zh_file_delete_multiple_items_title);
        setMessage(R.string.zh_file_delete_multiple_items_message);
        setConfirm(R.string.global_delete);

        setConfirmClickListener(() -> {
            files.forEach(FileUtils::deleteQuietly);
            if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
        });
    }

    private void singleChoiceMode(File file) {
        boolean isFolder = file.isDirectory();
        setTitle(isFolder ?
                context.getString(R.string.zh_file_delete_dir) :
                context.getString(R.string.zh_file_tips));
        setMessage(isFolder ?
                context.getString(R.string.zh_file_delete_dir_message) :
                context.getString(R.string.zh_file_delete));
        setConfirm(R.string.global_delete);

        setConfirmClickListener(() -> {
            FileUtils.deleteQuietly(file);
            if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
        });
    }

    public void show() {
        buildDialog();
    }
}
