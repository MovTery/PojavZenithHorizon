package net.kdt.pojavlaunch.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.movtery.customcontrols.ControlsListView;
import com.movtery.filelist.FileSelectedListener;

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
        Button mCancelButton = findViewById(R.id.zh_controls_cancel_button);

        controlsListView.listAtPath(new File(Tools.CTRLMAP_PATH));

        mCancelButton.setOnClickListener(v -> this.dismiss());
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
