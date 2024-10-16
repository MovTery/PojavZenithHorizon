package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.file.PasteFile;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogOperationFileBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogOperationFileBinding binding = DialogOperationFileBinding.inflate(getLayoutInflater());
    private final Runnable runnable;
    private final File root;
    private final List<File> selectedFiles;
    private String mFileSuffix;
    private OnCopyButtonClickListener mCopyClick;
    private OnMoreButtonClickListener mMoreClick;

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, File root, List<File> selectedFiles) {
        super(context);
        this.runnable = runnable;
        this.root = root;
        this.selectedFiles = selectedFiles;

        init(filesButton);
        handleButtons(filesButton);
    }

    public FilesDialog(@NonNull Context context, FilesButton filesButton, Runnable runnable, File root, File file) {
        super(context);
        this.runnable = runnable;
        this.root = root;
        List<File> singleFileList = new ArrayList<>();
        singleFileList.add(file);
        this.selectedFiles = singleFileList;

        init(filesButton);
        handleButtons(filesButton);
    }

    private void init(FilesButton filesButton) {
        this.setCancelable(true);
        this.setContentView(binding.getRoot());

        binding.closeButton.setOnClickListener(v -> this.dismiss());

        if (filesButton.more) {
            binding.moreView.setOnClickListener(v -> {
                if (this.mMoreClick != null) this.mMoreClick.onButtonClick();
                closeDialog();
            });
        }
        DraggableDialog.initDialog(this);
    }

    private void handleButtons(FilesButton filesButton) {
        binding.deleteView.setOnClickListener(view -> {
            DeleteDialog deleteDialog = new DeleteDialog(getContext(), this.runnable, selectedFiles);
            deleteDialog.show();
            closeDialog();
        });

        PasteFile pasteFile = PasteFile.getInstance();
        binding.copyView.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                pasteFile.setPaste(root, selectedFiles, PasteFile.PasteType.COPY); // 复制模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });
        binding.moveView.setOnClickListener(v -> {
            if (this.mCopyClick != null) {
                pasteFile.setPaste(root, selectedFiles, PasteFile.PasteType.MOVE); // 移动模式
                this.mCopyClick.onButtonClick();
            }
            closeDialog();
        });

        if (selectedFiles.size() == 1) { //单选模式
            File file = selectedFiles.get(0);
            binding.shareView.setOnClickListener(view -> {
                FileTools.shareFile(getContext(), file);
                closeDialog();
            });
            binding.renameView.setOnClickListener(view -> {
                if (file.isFile()) {
                    FileTools.renameFileListener(getContext(), runnable, file, mFileSuffix == null ? file.getName().substring(file.getName().lastIndexOf('.')) : mFileSuffix);
                } else if (file.isDirectory()) {
                    FileTools.renameFileListener(getContext(), runnable, file);
                }
                closeDialog();
            });

            setButtonClickable(filesButton.share, binding.shareView);
            setButtonClickable(filesButton.rename, binding.renameView);
        } else {
            //多选模式禁止使用分享、重命名
            setButtonClickable(false, binding.shareView);
            setButtonClickable(false, binding.renameView);
        }

        setDialogTexts(filesButton, selectedFiles.get(0));

        setButtonClickable(filesButton.delete, binding.deleteView);
        setButtonClickable(filesButton.copy, binding.copyView);
        setButtonClickable(filesButton.move, binding.moveView);
        setButtonClickable(filesButton.more, binding.moreView);
    }

    private void setDialogTexts(FilesButton filesButton, File file) {
        if (filesButton.titleText != null) binding.titleView.setText(filesButton.titleText);
        if (filesButton.messageText != null) binding.messageView.setText(filesButton.messageText);
        if (filesButton.moreButtonText != null) binding.moreTextView.setText(filesButton.moreButtonText);
        if (file != null && file.isDirectory())
            binding.titleView.setText(getContext().getString(R.string.file_folder_tips));
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
