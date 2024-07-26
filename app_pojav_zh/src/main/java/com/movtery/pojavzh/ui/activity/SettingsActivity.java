package com.movtery.pojavzh.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.ui.fragment.preference.PreferenceLauncherFragment;
import com.movtery.pojavzh.ui.fragment.preference.PreferenceExperimentalFragment;
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.BaseActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.services.ProgressServiceKeeper;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {
    private View mBackgroundView;
    private ImageButton mReturnButton, mVideoButton, mControlsButton, mJavaButton, mMiscButton, mLauncherButton, mExperimentalButton;
    private TextView mTitleView;
    private ProgressLayout mProgressLayout;
    private ProgressServiceKeeper mProgressServiceKeeper;
    private final Map<View, String> mTitle = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindViews();
        ZHTools.setBackgroundImage(this, BackgroundType.SETTINGS, mBackgroundView);

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
        mLauncherButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(PreferenceLauncherFragment.class, PreferenceLauncherFragment.TAG);
        });
        mExperimentalButton.setOnClickListener(v -> {
            onButtonClick(v);
            swapFragment(PreferenceExperimentalFragment.class, PreferenceExperimentalFragment.TAG);
        });

        ProgressKeeper.addTaskCountListener((mProgressServiceKeeper = new ProgressServiceKeeper(this)));
        ProgressKeeper.addTaskCountListener(mProgressLayout);
        mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
        mProgressLayout.observe(ProgressLayout.INSTALL_MODPACK);
        mProgressLayout.observe(ProgressLayout.AUTHENTICATE_MICROSOFT);
        mProgressLayout.observe(ProgressLayout.DOWNLOAD_VERSION_LIST);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContextExecutor.clearActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressLayout.cleanUpObservers();
        ProgressKeeper.removeTaskCountListener(mProgressLayout);
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper);
    }

    private void initialize() {
        mVideoButton.setClickable(false);
        mVideoButton.setAlpha(0.4f);

        mTitle.put(mVideoButton, getString(R.string.preference_category_video));
        mTitle.put(mControlsButton, getString(R.string.preference_category_buttons));
        mTitle.put(mJavaButton, getString(R.string.preference_category_java_tweaks));
        mTitle.put(mMiscButton, getString(R.string.preference_category_miscellaneous));
        mTitle.put(mLauncherButton, getString(R.string.zh_preference_category_launcher));
        mTitle.put(mExperimentalButton, getString(R.string.zh_preference_category_experimental));
    }

    private void onButtonClick(View view) {
        mVideoButton.setClickable(view != mVideoButton);
        mControlsButton.setClickable(view != mControlsButton);
        mJavaButton.setClickable(view != mJavaButton);
        mMiscButton.setClickable(view != mMiscButton);
        mLauncherButton.setClickable(view != mLauncherButton);
        mExperimentalButton.setClickable(view != mExperimentalButton);

        setAlpha(mVideoButton, mVideoButton.isClickable());
        setAlpha(mControlsButton, mControlsButton.isClickable());
        setAlpha(mJavaButton, mJavaButton.isClickable());
        setAlpha(mMiscButton, mMiscButton.isClickable());
        setAlpha(mLauncherButton, mLauncherButton.isClickable());
        setAlpha(mExperimentalButton, mExperimentalButton.isClickable());

        //每次切换页面都会更新标题
        mTitleView.setText(mTitle.get(view) != null ? mTitle.get(view) : getString(R.string.preference_category_video));
    }

    private void setAlpha(View button, boolean clickable) {
        if (clickable && button.getAlpha() < 1f) {
            button.animate()
                    .alpha(1f)
                    .setDuration(250);
        } else if (!clickable && button.getAlpha() > 0.4f) {
            button.animate()
                    .alpha(0.4f)
                    .setDuration(250);
        }
    }

    private void bindViews() {
        mBackgroundView = findViewById(R.id.background_view);

        mReturnButton = findViewById(R.id.settings_return_button);
        mVideoButton = findViewById(R.id.video_settings);
        mControlsButton = findViewById(R.id.controls_settings);
        mJavaButton = findViewById(R.id.java_settings);
        mMiscButton = findViewById(R.id.misc_settings);
        mLauncherButton = findViewById(R.id.launcher_settings);
        mExperimentalButton = findViewById(R.id.experimental_settings);

        mTitleView = findViewById(R.id.zh_settings_title);
        mProgressLayout = findViewById(R.id.zh_settings_progress_layout);
    }

    private void swapFragment(Class<? extends Fragment> fragmentClass, String fragmentTag) {
        ZHTools.swapSettingsFragment(this, fragmentClass, fragmentTag, null, false);
    }
}
