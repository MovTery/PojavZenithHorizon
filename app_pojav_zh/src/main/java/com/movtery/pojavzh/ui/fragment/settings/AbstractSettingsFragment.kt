package com.movtery.pojavzh.ui.fragment.settings

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF

abstract class AbstractSettingsFragment(layoutId: Int) : Fragment(layoutId),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onResume() {
        super.onResume()
        DEFAULT_PREF.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        DEFAULT_PREF.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LauncherPreferences.loadPreferences(requireContext())
    }
}