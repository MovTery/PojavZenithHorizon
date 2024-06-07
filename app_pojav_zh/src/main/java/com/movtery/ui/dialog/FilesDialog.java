package com.movtery.ui.dialog;

import static com.movtery.utils.PojavZHTools.renameFileListener;
import static com.movtery.utils.PojavZHTools.shareFile;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.ui.subassembly.filelist.FileItemBean;
import com.movtery.utils.PasteFile;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesDialog extends Dialog {
    private final Runnable runnable;
    private TextView mTitle, mMessage, moreText;
    private String mFileSuffix;
    private View.OnClickListener mMoreClick;
    private RelativeLayout mShareButton, mRenameButton, mDeleteButton, mMoveButton, mCopyButton, mMoreButton;
    private OnCopyButtonClickListener mCopyClick;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, List<FileItemBean> itemBeans, Runnable runnable, View.OnClickListener more) {
        super(context);
        this.runnable = runnable;
        init();
        multiSelectMode(filesButton, itemBeans, more);
    }

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, File file) {
        super(context);
        this.runnable = runnable;
        init();
        singleChoiceMode(filesButton, file);
    }

    private void init() {
        this.setCancelable(true);
        this.setContentView(R.layout.dialog_operation_file);

        ImageView mCloseButton = findViewById(R.id.zh_operation_close);
        mTitle = findViewById(R.id.zh_operation_title);
        mMessage = findViewById(R.id.zh_operation_message);
        moreText = findViewById(R.id.zh_file_more_text);
        mShareButton = findViewById(R.id.zh_file_share);
        mRenameButton = findViewById(R.id.zh_file_rename);
        mDeleteButton = findViewById(R.id.zh_file_delete);
        mCopyButton = findViewById(R.id.zh_file_copy);
        mMoveButton = findViewById(R.id.zh_file_move);
        mMoreButton = findViewById(R.id.zh_file_more);

        mCloseButton.setOnClickListener(v -> this.dismiss());
    }

    private void closeDialog() {
        FilesDialog.this.dismiss();
    }

    private void multiSelectMode(FilesButton filesButton, List<FileItemBean> itemBeans, View.OnClickListener more) {
        //多选模式禁用分享、重命名
        setButtonClickable(false, mShareButton);
        setButtonClickable(false, mRenameButton);
        setButtonClickable(true, mDeleteButton);
        setButtonClickable(true, mCopyButton);
        setButtonClickable(true, mMoveButton);
        setButtonClickable(more != null, mMoreButton);

        List<File> selectedFiles = new ArrayList<>();
        itemBeans.forEach(v -> {
            File file = v.getFile();
            if (file != null) selectedFiles.add(file);
        } );
        mDeleteButton.setOnClickListener(view -> {
            DeleteDialog deleteDialog = new DeleteDialog(getContext(), this.runnable, selectedFiles);
            deleteDialog.show();
            closeDialog();
        });
        mCopyButton.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                PasteFile.getInstance().setPaste(selectedFiles, PasteFile.PasteType.COPY); //复制模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });
        mMoveButton.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                PasteFile.getInstance().setPaste(selectedFiles, PasteFile.PasteType.MOVE); //移动模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });
        if (more != null) {
            mMoreButton.setOnClickListener(v -> {
                more.onClick(v);
                if (this.runnable != null) PojavApplication.sExecutorService.execute(this.runnable);
                closeDialog();
            });
        }
        if (filesButton != null && filesButton.titleText != null) mTitle.setText(filesButton.titleText);
        if (filesButton != null && filesButton.messageText != null) mMessage.setText(filesButton.messageText);
        if (filesButton != null && filesButton.moreButtonText != null) moreText.setText(filesButton.moreButtonText);
    }

    private void singleChoiceMode(FilesButton filesButton, File file) {
        setButtonClickable(filesButton.share, mShareButton);
        setButtonClickable(filesButton.rename, mRenameButton);
        setButtonClickable(filesButton.delete, mDeleteButton);
        setButtonClickable(filesButton.copy, mCopyButton);
        setButtonClickable(filesButton.move, mMoveButton);
        setButtonClickable(filesButton.more, mMoreButton);

        mShareButton.setOnClickListener(view -> {
            if (file != null) shareFile(getContext(), file.getName(), file.getAbsolutePath());
            closeDialog();
        });
        mRenameButton.setOnClickListener(view -> {
            if (file != null) {
                if (file.isFile()) {
                    renameFileListener(getContext(), runnable, file, this.mFileSuffix == null ? file.getName().substring(file.getName().lastIndexOf('.')) : this.mFileSuffix);
                } else if (file.isDirectory()) {
                    renameFileListener(getContext(), runnable, file);
                }
            }
            closeDialog();
        });
        mDeleteButton.setOnClickListener(view -> {
            if (file != null) {
                DeleteDialog deleteDialog = new DeleteDialog(getContext(), runnable, file);
                deleteDialog.show();
            }
            closeDialog();
        });
        mCopyButton.setOnClickListener(v -> {
            if (filesButton.copy && this.mCopyClick != null) {
                PasteFile.getInstance().setPaste(file, PasteFile.PasteType.COPY); //复制模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });
        mMoveButton.setOnClickListener(v -> {
            if (filesButton.move && this.mCopyClick != null) {
                PasteFile.getInstance().setPaste(file, PasteFile.PasteType.MOVE); //移动模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });

        if (filesButton.more && this.mMoreClick != null) mMoreButton.setOnClickListener(this.mMoreClick);

        if (filesButton.messageText != null) mMessage.setText(filesButton.messageText);
        if (filesButton.moreButtonText != null) moreText.setText(filesButton.moreButtonText);
        if (file != null && file.isDirectory()) mTitle.setText(getContext().getString(R.string.zh_file_folder_tips));
    }

    private void setButtonClickable(boolean clickable, RelativeLayout button) {
        button.setClickable(clickable);
        button.setAlpha(clickable ? 1f : 0.5f);
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
        public String titleText, messageText, moreButtonText;

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
