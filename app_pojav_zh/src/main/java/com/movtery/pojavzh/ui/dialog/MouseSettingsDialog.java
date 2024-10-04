package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Window;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.setting.AllSettings;

import net.kdt.pojavlaunch.databinding.DialogMouseSettingsBinding;

public class MouseSettingsDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    private final DialogMouseSettingsBinding binding = DialogMouseSettingsBinding.inflate(getLayoutInflater());

    public MouseSettingsDialog(@NonNull Context context, OnConfirmListener confirmListener, SelectMouseDialog.MouseSelectedListener mouseSelectedListener) {
        super(context);

        this.setCancelable(false);
        setContentView(binding.getRoot());
        init(confirmListener, mouseSelectedListener);
        DraggableDialog.initDialog(this);
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void init(OnConfirmListener listener, SelectMouseDialog.MouseSelectedListener mouseSelectedListener) {
        final int[] mouseSpeed = {AllSettings.Companion.getMouseSpeed()};
        final int[] mouseScale = {AllSettings.Companion.getMouseScale()};

        binding.speedSeek.setProgress(mouseSpeed[0]);
        binding.scaleSeek.setProgress(mouseScale[0]);
        String speedText = mouseSpeed[0] + " %";
        String scaleText = mouseScale[0] + " %";
        binding.speedText.setText(speedText);
        binding.scaleText.setText(scaleText);

        //设置值
        binding.speedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mouseSpeed[0] = progress;
                String text = progress + " %";
                binding.speedText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.scaleSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mouseScale[0] = progress;
                String text = progress + " %";
                binding.scaleText.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.customMouse.setOnClickListener(v -> {
            SelectMouseDialog selectMouseDialog = new SelectMouseDialog(getContext());
            selectMouseDialog.setOnSelectedListener(mouseSelectedListener);
            selectMouseDialog.show();
        });

        binding.confirmButton.setOnClickListener(v -> {
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
