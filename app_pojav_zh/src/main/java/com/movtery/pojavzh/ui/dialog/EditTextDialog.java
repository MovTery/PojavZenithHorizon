package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class EditTextDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final String title, message, editText, hintText;
    private final View.OnClickListener cancelListener;
    private final ConfirmListener confirmListener;
    private final int inputType;
    private EditText mEditBox;

    private EditTextDialog(@NonNull Context context, String title, String message, String editText, String hintText,
                           int inputType,
                           View.OnClickListener cancelListener, ConfirmListener confirmListener) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_edit_text);

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
        TextView mTitle = findViewById(R.id.zh_edit_text_title);
        TextView mMessage = findViewById(R.id.zh_edit_text_message);
        ScrollView mScrollTextView = findViewById(R.id.zh_edit_text_scroll);
        mEditBox = findViewById(R.id.zh_edit_text_edit);

        Button mConfirmButton = findViewById(R.id.zh_edit_text_confirm_button);
        Button mCancelButton = findViewById(R.id.zh_edit_text_cancel_button);

        if (this.title != null) {
            mTitle.setText(this.title);
        }
        if (this.message != null) {
            mMessage.setText(this.message);
        } else {
            mScrollTextView.setVisibility(View.GONE);
        }

        if (editText != null) mEditBox.setText(editText);
        if (hintText != null) mEditBox.setHint(hintText);

        if (inputType != -1) mEditBox.setInputType(inputType);

        if (this.confirmListener != null) {
            mConfirmButton.setOnClickListener(v -> {
                boolean dismissDialog = confirmListener.onConfirm(mEditBox);
                if (dismissDialog) this.dismiss();
            });
        }
        if (this.cancelListener != null) {
            mCancelButton.setOnClickListener(this.cancelListener);
        } else {
            mCancelButton.setOnClickListener(view -> this.dismiss());
        }
    }

    @Override
    public Window onInit() {
        return getWindow();
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
            this.title = context.getString(title);
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = context.getString(message);
            return this;
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

    public interface ConfirmListener {
        boolean onConfirm(EditText editText);
    }
}
