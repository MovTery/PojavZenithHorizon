package net.kdt.pojavlaunch;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExclusiveFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;

public class SettingsActivity extends BaseActivity {
    private ImageButton mReturnButton, mVideoButton, mControlsButton, mJavaButton, mMiscButton, mPojavZHButton, mExperimentalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindViews();

        mReturnButton.setOnClickListener(v -> finish());

        mVideoButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceVideoFragment.class, LauncherPreferenceVideoFragment.TAG);
        });
        mControlsButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceControlFragment.class, LauncherPreferenceControlFragment.TAG);
        });
        mJavaButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceJavaFragment.class, LauncherPreferenceJavaFragment.TAG);
        });
        mMiscButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceMiscellaneousFragment.class, LauncherPreferenceMiscellaneousFragment.TAG);
        });
        mPojavZHButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceExclusiveFragment.class, LauncherPreferenceExclusiveFragment.TAG);
        });
        mExperimentalButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(LauncherPreferenceExperimentalFragment.class, LauncherPreferenceExperimentalFragment.TAG);
        });

        initialize();
    }

    private void initialize() {
        mVideoButton.setClickable(false);
        mVideoButton.setAlpha(0.6f);
    }

    private void onButtonClick(View view) {
        mVideoButton.setClickable(view != mVideoButton);
        mControlsButton.setClickable(view != mControlsButton);
        mJavaButton.setClickable(view != mJavaButton);
        mMiscButton.setClickable(view != mMiscButton);
        mPojavZHButton.setClickable(view != mPojavZHButton);
        mExperimentalButton.setClickable(view != mExperimentalButton);

        setAlpha(mVideoButton, mVideoButton.isClickable());
        setAlpha(mControlsButton, mControlsButton.isClickable());
        setAlpha(mJavaButton, mJavaButton.isClickable());
        setAlpha(mMiscButton, mMiscButton.isClickable());
        setAlpha(mPojavZHButton, mPojavZHButton.isClickable());
        setAlpha(mExperimentalButton, mExperimentalButton.isClickable());
    }

    private void setAlpha(View button, boolean clickable) {
        if (clickable && button.getAlpha() != 1f) {
            button.setAlpha(0.6f);
            button.animate()
                    .alpha(1f)
                    .setDuration(300);
        } else if (!clickable && button.getAlpha() != 0.6f) {
            button.setAlpha(1f);
            button.animate()
                    .alpha(0.6f)
                    .setDuration(300);
        }
    }

    private void bindViews(){
        mReturnButton = findViewById(R.id.settings_return_button);
        mVideoButton = findViewById(R.id.video_settings);
        mControlsButton = findViewById(R.id.controls_settings);
        mJavaButton = findViewById(R.id.java_settings);
        mMiscButton = findViewById(R.id.misc_settings);
        mPojavZHButton = findViewById(R.id.pojav_zh_settings);
        mExperimentalButton = findViewById(R.id.experimental_settings);
    }

    private void swapFragment(Class<? extends Fragment> fragmentClass, String fragmentTag) {
        PojavZHTools.swapSettingsFragment(this, fragmentClass, fragmentTag, null, false);
    }
}
