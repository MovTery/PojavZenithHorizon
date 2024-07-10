package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class MouseSettingsDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    public MouseSettingsDialog(@NonNull Context context, OnConfirmListener confirmListener, SelectMouseDialog.MouseSelectedListener mouseSelectedListener) {
        super(context);

        this.setCancelable(false);
        setContentView(R.layout.dialog_mouse_settings);
        init(confirmListener, mouseSelectedListener);
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void init(OnConfirmListener listener, SelectMouseDialog.MouseSelectedListener mouseSelectedListener) {
        Button mConfirmButton = findViewById(R.id.zh_mouse_settings_confirm_button);
        SeekBar mMouseSpeedSeekBar = findViewById(R.id.zh_mouse_settings_speed_seek);
        SeekBar mMouseScaleSeekBar = findViewById(R.id.zh_mouse_settings_scale_seek);
        TextView mMouseSpeedText = findViewById(R.id.zh_mouse_settings_speed_text);
        TextView mMouseScaleText = findViewById(R.id.zh_mouse_settings_scale_text);
        View mCustomMouseView = findViewById(R.id.zh_mouse_settings_custom_mouse);

        final int[] mouseSpeed = {DEFAULT_PREF.getInt("mousespeed", 100)};
        final int[] mouseScale = {DEFAULT_PREF.getInt("mousescale", 100)};

        mMouseSpeedSeekBar.setProgress(mouseSpeed[0]);
        mMouseScaleSeekBar.setProgress(mouseScale[0]);
        String speedText = mouseSpeed[0] + " %";
        String scaleText = mouseScale[0] + " %";
        mMouseSpeedText.setText(speedText);
        mMouseScaleText.setText(scaleText);

        //设置值
        mMouseSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mouseSpeed[0] = progress;
                String text = progress + " %";
                mMouseSpeedText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mMouseScaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mouseScale[0] = progress;
                String text = progress + " %";
                mMouseScaleText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mCustomMouseView.setOnClickListener(v -> {
            SelectMouseDialog selectMouseDialog = new SelectMouseDialog(getContext());
            selectMouseDialog.setOnSelectedListener(mouseSelectedListener);
            selectMouseDialog.show();
        });

        mConfirmButton.setOnClickListener(v -> {
            if (listener != null) listener.onConfirm(mouseSpeed[0], mouseScale[0]);
            this.dismiss();
        });
    }

    @Override
    public Window onInit() {
        return getWindow();
    }

    public interface OnConfirmListener {
        void onConfirm(int mouseSpeed, int mouseScale);
    }
}
