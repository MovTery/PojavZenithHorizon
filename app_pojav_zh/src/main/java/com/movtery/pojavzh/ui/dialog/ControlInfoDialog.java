package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;
import com.movtery.pojavzh.ui.subassembly.customcontrols.EditControlData;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.customcontrols.CustomControls;

import java.io.File;

public class ControlInfoDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final ControlInfoData controlInfoData;
    private final Runnable runnable;

    public ControlInfoDialog(@NonNull Context context, Runnable runnable, ControlInfoData controlInfoData) {
        super(context);
        this.runnable = runnable;
        this.controlInfoData = controlInfoData;

        setCancelable(false);
        setContentView(R.layout.dialog_control_info);

        init(context);
        DraggableDialog.initDialog(this);
    }

    private void init(Context context) {
        setTextOrDefault(R.id.zh_control_info_name_text, R.string.zh_controls_info_name, controlInfoData.name);
        setTextOrDefault(R.id.zh_control_info_file_name_text, R.string.zh_controls_info_file_name, controlInfoData.fileName);
        setTextOrDefault(R.id.zh_control_info_author_text, R.string.zh_controls_info_author, controlInfoData.author);
        setTextOrDefault(R.id.zh_control_info_version_text, R.string.zh_controls_info_version, controlInfoData.version);
        setTextOrDefault(R.id.zh_control_info_desc_text, R.string.zh_controls_info_desc, controlInfoData.desc);

        Button closeButton = findViewById(R.id.zh_control_info_close);
        ImageButton editButton = findViewById(R.id.zh_control_info_edit);

        closeButton.setOnClickListener(v -> this.dismiss());
        editButton.setOnClickListener(v -> {
            EditControlInfoDialog editControlInfoDialog = new EditControlInfoDialog(context, false, controlInfoData.fileName, controlInfoData);
            editControlInfoDialog.setTitle(context.getString(R.string.zh_edit));
            editControlInfoDialog.setOnConfirmClickListener((fileName, controlInfoData) -> {
                File controlFile = new File(PathAndUrlManager.DIR_CTRLMAP_PATH, fileName);

                CustomControls customControls = EditControlData.loadCustomControlsFromFile(context, controlFile);

                if (customControls != null) {
                    customControls.mControlInfoDataList.name = controlInfoData.name;
                    customControls.mControlInfoDataList.author = controlInfoData.author;
                    customControls.mControlInfoDataList.version = controlInfoData.version;
                    customControls.mControlInfoDataList.desc = controlInfoData.desc;

                    EditControlData.saveToFile(context, customControls, controlFile);
                }

                PojavApplication.sExecutorService.execute(runnable);

                editControlInfoDialog.dismiss();
            });
            editControlInfoDialog.show();
            this.dismiss();
        });
    }

    private void setTextOrDefault(int textViewId, int stringId, String value) {
        TextView textView = findViewById(textViewId);
        String text = getContext().getString(stringId);
        text += " ";
        if (value != null && !value.isEmpty() && !value.equals("null")) {
            text += value;
        } else {
            text += getContext().getString(R.string.zh_unknown);
        }
        textView.setText(text);
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}