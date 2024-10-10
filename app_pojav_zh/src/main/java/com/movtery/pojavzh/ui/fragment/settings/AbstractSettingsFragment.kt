package com.movtery.pojavzh.ui.fragment.settings

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.movtery.pojavzh.event.single.SettingsChangeEvent
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class AbstractSettingsFragment(layoutId: Int) : Fragment(layoutId) {
    @Subscribe()
    fun onSettingsChange(event: SettingsChangeEvent) {
        onChange()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @CallSuper
    protected open fun onChange() {
        LauncherPreferences.loadPreferences(context)
    }
}