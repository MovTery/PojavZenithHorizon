package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.setting.Settings;

import net.kdt.pojavlaunch.R;

public class ControlSettingsDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    public ControlSettingsDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        setContentView(R.layout.dialog_control_settings);
        init();
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void init() {
        Button mConfirmButton = findViewById(R.id.zh_controls_settings_confirm_button);
        Switch mButtonSnappingSwitch = findViewById(R.id.zh_controls_settings_button_snapping);
        SeekBar mButtonSnappingDistanceSeekBar = findViewById(R.id.zh_controls_settings_button_snapping_distance);
        TextView mButtonSnappingDistanceText = findViewById(R.id.zh_controls_settings_button_snapping_distance_text);

        //设置值
        mButtonSnappingSwitch.setChecked(AllSettings.Companion.getButtonSnapping());
        mButtonSnappingDistanceSeekBar.setProgress(AllSettings.Companion.getButtonSnappingDistance());
        String text = AllSettings.Companion.getButtonSnappingDistance() + "dp";
        mButtonSnappingDistanceText.setText(text);

        mConfirmButton.setOnClickListener(v -> this.dismiss());
        mButtonSnappingSwitch.setOnCheckedChangeListener((compoundButton, b) -> Settings.Manager.Companion.put("buttonSnapping", b).save());
        mButtonSnappingDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Settings.Manager.Companion.put("buttonSnappingDistance", i).save();
                String text = i + "dp";
                mButtonSnappingDistanceText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
