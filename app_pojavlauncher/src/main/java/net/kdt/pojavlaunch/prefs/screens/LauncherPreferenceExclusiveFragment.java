package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.Tools.animationRate;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION_RATE;

import android.os.Bundle;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        // 获取值
        long rate = PREF_ANIMATION_RATE;

        CustomSeekBarPreference seek = requirePreference("animationRate", CustomSeekBarPreference.class);
        seek.setMin(0);
        seek.setMax(1000);
        seek.setValue((int) rate);
        seek.setSuffix(" ms");

        seek.setOnPreferenceChangeListener((preference, newValue) -> {
            animationRate(requireContext(), (Long) newValue);
            return true;
        });
    }
}
