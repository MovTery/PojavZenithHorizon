package net.kdt.pojavlaunch;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExclusiveFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;

public class SettingsActivity extends BaseActivity {
    private ImageButton mReturnButton, mVideoButton, mControlsButton, mJavaButton, mMiscButton, mPojavZHButton, mExperimentalButton;
    private View mIndicator;

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
        setIndicatorPosition(mVideoButton);
    }

    private void onButtonClick(View view) {
        mVideoButton.setClickable(view == mVideoButton);
        mControlsButton.setClickable(view == mControlsButton);
        mJavaButton.setClickable(view == mJavaButton);
        mMiscButton.setClickable(view == mMiscButton);
        mPojavZHButton.setClickable(view == mPojavZHButton);
        mExperimentalButton.setClickable(view == mExperimentalButton);

        //根据被点击的按钮设置小条的位置
        setIndicatorPosition(view);
    }

    private void setIndicatorPosition(View button) {
        if (button == null) {
            return;
        }

        int[] buttonLocation = new int[2];
        button.getLocationOnScreen(buttonLocation);
        int buttonTop = buttonLocation[1] - ((ConstraintLayout.LayoutParams) button.getLayoutParams()).topMargin;
        int buttonLeft = buttonLocation[0] - ((ConstraintLayout.LayoutParams) button.getLayoutParams()).leftMargin;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mIndicator.getLayoutParams();
        params.topMargin = buttonTop;
        params.leftMargin = buttonLeft + (button.getWidth() - mIndicator.getWidth()) / 2;
        mIndicator.setLayoutParams(params);

        mIndicator.setVisibility(View.VISIBLE);
    }

    private void bindViews(){
        mReturnButton = findViewById(R.id.settings_return_button);
        mVideoButton = findViewById(R.id.video_settings);
        mControlsButton = findViewById(R.id.controls_settings);
        mJavaButton = findViewById(R.id.java_settings);
        mMiscButton = findViewById(R.id.misc_settings);
        mPojavZHButton = findViewById(R.id.pojav_zh_settings);
        mExperimentalButton = findViewById(R.id.experimental_settings);

        mIndicator = findViewById(R.id.settings_indicator);
    }

    private void swapFragment(Class<? extends Fragment> fragmentClass, String fragmentTag) {
        Tools.swapSettingsFragment(this, fragmentClass, fragmentTag, null);
    }
}
