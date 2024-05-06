package net.kdt.pojavlaunch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class EditTextDialog extends Dialog {
    private View.OnClickListener confirm, cancel;
    private TextView mTitle, mMessage;
    private EditText mEditBox;
    private ScrollView mScrollTextView;
    private final String title, message, editText, hintText;
    private String cancelButtonText, confirmButtonText;

    public EditTextDialog(@NonNull Context context, String title, String message, String editText, String hintText) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_edit_text);

        this.title = title;
        this.message = message;
        this.editText = editText;
        this.hintText = hintText;
        init();
    }

    private void init() {
        mTitle = findViewById(R.id.zh_edit_text_title);
        mMessage = findViewById(R.id.zh_edit_text_message);
        mScrollTextView = findViewById(R.id.zh_edit_text_scroll);
    }

    public void setCancelButtonText(String cancelButtonText) {
        this.cancelButtonText = cancelButtonText;
    }

    public void setConfirmButtonText(String confirmButtonText) {
        this.confirmButtonText = confirmButtonText;
    }

    public void setCancel(View.OnClickListener cancel) {
        this.cancel = cancel;
    }

    public void setConfirm(View.OnClickListener confirm) {
        this.confirm = confirm;
    }

    public EditText getEditBox() {
        return mEditBox;
    }

    @Override
    public void show() {
        if (this.title != null) {
            this.mTitle.setText(this.title);
        }
        if (this.message != null) {
            this.mMessage.setText(this.message);
        } else {
            this.mScrollTextView.setVisibility(View.GONE);
        }

        mEditBox = findViewById(R.id.zh_edit_text_edit);
        if (editText != null) mEditBox.setText(editText);
        if (hintText != null) mEditBox.setHint(hintText);

        //初始化按钮
        Button mConfirmButton = findViewById(R.id.zh_edit_text_confirm_button);
        Button mCancelButton = findViewById(R.id.zh_edit_text_cancel_button);

        mConfirmButton.setOnClickListener(this.confirm);
        if (this.cancel != null) {
            mCancelButton.setOnClickListener(this.cancel);
        } else {
            mCancelButton.setOnClickListener(view -> this.dismiss());
        }
        if (this.cancelButtonText != null) mCancelButton.setText(this.cancelButtonText);
        if (this.confirmButtonText != null) mConfirmButton.setText(this.confirmButtonText);

        super.show();
    }
}
