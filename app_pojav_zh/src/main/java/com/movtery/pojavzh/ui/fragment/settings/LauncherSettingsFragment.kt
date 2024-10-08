package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.UpdateLauncher
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.CustomBackgroundFragment
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.ui.fragment.settings.wrapper.BaseSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.EditTextSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.movtery.pojavzh.utils.CleanUpCache.Companion.start
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.SettingsFragmentLauncherBinding
import net.kdt.pojavlaunch.extra.ExtraCore

class LauncherSettingsFragment() : AbstractSettingsFragment(R.layout.settings_fragment_launcher) {
    private lateinit var binding: SettingsFragmentLauncherBinding
    private var parentFragment: FragmentWithAnim? = null

    constructor(parentFragment: FragmentWithAnim?) : this() {
        this.parentFragment = parentFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentLauncherBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "checkLibraries",
            AllSettings.checkLibraries,
            binding.checkLibrariesLayout,
            binding.checkLibraries
        )

        ListSettingsWrapper(
            context,
            "downloadSource",
            "default",
            binding.downloadSourceLayout,
            binding.downloadSourceTitle,
            binding.downloadSourceValue,
            R.array.download_source_names, R.array.download_source_values
        )

        ListSettingsWrapper(
            context,
            "modInfoSource",
            "original",
            binding.modInfoSourceLayout,
            binding.modInfoSourceTitle,
            binding.modInfoSourceValue,
            R.array.mod_source_names, R.array.mod_source_values
        )

        ListSettingsWrapper(
            context,
            "modDownloadSource",
            "original",
            binding.modDownloadSourceLayout,
            binding.modDownloadSourceTitle,
            binding.modDownloadSourceValue,
            R.array.mod_source_names, R.array.mod_source_values
        )

        SwitchSettingsWrapper(
            context,
            "autoSetGameLanguage",
            AllSettings.autoSetGameLanguage,
            binding.autoSetGameLanguageLayout,
            binding.autoSetGameLanguage
        )

        SwitchSettingsWrapper(
            context,
            "gameLanguageOverridden",
            AllSettings.gameLanguageOverridden,
            binding.gameLanguageOverriddenLayout,
            binding.gameLanguageOverridden
        )

        ListSettingsWrapper(
            context,
            "setGameLanguage",
            "system",
            binding.setGameLanguageLayout,
            binding.setGameLanguageTitle,
            binding.setGameLanguageValue,
            R.array.all_game_language, R.array.all_game_language_value
        )

        ListSettingsWrapper(
            context,
            "launcherTheme",
            "system",
            binding.launcherThemeLayout,
            binding.launcherThemeTitle,
            binding.launcherThemeValue,
            R.array.launcher_theme_names, R.array.launcher_theme_values
        ).setRequiresReboot()

        BaseSettingsWrapper(
            context,
            binding.customBackgroundLayout
        ) {
            parentFragment?.apply {
                ZHTools.swapFragmentWithAnim(
                    this,
                    CustomBackgroundFragment::class.java,
                    CustomBackgroundFragment.TAG,
                    null
                )
            }
        }

        SwitchSettingsWrapper(
            context,
            "animation",
            AllSettings.animation,
            binding.animationLayout,
            binding.animation
        )

        SeekBarSettingsWrapper(
            context,
            "animationSpeed",
            AllSettings.animationSpeed,
            binding.animationSpeedLayout,
            binding.animationSpeedTitle,
            binding.animationSpeedSummary,
            binding.animationSpeedValue,
            binding.animationSpeed,
            "ms"
        )

        SeekBarSettingsWrapper(
            context,
            "pageOpacity",
            AllSettings.pageOpacity,
            binding.pageOpacityLayout,
            binding.pageOpacityTitle,
            binding.pageOpacitySummary,
            binding.pageOpacityValue,
            binding.pageOpacity,
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "enableLogOutput",
            AllSettings.enableLogOutput,
            binding.enableLogOutputLayout,
            binding.enableLogOutput
        )

        SwitchSettingsWrapper(
            context,
            "quitLauncher",
            AllSettings.quitLauncher,
            binding.quitLauncherLayout,
            binding.quitLauncher
        )

        BaseSettingsWrapper(
            context,
            binding.cleanUpCacheLayout
        ) {
            start(context)
        }

        BaseSettingsWrapper(
            context,
            binding.checkUpdateLayout
        ) {
            UpdateLauncher.CheckDownloadedPackage(context, false)
        }

        SwitchSettingsWrapper(
            context,
            "gameMenuShowMemory",
            AllSettings.gameMenuShowMemory,
            binding.gameMenuShowMemoryLayout,
            binding.gameMenuShowMemory
        )

        EditTextSettingsWrapper(
            "gameMenuMemoryText",
            AllSettings.gameMenuMemoryText,
            binding.gameMenuMemoryTextLayout,
            binding.gameMenuMemoryText
        )

        SeekBarSettingsWrapper(
            context,
            "gameMenuAlpha",
            AllSettings.gameMenuAlpha,
            binding.gameMenuAlphaLayout,
            binding.gameMenuAlphaTitle,
            binding.gameMenuAlphaSummary,
            binding.gameMenuAlphaValue,
            binding.gameMenuAlpha,
            "%"
        )
    }

    override fun onChange() {
        super.onChange()
        ExtraCore.setValue(ZHExtraConstants.PAGE_OPACITY_CHANGE, true)
    }
}