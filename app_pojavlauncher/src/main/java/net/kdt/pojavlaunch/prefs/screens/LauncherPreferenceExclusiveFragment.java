package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.os.Bundle;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;

public class LauncherPreferenceExclusiveFragment extends LauncherPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_exclusive);

        CustomSeekBarPreference seek = requirePreference("animationRate", CustomSeekBarPreference.class);
        seek.setMin(0);
        seek.setMax(1500);
        seek.setValue(DEFAULT_PREF.getInt("animationRate", 300));
        seek.setSuffix(" ms");
    }
}
