package com.movtery.pojavzh.ui.fragment.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.setting.AllSettings
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
            AllSettings.ignoreNotch,
            view.findViewById(R.id.ignoreNotch_layout),
            R.id.ignoreNotch_title,
            R.id.ignoreNotch_summary,
            R.id.ignoreNotch
        ).setRequiresReboot(true)
        ignoreNotch.mainView.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0)
                View.VISIBLE else View.GONE
        initSwitchView(ignoreNotch)

        initSeekBarView(
            bindSeekBarView(
                videoCategory,
                "resolutionRatio",
                AllSettings.resolutionRatio,
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
                AllSettings.sustainedPerformance,
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
                AllSettings.alternateSurface,
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
                AllSettings.forceVsync,
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
                AllSettings.vsyncInZink,
                view.findViewById(R.id.vsync_in_zink_layout),
                R.id.vsync_in_zink_title,
                R.id.vsync_in_zink_summary,
                R.id.vsync_in_zink,
            )
        )

        computeVisibility()
    }

    override fun onSettingsChange() {
        super.onSettingsChange()
        computeVisibility()
    }

    private fun computeVisibility() {
        mainView?.apply {
            findViewById<View>(R.id.force_vsync_layout).visibility =
                if (AllSettings.alternateSurface) View.VISIBLE else View.GONE
        }
    }
}