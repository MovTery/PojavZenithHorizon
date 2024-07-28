package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

public class TipDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final String title, message, confirm, cancel;
    private final View[] moreView;
    private final boolean showCancel, showConfirm;
    private final OnCancelClickListener cancelListener;
    private final OnConfirmClickListener confirmListener;
    private final OnDialogDismissListener dismissListener;

    private TipDialog(@NonNull Context context,
                     String title, String message, String confirm, String cancel,
                     View[] moreView,
                     boolean showCancel, boolean showConfirm,
                     OnCancelClickListener cancelListener, OnConfirmClickListener confirmListener, OnDialogDismissListener dismissListener) {
        super(context);
        this.title = title;
        this.message = message;
        this.confirm = confirm;
        this.cancel = cancel;

        this.moreView = moreView;

        this.showCancel = showCancel;
        this.showConfirm = showConfirm;

        this.cancelListener = cancelListener;
        this.confirmListener = confirmListener;
        this.dismissListener = dismissListener;

        init();
        DraggableDialog.initDialog(this);
    }

    private void init() {
        setContentView(R.layout.dialog_tip);

        TextView titleView = findViewById(R.id.zh_tip_title);
        TextView messageView = findViewById(R.id.zh_tip_message);
        LinearLayoutCompat moreViewLayout = findViewById(R.id.zh_tip_more);
        Button cancelButton = findViewById(R.id.zh_tip_cancel);
        Button confirmButton = findViewById(R.id.zh_tip_confirm);

        if (title != null) titleView.setText(title);
        if (message != null) messageView.setText(message);
        if (moreView.length >= 1) {
            for (View view : moreView) {
                moreViewLayout.addView(view);
            }
        }
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

        cancelButton.setVisibility(showCancel ? View.VISIBLE : View.GONE);
        confirmButton.setVisibility(showConfirm ? View.VISIBLE : View.GONE);
    }

    @Override
    public void dismiss() {
        if (dismissListener != null) {
            if (!dismissListener.onDismiss()) return;
        }
        super.dismiss();
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public static class Builder {
        private final Context context;
        private final List<View> moreView = new ArrayList<>();
        private String title, message, cancel, confirm;
        private OnCancelClickListener cancelClickListener;
        private OnConfirmClickListener confirmClickListener;
        private OnDialogDismissListener dialogDismissListener;
        private boolean cancelable = true;
        private boolean showCancel = true;
        private boolean showConfirm = true;

        public Builder(Context context) {
            this.context = context;
        }

        public void buildDialog() {
            TipDialog tipDialog = new TipDialog(this.context,
                    title, message, confirm, cancel,
                    moreView.toArray(new View[0]),
                    showCancel, showConfirm,
                    cancelClickListener, confirmClickListener, dialogDismissListener);
            tipDialog.setCancelable(cancelable);
            tipDialog.show();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int title) {
            this.title = this.context.getString(title);
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = this.context.getString(message);
            return this;
        }

        public Builder setCancel(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder setCancel(int cancel) {
            this.cancel = this.context.getString(cancel);
            return this;
        }

        public Builder setConfirm(String confirm) {
            this.confirm = confirm;
            return this;
        }

        public Builder setConfirm(int confirm) {
            this.confirm = this.context.getString(confirm);
            return this;
        }

        public Builder addView(View view) {
            this.moreView.add(view);
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

        public Builder setDialogDismissListener(OnDialogDismissListener dialogDismissListener) {
            this.dialogDismissListener = dialogDismissListener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setShowCancel(boolean showCancel) {
            this.showCancel = showCancel;
            return this;
        }

        public Builder setShowConfirm(boolean showConfirm) {
            this.showConfirm = showConfirm;
            return this;
        }
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public interface OnConfirmClickListener {
        void onConfirmClick();
    }

    public interface OnDialogDismissListener {
        boolean onDismiss();
    }
}
