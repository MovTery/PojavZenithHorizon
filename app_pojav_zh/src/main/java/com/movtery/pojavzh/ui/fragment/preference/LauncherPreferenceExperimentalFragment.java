package com.movtery.pojavzh.ui.fragment.preference;

import android.os.Bundle;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

public class LauncherPreferenceExperimentalFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceExperimentalFragment";

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_experimental);
    }
}
