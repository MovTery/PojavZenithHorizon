package com.movtery.pojavzh.ui.fragment.preference;

import android.os.Bundle;

import androidx.preference.Preference;

import com.movtery.pojavzh.feature.UpdateLauncher;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.fragment.CustomBackgroundFragment;
import com.movtery.pojavzh.utils.CleanUpCache;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

public class PreferenceLauncherFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceExclusiveFragment";

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_launcher);
        Preference launcherTheme = requirePreference("launcherTheme");
        launcherTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            new TipDialog.Builder(requireContext())
                    .setMessage(R.string.zh_setting_reboot_tip)
                    .setConfirmClickListener(() -> ZHTools.restartApp(requireContext()))
                    .buildDialog();
            return true;
        });

        Preference customBackgroundPreference = requirePreference("zh_custom_background");
        customBackgroundPreference.setOnPreferenceClickListener(preference -> {
            ZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, null, true);
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
