package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.file.PasteFile;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final Runnable runnable;
    private TextView mTitle, mMessage, moreText;
    private String mFileSuffix;
    private OnCopyButtonClickListener mCopyClick;
    private OnMoreButtonClickListener mMoreClick;
    private RelativeLayout mShareButton, mRenameButton, mDeleteButton, mMoveButton, mCopyButton, mMoreButton;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, List<File> selectedFiles) {
        super(context);
        this.runnable = runnable;

        init(filesButton);
        handleButtons(filesButton, selectedFiles);
    }

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, File file) {
        super(context);
        this.runnable = runnable;
        List<File> singleFileList = new ArrayList<>();
        singleFileList.add(file);

        init(filesButton);
        handleButtons(filesButton, singleFileList);
    }

    private void init(FilesButton filesButton) {
        this.setCancelable(true);
        this.setContentView(R.layout.dialog_operation_file);

        mTitle = findViewById(R.id.zh_operation_title);
        mMessage = findViewById(R.id.zh_operation_message);
        moreText = findViewById(R.id.zh_file_more_text);
        mShareButton = findViewById(R.id.zh_file_share);
        mRenameButton = findViewById(R.id.zh_file_rename);
        mDeleteButton = findViewById(R.id.zh_file_delete);
        mCopyButton = findViewById(R.id.zh_file_copy);
        mMoveButton = findViewById(R.id.zh_file_move);
        mMoreButton = findViewById(R.id.zh_file_more);

        ImageView mCloseButton = findViewById(R.id.zh_operation_close);
        mCloseButton.setOnClickListener(v -> this.dismiss());

        if (filesButton.more) {
            mMoreButton.setOnClickListener(v -> {
                if (this.mMoreClick != null) this.mMoreClick.onButtonClick();
                closeDialog();
            });
        }
        DraggableDialog.initDialog(this);
    }

    private void handleButtons(FilesButton filesButton, List<File> selectedFiles) {
        mDeleteButton.setOnClickListener(view -> {
            DeleteDialog deleteDialog = new DeleteDialog(getContext(), this.runnable, selectedFiles);
            deleteDialog.show();
            closeDialog();
        });

        PasteFile pasteFile = PasteFile.getInstance();
        mCopyButton.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                pasteFile.setPaste(selectedFiles, PasteFile.PasteType.COPY); // 复制模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });
        mMoveButton.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                pasteFile.setPaste(selectedFiles, PasteFile.PasteType.MOVE); // 移动模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });

        if (selectedFiles.size() == 1) { //单选模式
            File file = selectedFiles.get(0);
            mShareButton.setOnClickListener(view -> {
                FileTools.shareFile(getContext(), file.getName(), file.getAbsolutePath());
                closeDialog();
            });
            mRenameButton.setOnClickListener(view -> {
                if (file.isFile()) {
                    FileTools.renameFileListener(getContext(), runnable, file, mFileSuffix == null ? file.getName().substring(file.getName().lastIndexOf('.')) : mFileSuffix);
                } else if (file.isDirectory()) {
                    FileTools.renameFileListener(getContext(), runnable, file);
                }
                closeDialog();
            });

            setButtonClickable(filesButton.share, mShareButton);
            setButtonClickable(filesButton.rename, mRenameButton);
        } else {
            //多选模式禁止使用分享、重命名
            setButtonClickable(false, mShareButton);
            setButtonClickable(false, mRenameButton);
        }

        setDialogTexts(filesButton, selectedFiles.get(0));

        setButtonClickable(filesButton.delete, mDeleteButton);
        setButtonClickable(filesButton.copy, mCopyButton);
        setButtonClickable(filesButton.move, mMoveButton);
        setButtonClickable(filesButton.more, mMoreButton);
    }

    private void setDialogTexts(FilesButton filesButton, File file) {
        if (filesButton.titleText != null) mTitle.setText(filesButton.titleText);
        if (filesButton.messageText != null) mMessage.setText(filesButton.messageText);
        if (filesButton.moreButtonText != null) moreText.setText(filesButton.moreButtonText);
        if (file != null && file.isDirectory())
            mTitle.setText(getContext().getString(R.string.zh_file_folder_tips));
    }

    private void closeDialog() {
        FilesDialog.this.dismiss();
    }

    //此方法要在设置点击事件之后调用，否则禁用按钮后按钮仍然能够点击
    private void setButtonClickable(boolean clickable, RelativeLayout button) {
        button.setClickable(clickable);
        button.setAlpha(clickable ? 1f : 0.5f);
    }

    public void setCopyButtonClick(OnCopyButtonClickListener click) {
        this.mCopyClick = click;
    }

    public void setMoreButtonClick(OnMoreButtonClickListener click) {
        this.mMoreClick = click;
    }

    public void setFileSuffix(String suffixes) {
        this.mFileSuffix = suffixes;
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public interface OnCopyButtonClickListener {
        void onButtonClick();
    }

    public interface OnMoreButtonClickListener {
        void onButtonClick();
    }

    public static class FilesButton {
        private boolean copy, move, share, rename, delete, more;
        private String titleText, messageText, moreButtonText;

        public void setButtonVisibility(boolean copy, boolean move, boolean shareButton, boolean renameButton, boolean deleteButton, boolean moreButton) {
            this.copy = copy;
            this.move = move;
            this.share = shareButton;
            this.rename = renameButton;
            this.delete = deleteButton;
            this.more = moreButton;
        }

        public void setDialogText(String titleText, String messageText, String moreButtonText) {
            this.titleText = titleText;
            this.messageText = messageText;
            this.moreButtonText = moreButtonText;
        }

        public void setTitleText(String titleText) {
            this.titleText = titleText;
        }

        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }

        public void setMoreButtonText(String moreButtonText) {
            this.moreButtonText = moreButtonText;
        }
    }
}
