package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class FilesDialog extends Dialog {
    private final View.OnClickListener mMoreClick, mShareClick, mRenameClick, mDeleteClick;
    private final boolean mCancel, mMore, mShare, mRename, mDelete;
    private final String mMessageText, mMoreText;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, ButtonClick buttonClick) {
        super(context);
        this.mCancel = filesButton.cancel;
        this.mShare = filesButton.share;
        this.mRename = filesButton.rename;
        this.mDelete = filesButton.delete;
        this.mMore = filesButton.more;

        this.mShareClick = buttonClick.share;
        this.mRenameClick = buttonClick.rename;
        this.mDeleteClick = buttonClick.delete;
        this.mMoreClick = buttonClick.more;

        this.mMessageText = filesButton.messageText;
        this.mMoreText = filesButton.moreButtonText;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_files);
        init();
    }

    private void init() {
        TextView mMessage = findViewById(R.id.zh_files_message);
        Button mCancelButton = findViewById(R.id.zh_files_cancel);
        Button mShareButton = findViewById(R.id.zh_files_share);
        Button mRenameButton = findViewById(R.id.zh_files_rename);
        Button mDeleteButton = findViewById(R.id.zh_files_delete);
        Button mMoreButton = findViewById(R.id.zh_files_more);

        mCancelButton.setVisibility(mCancel ? View.VISIBLE : View.GONE);
        mShareButton.setVisibility(mShare ? View.VISIBLE : View.GONE);
        mRenameButton.setVisibility(mRename ? View.VISIBLE : View.GONE);
        mDeleteButton.setVisibility(mDelete ? View.VISIBLE : View.GONE);
        mMoreButton.setVisibility(mMore ? View.VISIBLE : View.GONE);

        if (this.mMessageText != null) mMessage.setText(this.mMessageText);
        mCancelButton.setOnClickListener(view -> FilesDialog.this.dismiss());
        mShareButton.setOnClickListener(this.mShareClick);
        mRenameButton.setOnClickListener(this.mRenameClick);
        mDeleteButton.setOnClickListener(this.mDeleteClick);

        if (this.mMoreText != null) mMoreButton.setText(this.mMoreText);
        mMoreButton.setOnClickListener(this.mMoreClick);
    }

    public static class FilesButton {
        public boolean cancel, share, rename, delete, more;
        public String messageText, moreButtonText;
        public void setButtonVisibility(boolean cancelButton, boolean shareButton, boolean renameButton, boolean deleteButton, boolean moreButton) {
            this.cancel = cancelButton;
            this.share = shareButton;
            this.rename = renameButton;
            this.delete = deleteButton;
            this.more = moreButton;
        }
    }

    public static class ButtonClick {
        public View.OnClickListener share, rename, delete, more;
        public void setShareButton(Context context, File file) {
            this.share = view -> shareFile(context, file.getName(), file.getAbsolutePath());
        }
        public void setRenameButton(Activity activity, FileListView mFileListView, File file) {
            this.rename = view -> renameFileListener(activity, mFileListView, file, false);
        }
        public void setDeleteButton(Activity activity, FileListView mFileListView, File file) {
            this.delete = view -> deleteFileListener(activity, mFileListView, file, false);
        }
        public void setMoreButton(View.OnClickListener click) {
            this.more = click;
        }
    }
}
