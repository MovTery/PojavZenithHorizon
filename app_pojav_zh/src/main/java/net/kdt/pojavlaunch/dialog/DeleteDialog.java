package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteDialog extends Dialog {
    private final Runnable runnable;
    private final File mFile;
    private final boolean isFolder;

    public DeleteDialog(@NonNull Context context, Runnable runnable, File file) {
        super(context);
        this.runnable = runnable;
        this.mFile = file;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_delete);

        isFolder = mFile.isDirectory();
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        String fileName = mFile.getName();
        TextView mTitle = findViewById(R.id.zh_delete_title);
        TextView mMessage = findViewById(R.id.zh_delete_message);
        Button mCancelButton = findViewById(R.id.zh_delete_cancel);
        Button mDeleteButton = findViewById(R.id.zh_delete_confirm);

        mTitle.setText(isFolder ?
                getContext().getString(R.string.zh_file_delete_dir) :
                getContext().getString(R.string.zh_file_tips));
        mMessage.setText(isFolder ?
                getContext().getString(R.string.zh_file_delete_dir_message) :
                getContext().getString(R.string.zh_file_delete));

        mCancelButton.setOnClickListener(view -> DeleteDialog.this.dismiss());
        mDeleteButton.setOnClickListener(view -> {
            try {
                if (isFolder) {
                    FileUtils.deleteDirectory(mFile);
                    runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_file_delete_dir_success) + "\n" + fileName, Toast.LENGTH_LONG).show());
                } else {
                    FileUtils.deleteQuietly(mFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (runnable != null) PojavApplication.sExecutorService.execute(runnable);

            DeleteDialog.this.dismiss();
        });
    }
}
