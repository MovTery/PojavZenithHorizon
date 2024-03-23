package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import net.kdt.pojavlaunch.fragments.ControlButtonFragment;
import net.kdt.pojavlaunch.fragments.settings.ExperimentalSettingsFragment;
import net.kdt.pojavlaunch.fragments.settings.JavaSettingsFragment;
import net.kdt.pojavlaunch.fragments.settings.MiscSettingsFragment;
import net.kdt.pojavlaunch.fragments.settings.PojavZHSettingsFragment;
import net.kdt.pojavlaunch.fragments.settings.VideoSettingsFragment;

public class SettingsActivity extends BaseActivity {
    private ImageButton mVideoButton, mControlsButton, mJavaButton, mMiscButton, mPojavZHButton, mExperimentalButton;
    private ImageButton mReturnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindViews();

        mReturnButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.activity_cut_into, R.anim.activity_cut_out);
        });

        mVideoButton.setOnClickListener(v -> swapFragment(VideoSettingsFragment.class, VideoSettingsFragment.TAG));
        mControlsButton.setOnClickListener(v -> swapFragment(ControlButtonFragment.class, ControlButtonFragment.TAG));
        mJavaButton.setOnClickListener(v -> swapFragment(JavaSettingsFragment.class, JavaSettingsFragment.TAG));
        mMiscButton.setOnClickListener(v -> swapFragment(MiscSettingsFragment.class, MiscSettingsFragment.TAG));
        mPojavZHButton.setOnClickListener(v -> swapFragment(PojavZHSettingsFragment.class, PojavZHSettingsFragment.TAG));
        mExperimentalButton.setOnClickListener(v -> swapFragment(ExperimentalSettingsFragment.class, ExperimentalSettingsFragment.TAG));
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
