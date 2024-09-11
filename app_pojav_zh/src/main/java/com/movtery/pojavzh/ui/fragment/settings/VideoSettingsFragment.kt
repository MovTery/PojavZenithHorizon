package com.movtery.pojavzh.ui.fragment.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class VideoSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_video) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view

        val videoCategory = bindCategory(view.findViewById(R.id.video_category))

        val renderers = Tools.getCompatibleRenderers(requireContext())
        initListView(
            bindListView(
                videoCategory,
                "renderer",
                view.findViewById(R.id.renderer_layout),
                R.id.renderer_title,
                null,
                R.id.renderer_value
            ), "opengles2", renderers.rendererDisplayNames, renderers.rendererIds.toTypedArray()
        )

        val ignoreNotch = bindSwitchView(
            videoCategory,
            "ignoreNotch",
            LauncherPreferences.PREF_IGNORE_NOTCH,
            view.findViewById(R.id.ignoreNotch_layout),
            R.id.ignoreNotch_title,
            R.id.ignoreNotch_summary,
            R.id.ignoreNotch
        )
        ignoreNotch.mainView.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0)
                View.VISIBLE else View.GONE
        initSwitchView(ignoreNotch)

        initSeekBarView(
            bindSeekBarView(
                videoCategory,
                "resolutionRatio",
                LauncherPreferences.PREF_SCALE_FACTOR,
                "%",
                view.findViewById(R.id.resolutionRatio_layout),
                R.id.resolutionRatio_title,
                R.id.resolutionRatio_summary,
                R.id.resolutionRatio,
                R.id.resolutionRatio_value,
            )
        )

        initSwitchView(
            bindSwitchView(
                videoCategory,
                "sustainedPerformance",
                LauncherPreferences.PREF_SUSTAINED_PERFORMANCE,
                view.findViewById(R.id.sustainedPerformance_layout),
                R.id.sustainedPerformance_title,
                R.id.sustainedPerformance_summary,
                R.id.sustainedPerformance,
            )
        )

        initSwitchView(
            bindSwitchView(
                videoCategory,
                "alternate_surface",
                LauncherPreferences.PREF_USE_ALTERNATE_SURFACE,
                view.findViewById(R.id.alternate_surface_layout),
                R.id.alternate_surface_title,
                R.id.alternate_surface_summary,
                R.id.alternate_surface,
            )
        )

        initSwitchView(
            bindSwitchView(
                videoCategory,
                "force_vsync",
                LauncherPreferences.PREF_FORCE_VSYNC,
                view.findViewById(R.id.force_vsync_layout),
                R.id.force_vsync_title,
                R.id.force_vsync_summary,
                R.id.force_vsync,
            )
        )

        initSwitchView(
            bindSwitchView(
                videoCategory,
                "vsync_in_zink",
                LauncherPreferences.PREF_VSYNC_IN_ZINK,
                view.findViewById(R.id.vsync_in_zink_layout),
                R.id.vsync_in_zink_title,
                R.id.vsync_in_zink_summary,
                R.id.vsync_in_zink,
            )
        )

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