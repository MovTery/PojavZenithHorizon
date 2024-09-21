package com.movtery.pojavzh.ui.fragment.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsListView
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsSwitchView
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class VideoSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_video) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view

        val renderers = Tools.getCompatibleRenderers(requireContext())
        view.findViewById<SettingsListView>(R.id.renderer_layout)
            .setEntries(renderers.rendererDisplayNames, renderers.rendererIds.toTypedArray())

        view.findViewById<SettingsSwitchView>(R.id.ignoreNotch_layout).visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0)
                View.VISIBLE else View.GONE

        computeVisibility()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        computeVisibility()
    }

    private fun computeVisibility() {
        mainView?.apply {
            findViewById<View>(R.id.force_vsync_layout).visibility =
                if (LauncherPreferences.PREF_USE_ALTERNATE_SURFACE) View.VISIBLE else View.GONE
        }
    }
}