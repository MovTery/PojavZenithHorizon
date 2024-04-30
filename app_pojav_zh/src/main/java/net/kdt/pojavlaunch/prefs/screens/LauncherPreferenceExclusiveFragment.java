package net.kdt.pojavlaunch.prefs.screens;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.preference.Preference;

import net.kdt.pojavlaunch.R;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceExclusiveFragment";
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);
        Preference launcherTheme = findPreference("launcherTheme");
        if (launcherTheme != null) {
            launcherTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_tip));
                builder.setMessage(getString(R.string.zh_setting_reboot_tip));
                builder.setPositiveButton(getString(R.string.zh_help_ok), null);

                builder.show();

                return true;
            });
        }
    }
}
