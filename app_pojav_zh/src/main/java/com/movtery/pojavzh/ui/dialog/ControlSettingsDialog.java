package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
        mButtonSnappingSwitch.setChecked(DEFAULT_PREF.getBoolean("buttonSnapping", true));
        mButtonSnappingDistanceSeekBar.setProgress(DEFAULT_PREF.getInt("buttonSnappingDistance", 8));
        String text = DEFAULT_PREF.getInt("buttonSnappingDistance", 8) + "dp";
        mButtonSnappingDistanceText.setText(text);

        mConfirmButton.setOnClickListener(v -> this.dismiss());
        mButtonSnappingSwitch.setOnCheckedChangeListener((compoundButton, b) -> DEFAULT_PREF.edit().putBoolean("buttonSnapping", b).apply());
        mButtonSnappingDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                DEFAULT_PREF.edit().putInt("buttonSnappingDistance", i).apply();
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
