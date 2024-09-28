package com.movtery.pojavzh.ui.fragment.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class VideoSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_video) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        mainView = view

        val renderers = Tools.getCompatibleRenderers(context)
        ListSettingsWrapper(
            context,
            "renderer",
            "opengles2",
            view.findViewById(R.id.renderer_layout),
            view.findViewById(R.id.renderer_title),
            view.findViewById(R.id.renderer_value),
            renderers.rendererDisplayNames,
            renderers.rendererIds.toTypedArray()
        )

        val ignoreNotch = SwitchSettingsWrapper(
            context,
            "ignoreNotch",
            AllSettings.ignoreNotch,
            view.findViewById(R.id.ignoreNotch_layout),
            view.findViewById(R.id.ignoreNotch)
        ).setRequiresReboot()
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0))
            ignoreNotch.setGone()

        SeekBarSettingsWrapper(
            context,
            "resolutionRatio",
            AllSettings.resolutionRatio,
            view.findViewById(R.id.resolutionRatio_layout),
            view.findViewById(R.id.resolutionRatio_title),
            view.findViewById(R.id.resolutionRatio_summary),
            view.findViewById(R.id.resolutionRatio_value),
            view.findViewById(R.id.resolutionRatio),
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "sustainedPerformance",
            AllSettings.sustainedPerformance,
            view.findViewById(R.id.sustainedPerformance_layout),
            view.findViewById(R.id.sustainedPerformance)
        )

        SwitchSettingsWrapper(
            context,
            "alternate_surface",
            AllSettings.alternateSurface,
            view.findViewById(R.id.alternate_surface_layout),
            view.findViewById(R.id.alternate_surface)
        )

        SwitchSettingsWrapper(
            context,
            "force_vsync",
            AllSettings.forceVsync,
            view.findViewById(R.id.force_vsync_layout),
            view.findViewById(R.id.force_vsync)
        )

        SwitchSettingsWrapper(
            context,
            "vsync_in_zink",
            AllSettings.vsyncInZink,
            view.findViewById(R.id.vsync_in_zink_layout),
            view.findViewById(R.id.vsync_in_zink)
        )

        computeVisibility()
    }

    override fun onChange() {
        super.onChange()
        computeVisibility()
    }

    private fun computeVisibility() {
        mainView?.apply {
            findViewById<View>(R.id.force_vsync_layout).visibility =
                if (AllSettings.alternateSurface) View.VISIBLE else View.GONE
        }
    }
}