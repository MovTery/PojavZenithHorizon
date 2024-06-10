package com.movtery.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.movtery.ui.subassembly.customcontrols.ControlsListView;
import com.movtery.ui.subassembly.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlSelectedListener;

import java.io.File;

public class SelectControlsDialog extends Dialog {
    private ControlsListView controlsListView;

    public SelectControlsDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_select_controls);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        controlsListView = findViewById(R.id.zh_controls);
        ImageButton mCloseButton = findViewById(R.id.zh_controls_close_button);

        controlsListView.listAtPath(new File(Tools.CTRLMAP_PATH));

        mCloseButton.setOnClickListener(v -> this.dismiss());
    }

    public void setOnSelectedListener(ControlSelectedListener controlSelectedListener) {
        this.controlsListView.setFileSelectedListener(new FileSelectedListener(){
            @Override
            public void onFileSelected(File file, String path) {
                controlSelectedListener.onSelectedListener(file);
                dismiss();
            }

            @Override
            public void onItemLongClick(File file, String path) {}
        });
    }
}
