package com.movtery.pojavzh.ui.fragment.settings

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.SettingsFragmentVideoBinding
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class VideoSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_video) {
    private lateinit var binding: SettingsFragmentVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentVideoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        val renderers = Tools.getCompatibleRenderers(context)
        ListSettingsWrapper(
            context,
            "renderer",
            "opengles2",
            binding.rendererLayout,
            binding.rendererTitle,
            binding.rendererValue,
            renderers.rendererDisplayNames,
            renderers.rendererIds.toTypedArray()
        )

        val ignoreNotch = SwitchSettingsWrapper(
            context,
            "ignoreNotch",
            AllSettings.ignoreNotch,
            binding.ignoreNotchLayout,
            binding.ignoreNotch
        )
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && LauncherPreferences.PREF_NOTCH_SIZE > 0))
            ignoreNotch.setGone()

        SeekBarSettingsWrapper(
            context,
            "resolutionRatio",
            AllSettings.resolutionRatio,
            binding.resolutionRatioLayout,
            binding.resolutionRatioTitle,
            binding.resolutionRatioSummary,
            binding.resolutionRatioValue,
            binding.resolutionRatio,
            "%"
        ).setOnSeekBarProgressChangeListener { progress ->
            changeResolutionRatioPreview(progress)
        }

        SwitchSettingsWrapper(
            context,
            "sustainedPerformance",
            AllSettings.sustainedPerformance,
            binding.sustainedPerformanceLayout,
            binding.sustainedPerformance
        )

        SwitchSettingsWrapper(
            context,
            "alternate_surface",
            AllSettings.alternateSurface,
            binding.alternateSurfaceLayout,
            binding.alternateSurface
        )

        SwitchSettingsWrapper(
            context,
            "force_vsync",
            AllSettings.forceVsync,
            binding.forceVsyncLayout,
            binding.forceVsync
        )

        SwitchSettingsWrapper(
            context,
            "vsync_in_zink",
            AllSettings.vsyncInZink,
            binding.vsyncInZinkLayout,
            binding.vsyncInZink
        )

        val zinkPreferSystemDriver = SwitchSettingsWrapper(
            context,
            "zinkPreferSystemDriver",
            AllSettings.zinkPreferSystemDriver,
            binding.zinkPreferSystemDriverLayout,
            binding.zinkPreferSystemDriver
        )
        if (!Tools.checkVulkanSupport(context.packageManager)) {
            zinkPreferSystemDriver.setGone()
        } else {
            zinkPreferSystemDriver.setOnCheckedChangeListener { buttonView, isChecked, listener ->
                if (isChecked and ZHTools.isAdrenoGPU()) {
                    TipDialog.Builder(requireActivity())
                        .setTitle(R.string.generic_warning)
                        .setMessage(R.string.setting_zink_driver_adreno)
                        .setCancelable(false)
                        .setConfirmClickListener { listener.onSave() }
                        .setCancelClickListener { buttonView.isChecked = false }
                        .buildDialog()
                } else {
                    listener.onSave()
                }
            }
        }

        changeResolutionRatioPreview(AllSettings.resolutionRatio)
        computeVisibility()
    }

    private fun changeResolutionRatioPreview(progress: Int) {
        binding.resolutionRatioPreview.text = getResolutionRatioPreview(resources, progress)
    }

    override fun onChange() {
        super.onChange()
        computeVisibility()
    }

    private fun computeVisibility() {
        binding.apply {
            binding.forceVsyncLayout.visibility = if (AllSettings.alternateSurface) View.VISIBLE else View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun getResolutionRatioPreview(resources: Resources, progress: Int): String {
            val metrics = Tools.currentDisplayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || width > height

            val progressFloat = progress.toFloat() / 100F
            val previewWidth = (Tools.getDisplayFriendlyRes((if (isLandscape) width else height), progressFloat)).toInt()
            val previewHeight = (Tools.getDisplayFriendlyRes((if (isLandscape) height else width), progressFloat)).toInt()

            return "$previewWidth x $previewHeight"
        }
    }
}