package net.kdt.pojavlaunch.dialog;

import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.utils.PasteFile;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class FilesDialog extends Dialog {
    private final boolean mCopy, mMove, mMore, mShare, mRename, mDelete;
    private final String mMessageText, mMoreText;
    private final Runnable runnable;
    private final File mFile;
    private String mFileSuffix;
    private View.OnClickListener mMoreClick;
    private RelativeLayout mMoveButton, mCopyButton, mMoreButton;
    private OnCopyButtonClickListener mCopyClick;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, File file) {
        super(context);
        this.runnable = runnable;
        this.mFile = file;
        PasteFile.COPY_FILE = file;

        this.mCopy = filesButton.copy;
        this.mMove = filesButton.move;
        this.mShare = filesButton.share;
        this.mRename = filesButton.rename;
        this.mDelete = filesButton.delete;
        this.mMore = filesButton.more;

        this.mMessageText = filesButton.messageText;
        this.mMoreText = filesButton.moreButtonText;

        this.setCancelable(true);
        this.setContentView(R.layout.dialog_operation_file);
        init();
    }

    private void init() {
        ImageView mCloseButton = findViewById(R.id.zh_operation_close);
        TextView mTitle = findViewById(R.id.zh_operation_title);
        TextView mMessage = findViewById(R.id.zh_operation_message);
        TextView moreText = findViewById(R.id.zh_file_more_text);
        RelativeLayout mShareButton = findViewById(R.id.zh_file_share);
        RelativeLayout mRenameButton = findViewById(R.id.zh_file_rename);
        RelativeLayout mDeleteButton = findViewById(R.id.zh_file_delete);
        mCopyButton = findViewById(R.id.zh_file_copy);
        mMoveButton = findViewById(R.id.zh_file_move);
        mMoreButton = findViewById(R.id.zh_file_more);

        mCloseButton.setOnClickListener(v -> this.dismiss());
        mShareButton.setOnClickListener(view -> {
            shareFile(getContext(), mFile.getName(), mFile.getAbsolutePath());
            FilesDialog.this.dismiss();
        });
        mRenameButton.setOnClickListener(view -> {
            if (this.mFile.isFile()) {
                renameFileListener(getContext(), runnable, mFile, this.mFileSuffix == null ? mFile.getName().substring(mFile.getName().lastIndexOf('.')) : this.mFileSuffix);
            } else if (this.mFile.isDirectory()) {
                renameFileListener(getContext(), runnable, mFile);
            }
            FilesDialog.this.dismiss();
        });
        mDeleteButton.setOnClickListener(view -> {
            DeleteDialog deleteDialog = new DeleteDialog(getContext(), runnable, mFile);
            deleteDialog.show();
            FilesDialog.this.dismiss();
        });

        if (this.mMessageText != null) mMessage.setText(this.mMessageText);
        if (this.mMoreText != null) moreText.setText(this.mMoreText);
        if (this.mFile.isDirectory()) mTitle.setText(getContext().getString(R.string.zh_file_folder_tips));

        setButtonClickable(mShare, mShareButton);
        setButtonClickable(mRename, mRenameButton);
        setButtonClickable(mDelete, mDeleteButton);
        setButtonClickable(mCopy, mCopyButton);
        setButtonClickable(mMove, mMoveButton);
        setButtonClickable(mMore, mMoreButton);
    }

    private void setButtonClickable(boolean clickable, RelativeLayout button) {
        button.setClickable(clickable);
        button.setAlpha(clickable ? 1f : 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mCopy && this.mCopyClick != null) mCopyButton.setOnClickListener(v -> {
            PasteFile.PASTE_TYPE = PasteFile.PasteType.COPY; //复制模式
            this.mCopyClick.onButtonClick();
            this.dismiss();
        });

        if (this.mMove) mMoveButton.setOnClickListener(v -> {
            PasteFile.PASTE_TYPE = PasteFile.PasteType.MOVE; //移动模式
            this.mCopyClick.onButtonClick();
            this.dismiss();
        });

        if (this.mMore && this.mMoreClick != null) mMoreButton.setOnClickListener(this.mMoreClick);
    }

    public void setMoreButtonClick(View.OnClickListener click) {
        this.mMoreClick = click;
    }

    public void setCopyButtonClick(OnCopyButtonClickListener listener) {
        this.mCopyClick = listener;
    }

    public void setFileSuffix(String suffixes) {
        this.mFileSuffix = suffixes;
    }

    public interface OnCopyButtonClickListener {
        void onButtonClick();
    }

    public static class FilesButton {
        public boolean copy, move, share, rename, delete, more;
        public String messageText, moreButtonText;

        public void setButtonVisibility(boolean copy, boolean move, boolean shareButton, boolean renameButton, boolean deleteButton, boolean moreButton) {
            this.copy = copy;
            this.move = move;
            this.share = shareButton;
            this.rename = renameButton;
            this.delete = deleteButton;
            this.more = moreButton;
        }
    }
}
