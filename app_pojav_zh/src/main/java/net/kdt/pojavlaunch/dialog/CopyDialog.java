package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CopyDialog extends Dialog {
    private final FileListView mFileListView;
    private final File mFile;

    public CopyDialog(@NonNull Context context, FileListView fileListView, File file) {
        super(context);
        this.mFileListView = fileListView;
        this.mFile = file;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_operation);
        init(context);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context) {
        TextView mTitle = findViewById(R.id.zh_operation_title);
        TextView mMessage = findViewById(R.id.zh_operation_message);
        Button mCancelButton = findViewById(R.id.zh_operation_cancel);
        Button mCopyButton = findViewById(R.id.zh_operation_share);

        findViewById(R.id.zh_operation_more).setVisibility(View.GONE);
        findViewById(R.id.zh_operation_rename).setVisibility(View.GONE);
        findViewById(R.id.zh_operation_delete).setVisibility(View.GONE);

        mTitle.setText(context.getString(R.string.zh_file_copy_dialog_title));
        mMessage.setText(context.getString(R.string.zh_file_copy_dialog_message));
        mCopyButton.setText(context.getString(R.string.zh_confirm));

        mCancelButton.setOnClickListener(view -> CopyDialog.this.dismiss());
        mCopyButton.setOnClickListener(view -> {
            CopyDialog.this.dismiss();

            String fileName = mFile.getName();
            String suffix = fileName.substring(fileName.lastIndexOf('.'));
            String newName = fileName.substring(0, fileName.lastIndexOf(suffix)) + "_new";

            //复制自定义名称
            AlertDialog.Builder copyBuilder = new AlertDialog.Builder(context);
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_edit_text, null);
            EditText input = itemView.findViewById(R.id.zh_edit_text);
            input.setText(newName);

            copyBuilder.setTitle(context.getString(R.string.zh_file_copy_dialog_new_name_title));
            copyBuilder.setView(itemView);
            copyBuilder.setPositiveButton(context.getString(R.string.zh_confirm), (dialog, which) -> {
                String newFileName = input.getText().toString();
                if (!newFileName.isEmpty()) {
                    //新线程复制文件
                    PojavApplication.sExecutorService.execute(() -> {
                        try {
                            FileUtils.copyFile(mFile, new File(mFile.getParent(), newFileName + suffix));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        runOnUiThread(mFileListView::refreshPath);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_file_rename_empty), Toast.LENGTH_SHORT).show());
                }
            });
            copyBuilder.setNegativeButton(context.getString(android.R.string.cancel), null);
            copyBuilder.show();
        });
    }
}
