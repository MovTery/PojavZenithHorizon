package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlsListViewCreator;
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class SelectControlsDialog extends FullScreenDialog {
    private ControlsListViewCreator controlsListViewCreator;

    public SelectControlsDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_select_item);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        controlsListViewCreator = new ControlsListViewCreator(getContext(), findViewById(R.id.zh_select_view));
        ImageButton mCloseButton = findViewById(R.id.zh_select_item_close_button);

        controlsListViewCreator.listAtPath();

        mCloseButton.setOnClickListener(v -> this.dismiss());
    }

    public void setOnSelectedListener(ControlSelectedListener controlSelectedListener) {
        this.controlsListViewCreator.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                controlSelectedListener.onSelectedListener(file);
                dismiss();
            }

            @Override
            public void onItemLongClick(File file, String path) {
            }
        });
    }

    public interface ControlSelectedListener {
        void onSelectedListener(File file);
    }
}
