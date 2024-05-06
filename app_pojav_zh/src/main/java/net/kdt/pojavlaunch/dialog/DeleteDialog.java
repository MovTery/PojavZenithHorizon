package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteDialog extends Dialog {
    private final FileListView mFileListView;
    private final File mFile;
    private final boolean isFolder;

    public DeleteDialog(@NonNull Context context, FileListView fileListView, File file) {
        super(context);
        this.mFileListView = fileListView;
        this.mFile = file;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_operation);

        isFolder = mFile.isDirectory();
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        String fileName = mFile.getName();
        TextView mTitle = findViewById(R.id.zh_operation_title);
        TextView mMessage = findViewById(R.id.zh_operation_message);
        Button mCancelButton = findViewById(R.id.zh_operation_cancel);
        Button mDeleteButton = findViewById(R.id.zh_operation_share);

        findViewById(R.id.zh_operation_more).setVisibility(View.GONE);
        findViewById(R.id.zh_operation_rename).setVisibility(View.GONE);
        findViewById(R.id.zh_operation_delete).setVisibility(View.GONE);

        mTitle.setText(isFolder ?
                getContext().getString(R.string.zh_file_delete_dir) :
                getContext().getString(R.string.zh_file_tips));
        String message = (isFolder ?
                getContext().getString(R.string.zh_file_delete_dir_message) :
                getContext().getString(R.string.zh_file_delete)) + "\n" + fileName;
        mMessage.setText(message);
        mDeleteButton.setText(getContext().getString(R.string.global_delete));

        mCancelButton.setOnClickListener(view -> DeleteDialog.this.dismiss());
        mDeleteButton.setOnClickListener(view -> {
            try {
                if (isFolder) {
                    runOnUiThread(() -> mFileListView.listFileAt(mFile.getParentFile()));
                    FileUtils.deleteDirectory(mFile);
                    runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_file_delete_dir_success) + "\n" + fileName, Toast.LENGTH_LONG).show());
                } else {
                    boolean deleted = FileUtils.deleteQuietly(mFile);
                    if (deleted) {
                        runOnUiThread(() -> Toast.makeText(getContext(), getContext().getString(R.string.zh_file_deleted) + fileName, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(mFileListView::refreshPath);

            DeleteDialog.this.dismiss();
        });
    }
}
