package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.databinding.DialogEditTextBinding;

public class EditTextDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogEditTextBinding binding = DialogEditTextBinding.inflate(getLayoutInflater());
    private final String title, message, editText, hintText;
    private final View.OnClickListener cancelListener;
    private final ConfirmListener confirmListener;
    private final int inputType;

    private EditTextDialog(@NonNull Context context, String title, String message, String editText, String hintText,
                           int inputType,
                           View.OnClickListener cancelListener, ConfirmListener confirmListener) {
        super(context);

        this.setCancelable(false);
        this.setContentView(binding.getRoot());

        this.title = title;
        this.message = message;
        this.editText = editText;
        this.hintText = hintText;

        this.inputType = inputType;

        this.cancelListener = cancelListener;
        this.confirmListener = confirmListener;
        init();
        DraggableDialog.initDialog(this);
    }

    private void init() {
        if (this.title != null) {
            binding.titleView.setText(this.title);
        }
        if (this.message != null) {
            binding.messageView.setText(this.message);
        } else {
            binding.scrollView.setVisibility(View.GONE);
        }

        if (editText != null) binding.textEdit.setText(editText);
        if (hintText != null) binding.textEdit.setHint(hintText);

        if (inputType != -1) binding.textEdit.setInputType(inputType);

        if (this.confirmListener != null) {
            binding.confirmButton.setOnClickListener(v -> {
                boolean dismissDialog = confirmListener.onConfirm(binding.textEdit);
                if (dismissDialog) this.dismiss();
            });
        }
        View.OnClickListener cancelListener = this.cancelListener != null ? this.cancelListener : view -> this.dismiss();
        binding.cancelButton.setOnClickListener(cancelListener);
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public interface ConfirmListener {
        boolean onConfirm(EditText editText);
    }

    public static class Builder {
        private final Context context;
        private String title, message, editText, hintText;
        private int inputType = -1;
        private View.OnClickListener cancelListener;
        private ConfirmListener confirmListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int title) {
            return setTitle(context.getString(title));
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            return setMessage(context.getString(message));
        }

        public Builder setEditText(String editText) {
            this.editText = editText;
            return this;
        }

        public Builder setHintText(String hintText) {
            this.hintText = hintText;
            return this;
        }

        public Builder setHintText(int hintText) {
            this.hintText = context.getString(hintText);
            return this;
        }

        public Builder setCancel(View.OnClickListener cancel) {
            this.cancelListener = cancel;
            return this;
        }

        public Builder setInputType(int inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder setConfirmListener(ConfirmListener confirmListener) {
            this.confirmListener = confirmListener;
            return this;
        }

        public void buildDialog() {
            new EditTextDialog(this.context, this.title, this.message, this.editText, this.hintText,
                    inputType,
                    this.cancelListener, this.confirmListener).show();
        }
    }
}
