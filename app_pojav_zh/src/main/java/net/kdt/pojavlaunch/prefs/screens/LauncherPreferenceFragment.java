package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.SlideAnimation;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * Preference for the main screen, any sub-screen should inherit this class for consistent behavior,
 * overriding only onCreatePreferences
 */
public abstract class LauncherPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, SlideAnimation {
    private View mMainView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;
        view.setBackgroundResource(R.drawable.background_card);

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        DEFAULT_PREF.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        LauncherPreferences.loadPreferences(getContext());
    }

    protected Preference requirePreference(CharSequence key) {
        Preference preference = findPreference(key);
        if(preference != null) return preference;
        throw new IllegalStateException("Preference "+key+" is null");
    }
    @SuppressWarnings("unchecked")
    protected <T extends Preference> T requirePreference(CharSequence key, Class<T> preferenceClass) {
        Preference preference = requirePreference(key);
        if(preferenceClass.isInstance(preference)) return (T)preference;
        throw new IllegalStateException("Preference "+key+" is not an instance of "+preferenceClass.getSimpleName());
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.BounceInDown);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.FadeOutUp);
    }
}
