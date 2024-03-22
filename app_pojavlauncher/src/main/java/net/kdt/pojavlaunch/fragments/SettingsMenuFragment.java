package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;

public class SettingsMenuFragment extends Fragment {
    private ImageButton mVideoButton, mControlsButton, mJavaButton, mMiscButton, mPojavZHButton, mExperimentalButton;
    public SettingsMenuFragment() {
        super(R.layout.fragment_settings_menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);

        mVideoButton.setOnClickListener(v -> {});
        mControlsButton.setOnClickListener(v -> {});
        mJavaButton.setOnClickListener(v -> {});
        mMiscButton.setOnClickListener(v -> {});
        mPojavZHButton.setOnClickListener(v -> {});
        mExperimentalButton.setOnClickListener(v -> {});
    }

    private void bindViews(@NonNull View view){
        mVideoButton = view.findViewById(R.id.video_settings);
        mControlsButton = view.findViewById(R.id.controls_settings);
        mJavaButton = view.findViewById(R.id.java_settings);
        mMiscButton = view.findViewById(R.id.misc_settings);
        mPojavZHButton = view.findViewById(R.id.pojav_zh_settings);
        mExperimentalButton = view.findViewById(R.id.experimental_settings);
    }
}
