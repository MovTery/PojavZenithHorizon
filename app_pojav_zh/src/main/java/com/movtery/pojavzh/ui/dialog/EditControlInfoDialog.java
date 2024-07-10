package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.R;

public class EditControlInfoDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final ControlInfoData controlInfoData;
    private final String mFileName;
    private final boolean editFileName;
    private String title;
    private EditText mFileNameEditBox, mNameEditBox, mVersionEditBox, mAuthorEditBox, mDescEditBox;
    private OnConfirmClickListener mOnConfirmClickListener;

    public EditControlInfoDialog(@NonNull Context context, boolean editFileName, String fileName, ControlInfoData controlInfoData) {
        super(context);

        this.editFileName = editFileName;
        this.controlInfoData = controlInfoData;
        this.mFileName = fileName;

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_edit_control_info);

        initViews();
        initButtons();
        initData();
        DraggableDialog.initDialog(this);
    }

    private void initViews() {
        mFileNameEditBox = findViewById(R.id.zh_edit_control_info_file_name_edit);
        mNameEditBox = findViewById(R.id.zh_edit_control_info_name_edit);
        mVersionEditBox = findViewById(R.id.zh_edit_control_info_version_edit);
        mAuthorEditBox = findViewById(R.id.zh_edit_control_info_author_edit);
        mDescEditBox = findViewById(R.id.zh_edit_control_info_desc_edit);
        mFileNameEditBox.setEnabled(editFileName);

        //设置hint
        mFileNameEditBox.setHint(R.string.zh_required); //必填
        mNameEditBox.setHint(R.string.zh_optional); //选填
        mVersionEditBox.setHint(R.string.zh_optional);
        mAuthorEditBox.setHint(R.string.zh_optional);
        mDescEditBox.setHint(R.string.zh_optional);
    }

    private void initButtons() {
        Button cancelButton = findViewById(R.id.zh_edit_control_info_cancel_button);
        Button confirmButton = findViewById(R.id.zh_edit_control_info_confirm_button);

        cancelButton.setOnClickListener(v -> dismiss());
        confirmButton.setOnClickListener(v -> confirmClick());
    }

    private void confirmClick() {
        if (mFileNameEditBox.getText().toString().isEmpty()) {
            mFileNameEditBox.setError(getContext().getString(R.string.global_error_field_empty));
            return;
        }
        updateControlInfoData();
        if (mOnConfirmClickListener != null) {
            mOnConfirmClickListener.OnClick(mFileNameEditBox.getText().toString(), controlInfoData);
        }
    }

    private void updateControlInfoData() {
        controlInfoData.name = getValueOrDefault(mNameEditBox);
        controlInfoData.version = getValueOrDefault(mVersionEditBox);
        controlInfoData.author = getValueOrDefault(mAuthorEditBox);
        controlInfoData.desc = getValueOrDefault(mDescEditBox);
    }

    private String getValueOrDefault(EditText editText) {
        String value = editText.getText().toString();
        return value.isEmpty() ? "null" : value;
    }

    private void initData() {
        if (mFileName != null && !mFileName.isEmpty() && !mFileName.equals("null"))
            mFileNameEditBox.setText(mFileName);
        setValueIfNotNull(controlInfoData.name, mNameEditBox);
        setValueIfNotNull(controlInfoData.version, mVersionEditBox);
        setValueIfNotNull(controlInfoData.author, mAuthorEditBox);
        setValueIfNotNull(controlInfoData.desc, mDescEditBox);
    }

    private void setValueIfNotNull(String value, EditText editText) {
        if (value != null && !value.isEmpty() && !value.equals("null")) editText.setText(value);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.mOnConfirmClickListener = listener;
    }

    public EditText getFileNameEditBox() {
        return mFileNameEditBox;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void show() {
        TextView titleView = findViewById(R.id.zh_edit_control_info_title);
        if (title != null && !title.isEmpty()) titleView.setText(title);
        super.show();
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public interface OnConfirmClickListener {
        void OnClick(String fileName, ControlInfoData controlInfoData);
    }
}