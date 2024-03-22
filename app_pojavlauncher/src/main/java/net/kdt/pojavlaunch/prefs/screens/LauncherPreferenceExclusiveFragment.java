package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.Tools.animationRate;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        // 获取值
        int rate = DEFAULT_PREF.getInt("animationRate", 300);

        CustomSeekBarPreference seek = requirePreference("animationRate", CustomSeekBarPreference.class);
        seek.setMin(0);
        seek.setMax(1000);
        seek.setValue(rate);
        seek.setSuffix(" ms");

        seek.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                if (newValue instanceof Integer) {
                    try {
                        long rate = (long) newValue;
                        animationRate(requireContext(), rate);
                        return true;
                    } catch (NumberFormatException e) {
                        Log.e("LauncherPreferenceExclusiveFragment", "Invalid number format for animation rate: " + newValue);
                        animationRate(requireContext(), 300L);
                        return false;
                    }
                } else {
                    Log.e("LauncherPreferenceExclusiveFragment", "Unexpected type for newValue: " + newValue.getClass().getName());
                    animationRate(requireContext(), 300L);
                    return false;
                }
            }
        });
    }
}
