package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.refreshFileCount;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class FilesDialog extends Dialog {
    private View.OnClickListener mMoreClick;
    private Button mMoreButton;
    private final boolean mCancel, mMore, mShare, mRename, mDelete, mDisplayThumbnails;
    private final String mMessageText, mMoreText, mTitle;
    private final FileListView mFileListView;
    private final File mFile;
    private final TextView mTitleView;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, FileListView fileListView, File file) {
        super(context);
        this.mFileListView = fileListView;
        this.mFile = file;

        this.mCancel = true;
        this.mShare = filesButton.share;
        this.mRename = filesButton.rename;
        this.mDelete = filesButton.delete;
        this.mMore = filesButton.more;

        this.mDisplayThumbnails = filesButton.displayThumbnails;

        this.mMessageText = filesButton.messageText;
        this.mMoreText = filesButton.moreButtonText;

        this.mTitleView = null;
        this.mTitle = null;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_operation);
        init();
    }

    public FilesDialog(@NonNull Context context, FilesButton filesButton, FileListView fileListView, File file, TextView titleView, String title) {
        super(context);
        this.mFileListView = fileListView;
        this.mFile = file;

        this.mCancel = true;
        this.mShare = filesButton.share;
        this.mRename = filesButton.rename;
        this.mDelete = filesButton.delete;
        this.mMore = filesButton.more;

        this.mDisplayThumbnails = filesButton.displayThumbnails;

        this.mMessageText = filesButton.messageText;
        this.mMoreText = filesButton.moreButtonText;

        this.mTitleView = titleView;
        this.mTitle = title;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_operation);
        init();
    }
    private void init() {
        TextView mMessage = findViewById(R.id.zh_operation_message);
        Button mCancelButton = findViewById(R.id.zh_operation_cancel);
        Button mShareButton = findViewById(R.id.zh_operation_share);
        Button mRenameButton = findViewById(R.id.zh_operation_rename);
        Button mDeleteButton = findViewById(R.id.zh_operation_delete);
        mMoreButton = findViewById(R.id.zh_operation_more);

        mCancelButton.setVisibility(mCancel ? View.VISIBLE : View.GONE);
        mShareButton.setVisibility(mShare ? View.VISIBLE : View.GONE);
        mRenameButton.setVisibility(mRename ? View.VISIBLE : View.GONE);
        mDeleteButton.setVisibility(mDelete ? View.VISIBLE : View.GONE);
        mMoreButton.setVisibility(mMore ? View.VISIBLE : View.GONE);

        if (this.mMessageText != null) mMessage.setText(this.mMessageText);
        mCancelButton.setOnClickListener(view -> FilesDialog.this.dismiss());
        mShareButton.setOnClickListener(view -> {
            shareFile(getContext(), mFile.getName(), mFile.getAbsolutePath());
            FilesDialog.this.dismiss();
        });
        mRenameButton.setOnClickListener(view -> {
            renameFileListener(getContext(), mFileListView, mFile, this.mDisplayThumbnails);
            FilesDialog.this.dismiss();
        });
        mDeleteButton.setOnClickListener(view -> {
            AlertDialog.Builder deleteConfirmation = deleteFileListener(getContext(), mFileListView, mFile, this.mDisplayThumbnails, this.mTitleView, this.mTitle);
            deleteConfirmation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    refreshFileCount(getContext(), FilesDialog.this.mFileListView, FilesDialog.this.mTitleView, FilesDialog.this.mTitle);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            deleteConfirmation.show();
            FilesDialog.this.dismiss();
        });

        if (this.mMoreText != null) mMoreButton.setText(this.mMoreText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mMore || this.mMoreClick != null) mMoreButton.setOnClickListener(this.mMoreClick);
    }

    public void setMoreButtonClick(View.OnClickListener click) {
        this.mMoreClick = click;
    }

    public static class FilesButton {
        public boolean cancel, share, rename, delete, more, displayThumbnails;
        public String messageText, moreButtonText;
        public void setButtonVisibility(boolean shareButton, boolean renameButton, boolean deleteButton, boolean moreButton) {
            this.share = shareButton;
            this.rename = renameButton;
            this.delete = deleteButton;
            this.more = moreButton;
        }
    }
}
