package com.movtery.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class TipDialog extends Dialog {
    private final String title, message, confirm, cancel;
    private final OnCancelClickListener cancelListener;
    private final OnConfirmClickListener confirmListener;

    public TipDialog(@NonNull Context context,
                     String title, String message, String confirm, String cancel,
                     OnCancelClickListener cancelListener, OnConfirmClickListener confirmListener) {
        super(context);
        this.title = title;
        this.message = message;
        this.confirm = confirm;
        this.cancel = cancel;

        this.cancelListener = cancelListener;
        this.confirmListener = confirmListener;

        init();
    }

    private void init() {
        setContentView(R.layout.dialog_tip);

        TextView titleView = findViewById(R.id.zh_tip_title);
        TextView messageView = findViewById(R.id.zh_tip_message);
        Button cancelButton = findViewById(R.id.zh_tip_cancel);
        Button confirmButton = findViewById(R.id.zh_tip_confirm);

        if (title != null) titleView.setText(title);
        if (message != null) messageView.setText(message);
        if (cancel != null) cancelButton.setText(cancel);
        if (confirm != null) confirmButton.setText(confirm);

        if (cancelListener != null) {
            cancelButton.setOnClickListener(v -> {
                cancelListener.onCancelClick();
                this.dismiss();
            });
        } else {
            cancelButton.setOnClickListener(v -> this.dismiss());
        }
        if (confirmListener != null) {
            confirmButton.setOnClickListener(v -> {
                confirmListener.onConfirmClick();
                this.dismiss();
            });
        } else {
            confirmButton.setOnClickListener(v -> this.dismiss());
        }
    }

    public static class Builder {
        private final Context context;
        private String title, message, cancel, confirm;
        private OnCancelClickListener cancelClickListener;
        private OnConfirmClickListener confirmClickListener;
        private boolean cancelable = true;

        public Builder(Context context) {
            this.context = context;
        }

        public void buildDialog() {
            TipDialog tipDialog = new TipDialog(this.context,
                    title, message, confirm, cancel, cancelClickListener, confirmClickListener);
            tipDialog.setCancelable(cancelable);
            tipDialog.show();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setCancel(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder setConfirm(String confirm) {
            this.confirm = confirm;
            return this;
        }

        public Builder setCancelClickListener(OnCancelClickListener cancelClickListener) {
            this.cancelClickListener = cancelClickListener;
            return this;
        }

        public Builder setConfirmClickListener(OnConfirmClickListener confirmClickListener) {
            this.confirmClickListener = confirmClickListener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public interface OnConfirmClickListener {
        void onConfirmClick();
    }
}
