package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExclusiveFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceExperimentalFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;

public class SettingsActivity extends BaseActivity {
    private ImageButton mReturnButton, mVideoButton, mControlsButton, mJavaButton, mMiscButton, mPojavZHButton, mExperimentalButton;

    private void mSettingsOptionsLocked(View view) {
        mVideoButton.setClickable(view != mVideoButton);
        mControlsButton.setClickable(view != mControlsButton);
        mJavaButton.setClickable(view != mJavaButton);
        mMiscButton.setClickable(view != mMiscButton);
        mPojavZHButton.setClickable(view != mPojavZHButton);
        mExperimentalButton.setClickable(view != mExperimentalButton);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindViews();

        mReturnButton.setOnClickListener(v -> finish());

        mVideoButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceVideoFragment.class, LauncherPreferenceVideoFragment.TAG);
        });
        mControlsButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceControlFragment.class, LauncherPreferenceControlFragment.TAG);
        });
        mJavaButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceJavaFragment.class, LauncherPreferenceJavaFragment.TAG);
        });
        mMiscButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceMiscellaneousFragment.class, LauncherPreferenceMiscellaneousFragment.TAG);
        });
        mPojavZHButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceExclusiveFragment.class, LauncherPreferenceExclusiveFragment.TAG);
        });
        mExperimentalButton.setOnClickListener(v -> {
            mSettingsOptionsLocked(v);
            swapFragment(LauncherPreferenceExperimentalFragment.class, LauncherPreferenceExperimentalFragment.TAG);
        });
    }

    private void bindViews(){
        mReturnButton = findViewById(R.id.settings_return_button);
        mVideoButton = findViewById(R.id.video_settings);
        mControlsButton = findViewById(R.id.controls_settings);
        mJavaButton = findViewById(R.id.java_settings);
        mMiscButton = findViewById(R.id.misc_settings);
        mPojavZHButton = findViewById(R.id.pojav_zh_settings);
        mExperimentalButton = findViewById(R.id.experimental_settings);

        mVideoButton.setClickable(false);
    }

    private void swapFragment(Class<? extends Fragment> fragmentClass, String fragmentTag) {
        swapFragment(this, fragmentClass, fragmentTag, false, null);
    }
    public void swapFragment(FragmentActivity fragmentActivity , Class<? extends Fragment> fragmentClass,
                                    @Nullable String fragmentTag, boolean addCurrentToBackstack, @Nullable Bundle bundle) {
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();

        transaction.setReorderingAllowed(true);
        if(PREF_ANIMATION) transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);
        transaction.replace(R.id.zh_settings_fragment, fragmentClass, bundle, fragmentTag);
        if(addCurrentToBackstack) transaction.addToBackStack(null);

        transaction.commit();
    }
}
