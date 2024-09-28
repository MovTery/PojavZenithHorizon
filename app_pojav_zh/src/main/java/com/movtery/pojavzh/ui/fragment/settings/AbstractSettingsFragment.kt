package com.movtery.pojavzh.ui.fragment.settings

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.movtery.pojavzh.setting.OnSettingsChangeListener
import com.movtery.pojavzh.setting.Settings
import net.kdt.pojavlaunch.prefs.LauncherPreferences

abstract class AbstractSettingsFragment(layoutId: Int) : Fragment(layoutId),
    OnSettingsChangeListener {
    override fun onResume() {
        super.onResume()
        Settings.Manager.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Settings.Manager.removeListener(this)
    }

    @CallSuper
    override fun onChange() {
        LauncherPreferences.loadPreferences(context)
    }
}