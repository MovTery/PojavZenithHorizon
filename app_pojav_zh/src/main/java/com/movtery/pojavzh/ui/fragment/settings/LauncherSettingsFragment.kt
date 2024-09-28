package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.UpdateLauncher
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.CustomBackgroundFragment
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.ui.fragment.settings.wrapper.BaseSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.movtery.pojavzh.utils.CleanUpCache.Companion.start
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraCore

class LauncherSettingsFragment(val parent: FragmentWithAnim) :
    AbstractSettingsFragment(R.layout.settings_fragment_launcher) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "checkLibraries",
            AllSettings.checkLibraries,
            view.findViewById(R.id.checkLibraries_layout),
            view.findViewById(R.id.checkLibraries)
        )

        ListSettingsWrapper(
            context,
            "downloadSource",
            "default",
            view.findViewById(R.id.downloadSource_layout),
            view.findViewById(R.id.downloadSource_title),
            view.findViewById(R.id.downloadSource_value),
            R.array.download_source_names, R.array.download_source_values
        )

        ListSettingsWrapper(
            context,
            "modInfoSource",
            "original",
            view.findViewById(R.id.modInfoSource_layout),
            view.findViewById(R.id.modInfoSource_title),
            view.findViewById(R.id.modInfoSource_value),
            R.array.mod_source_names, R.array.mod_source_values
        )

        ListSettingsWrapper(
            context,
            "modDownloadSource",
            "original",
            view.findViewById(R.id.modDownloadSource_layout),
            view.findViewById(R.id.modDownloadSource_title),
            view.findViewById(R.id.modDownloadSource_value),
            R.array.mod_source_names, R.array.mod_source_values
        )

        SwitchSettingsWrapper(
            context,
            "autoSetGameLanguage",
            AllSettings.autoSetGameLanguage,
            view.findViewById(R.id.autoSetGameLanguage_layout),
            view.findViewById(R.id.autoSetGameLanguage)
        )

        SwitchSettingsWrapper(
            context,
            "gameLanguageOverridden",
            AllSettings.autoSetGameLanguage,
            view.findViewById(R.id.gameLanguageOverridden_layout),
            view.findViewById(R.id.gameLanguageOverridden)
        )

        ListSettingsWrapper(
            context,
            "setGameLanguage",
            "system",
            view.findViewById(R.id.setGameLanguage_layout),
            view.findViewById(R.id.setGameLanguage_title),
            view.findViewById(R.id.setGameLanguage_value),
            R.array.all_game_language, R.array.all_game_language_value
        )

        ListSettingsWrapper(
            context,
            "launcherTheme",
            "system",
            view.findViewById(R.id.launcherTheme_layout),
            view.findViewById(R.id.launcherTheme_title),
            view.findViewById(R.id.launcherTheme_value),
            R.array.launcher_theme_names, R.array.launcher_theme_values
        )

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.zh_custom_background_layout)
        ) {
            ZHTools.swapFragmentWithAnim(
                parent,
                CustomBackgroundFragment::class.java,
                CustomBackgroundFragment.TAG,
                null
            )
        }

        SwitchSettingsWrapper(
            context,
            "animation",
            AllSettings.animation,
            view.findViewById(R.id.animation_layout),
            view.findViewById(R.id.animation)
        )

        SeekBarSettingsWrapper(
            context,
            "animationSpeed",
            AllSettings.animationSpeed,
            view.findViewById(R.id.animationSpeed_layout),
            view.findViewById(R.id.animationSpeed_title),
            view.findViewById(R.id.animationSpeed_summary),
            view.findViewById(R.id.animationSpeed_value),
            view.findViewById(R.id.animationSpeed),
            "ms"
        )

        SeekBarSettingsWrapper(
            context,
            "pageOpacity",
            AllSettings.pageOpacity,
            view.findViewById(R.id.pageOpacity_layout),
            view.findViewById(R.id.pageOpacity_title),
            view.findViewById(R.id.pageOpacity_summary),
            view.findViewById(R.id.pageOpacity_value),
            view.findViewById(R.id.pageOpacity),
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "enableLogOutput",
            AllSettings.enableLogOutput,
            view.findViewById(R.id.enableLogOutput_layout),
            view.findViewById(R.id.enableLogOutput)
        )

        SwitchSettingsWrapper(
            context,
            "quitLauncher",
            AllSettings.quitLauncher,
            view.findViewById(R.id.quitLauncher_layout),
            view.findViewById(R.id.quitLauncher)
        )

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.zh_clean_up_cache_layout)
        ) {
            start(context)
        }

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.zh_check_update_layout)
        ) {
            UpdateLauncher.CheckDownloadedPackage(context, false)
        }
    }

    override fun onChange() {
        super.onChange()
        ExtraCore.setValue(ZHExtraConstants.PAGE_OPACITY_CHANGE, true)
    }
}