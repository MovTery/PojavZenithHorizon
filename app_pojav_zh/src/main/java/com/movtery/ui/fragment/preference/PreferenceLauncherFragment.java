package com.movtery.ui.fragment.preference;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.preference.Preference;

import net.kdt.pojavlaunch.MainActivity;

import com.movtery.feature.UpdateLauncher;
import com.movtery.ui.fragment.CustomBackgroundFragment;
import com.movtery.utils.CleanUpCache;
import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

public class PreferenceLauncherFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceExclusiveFragment";

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_launcher);
        Preference launcherTheme = findPreference("launcherTheme");
        if (launcherTheme != null) {
            launcherTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_tip));
                builder.setMessage(getString(R.string.zh_setting_reboot_tip));
                builder.setPositiveButton(getString(R.string.zh_confirm),
                        (dialogInterface, i) -> MainActivity.fullyExit());
                builder.setNegativeButton(getString(android.R.string.cancel), null);

                builder.show();

                return true;
            });
        }

        Preference customBackgroundPreference = requirePreference("zh_custom_background");
        customBackgroundPreference.setOnPreferenceClickListener(preference -> {
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, null, true);
            return true;
        });

        Preference cleanUpCachePreference = requirePreference("zh_clean_up_cache");
        cleanUpCachePreference.setOnPreferenceClickListener(preference -> {
            CleanUpCache.start(requireContext());
            return true;
        });

        Preference updatePreference = requirePreference("zh_check_update");
        updatePreference.setOnPreferenceClickListener(preference -> {
            UpdateLauncher.CheckDownloadedPackage(requireContext(), false);
            return true;
        });
    }
}
