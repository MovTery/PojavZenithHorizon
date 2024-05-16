package net.kdt.pojavlaunch.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.filelist.FileListView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlSelectedListener;

import java.io.File;

public class SelectControlsDialog extends Dialog {
    private FileListView flv;

    public SelectControlsDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_select_controls);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        TextView mTitle = findViewById(R.id.zh_controls_title);
        flv = findViewById(R.id.zh_controls);
        Button mCancelButton = findViewById(R.id.zh_controls_cancel_button);

        flv.setTitleListener(mTitle::setText);

        if (Build.VERSION.SDK_INT < 29) flv.listFileAt(new File(Tools.CTRLMAP_PATH));
        else flv.lockPathAt(new File(Tools.CTRLMAP_PATH));

        mCancelButton.setOnClickListener(v -> this.dismiss());
    }

    public void setOnSelectedListener(ControlSelectedListener controlSelectedListener) {
        this.flv.setFileSelectedListener(new FileSelectedListener(){
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
