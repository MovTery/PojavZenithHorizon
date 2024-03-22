package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.os.Bundle;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        int rate = LauncherPreferences.PREF_ANIMATION_RATE;

        CustomSeekBarPreference seek = requirePreference("animationRate", CustomSeekBarPreference.class);
        seek.setRange(0, 1500);
        seek.setValue(rate);
        seek.setSuffix(" ms");
    }
}
