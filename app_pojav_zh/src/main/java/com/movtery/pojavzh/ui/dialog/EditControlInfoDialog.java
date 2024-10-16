package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogEditControlInfoBinding;

public class EditControlInfoDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogEditControlInfoBinding binding = DialogEditControlInfoBinding.inflate(getLayoutInflater());
    private final ControlInfoData controlInfoData;
    private final String mFileName;
    private final boolean editFileName;
    private String title;
    private OnConfirmClickListener mOnConfirmClickListener;

    public EditControlInfoDialog(@NonNull Context context, boolean editFileName, String fileName, ControlInfoData controlInfoData) {
        super(context);

        this.editFileName = editFileName;
        this.controlInfoData = controlInfoData;
        this.mFileName = fileName;

        this.setCancelable(false);
        this.setContentView(binding.getRoot());

        initViews();
        initButtons();
        initData();
        DraggableDialog.initDialog(this);
    }

    private void initViews() {
        binding.fileNameEdit.setEnabled(editFileName);

        //设置hint
        binding.fileNameEdit.setHint(R.string.generic_required); //必填
        binding.nameEdit.setHint(R.string.generic_optional); //选填
        binding.versionEdit.setHint(R.string.generic_optional);
        binding.authorEdit.setHint(R.string.generic_optional);
        binding.descEdit.setHint(R.string.generic_optional);
    }

    private void initButtons() {
        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.confirmButton.setOnClickListener(v -> confirmClick());
    }

    private void confirmClick() {
        if (binding.fileNameEdit.getText().toString().isEmpty()) {
            binding.fileNameEdit.setError(getContext().getString(R.string.generic_error_field_empty));
            return;
        }
        updateControlInfoData();
        if (mOnConfirmClickListener != null) {
            mOnConfirmClickListener.OnClick(binding.fileNameEdit.getText().toString(), controlInfoData);
        }
    }

    private void updateControlInfoData() {
        controlInfoData.name = getValueOrDefault(binding.nameEdit);
        controlInfoData.version = getValueOrDefault(binding.versionEdit);
        controlInfoData.author = getValueOrDefault(binding.authorEdit);
        controlInfoData.desc = getValueOrDefault(binding.descEdit);
    }

    private String getValueOrDefault(EditText editText) {
        String value = editText.getText().toString();
        return value.isEmpty() ? "null" : value;
    }

    private void initData() {
        if (mFileName != null && !mFileName.isEmpty() && !mFileName.equals("null"))
            binding.fileNameEdit.setText(mFileName);
        setValueIfNotNull(controlInfoData.name, binding.nameEdit);
        setValueIfNotNull(controlInfoData.version, binding.versionEdit);
        setValueIfNotNull(controlInfoData.author, binding.authorEdit);
        setValueIfNotNull(controlInfoData.desc, binding.descEdit);
    }

    private void setValueIfNotNull(String value, EditText editText) {
        if (value != null && !value.isEmpty() && !value.equals("null")) editText.setText(value);
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.mOnConfirmClickListener = listener;
    }

    public EditText getFileNameEditBox() {
        return binding.fileNameEdit;
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