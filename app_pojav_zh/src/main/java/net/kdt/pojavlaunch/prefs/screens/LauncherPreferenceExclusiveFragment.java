package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.PojavZHTools.updateLauncher;

import android.os.Bundle;

import androidx.preference.Preference;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.fragments.CustomMouseFragment;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceExclusiveFragment";
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        Preference customMousePreference = requirePreference("zh_custom_mouse");
        customMousePreference.setOnPreferenceClickListener(preference -> {
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomMouseFragment.class, CustomMouseFragment.TAG, null, true);
            return true;
        });
    }
}
