package com.movtery.pojavzh.ui.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.UpdateLauncher
import com.movtery.pojavzh.ui.fragment.CustomBackgroundFragment
import com.movtery.pojavzh.utils.CleanUpCache.Companion.start
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class LauncherSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_launcher) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val downloadCategory = bindCategory(view.findViewById(R.id.download_category))
        val languageCategory = bindCategory(view.findViewById(R.id.language_category))
        val personalizationCategory = bindCategory(view.findViewById(R.id.personalization_category))
        val launcherCategory = bindCategory(view.findViewById(R.id.launcher_category))

        initSwitchView(
            bindSwitchView(
                downloadCategory,
                "checkLibraries",
                LauncherPreferences.PREF_CHECK_LIBRARY_SHA,
                view.findViewById(R.id.checkLibraries_layout),
                R.id.checkLibraries_title,
                R.id.checkLibraries_summary,
                R.id.checkLibraries
            )
        )

        initListView(
            bindListView(
                downloadCategory,
                "downloadSource",
                view.findViewById(R.id.downloadSource_layout),
                R.id.downloadSource_title,
                R.id.downloadSource_summary,
                R.id.downloadSource_value
            ), "default", R.array.download_source_names, R.array.download_source_values
        )

        initListView(
            bindListView(
                downloadCategory,
                "modInfoSource",
                view.findViewById(R.id.modInfoSource_layout),
                R.id.modInfoSource_title,
                null,
                R.id.modInfoSource_value
            ), "original", R.array.mod_source_names, R.array.mod_source_values
        )

        initListView(
            bindListView(
                downloadCategory,
                "modDownloadSource",
                view.findViewById(R.id.modDownloadSource_layout),
                R.id.modDownloadSource_title,
                null,
                R.id.modDownloadSource_value
            ), "original", R.array.mod_source_names, R.array.mod_source_values
        )

        initSwitchView(
            bindSwitchView(
                languageCategory,
                "autoSetGameLanguage",
                LauncherPreferences.PREF_AUTOMATICALLY_SET_GAME_LANGUAGE,
                view.findViewById(R.id.autoSetGameLanguage_layout),
                R.id.autoSetGameLanguage_title,
                R.id.autoSetGameLanguage_summary,
                R.id.autoSetGameLanguage
            )
        )

        initSwitchView(
            bindSwitchView(
                languageCategory,
                "gameLanguageOverridden",
                LauncherPreferences.PREF_GAME_LANGUAGE_OVERRIDDEN,
                view.findViewById(R.id.gameLanguageOverridden_layout),
                R.id.gameLanguageOverridden_title,
                R.id.gameLanguageOverridden_summary,
                R.id.gameLanguageOverridden
            )
        )

        initListView(
            bindListView(
                languageCategory,
                "setGameLanguage",
                view.findViewById(R.id.setGameLanguage_layout),
                R.id.setGameLanguage_title,
                null,
                R.id.setGameLanguage_value
            ),
            ZHTools.getSystemLanguage(),
            R.array.all_game_language,
            R.array.all_game_language_value
        )

        initListView(
            bindListView(
                personalizationCategory,
                "launcherTheme",
                view.findViewById(R.id.launcherTheme_layout),
                R.id.launcherTheme_title,
                null,
                R.id.launcherTheme_value
            ).setRequiresReboot(true),
            "system",
            R.array.launcher_theme_names,
            R.array.launcher_theme_values
        )

        val customBackground = bindView(
            personalizationCategory,
            view.findViewById(R.id.zh_custom_background_layout),
            R.id.zh_custom_background_title,
            R.id.zh_custom_background_summary
        )
        customBackground.mainView.setOnClickListener {
            ZHTools.swapFragmentWithAnim(
                this,
                CustomBackgroundFragment::class.java,
                CustomBackgroundFragment.TAG,
            null
            )
        }

        initSwitchView(
            bindSwitchView(
                personalizationCategory,
                "animation",
                LauncherPreferences.PREF_ANIMATION,
                view.findViewById(R.id.animation_layout),
                R.id.animation_title,
                R.id.animation_summary,
                R.id.animation
            )
        )

        initSeekBarView(
            bindSeekBarView(
                personalizationCategory,
                "animationSpeed",
                LauncherPreferences.PREF_ANIMATION_SPEED,
                "ms",
                view.findViewById(R.id.animationSpeed_layout),
                R.id.animationSpeed_title,
                R.id.animationSpeed_summary,
                R.id.animationSpeed,
                R.id.animationSpeed_value
            )
        )

        initSeekBarView(
            bindSeekBarView(
                personalizationCategory,
                "pageOpacity",
                LauncherPreferences.PREF_PAGE_OPACITY,
                "%",
                view.findViewById(R.id.pageOpacity_layout),
                R.id.pageOpacity_title,
                R.id.pageOpacity_summary,
                R.id.pageOpacity,
                R.id.pageOpacity_value
            )
        )

        initSwitchView(
            bindSwitchView(
                launcherCategory,
                "enableLogOutput",
                LauncherPreferences.PREF_ENABLE_LOG_OUTPUT,
                view.findViewById(R.id.enableLogOutput_layout),
                R.id.enableLogOutput_title,
                R.id.enableLogOutput_summary,
                R.id.enableLogOutput
            )
        )

        initSwitchView(
            bindSwitchView(
                launcherCategory,
                "quitLauncher",
                LauncherPreferences.PREF_QUILT_LAUNCHER,
                view.findViewById(R.id.quitLauncher_layout),
                R.id.quitLauncher_title,
                R.id.quitLauncher_summary,
                R.id.quitLauncher
            )
        )

        val clearUpCache = bindView(
            personalizationCategory,
            view.findViewById(R.id.zh_clean_up_cache_layout),
            R.id.zh_clean_up_cache_title,
            R.id.zh_clean_up_cache_summary
        )
        clearUpCache.mainView.setOnClickListener { start(requireContext()) }

        val checkUpdate = bindView(
            personalizationCategory,
            view.findViewById(R.id.zh_check_update_layout),
            R.id.zh_check_update_title,
            R.id.zh_check_update_summary
        )
        checkUpdate.mainView.setOnClickListener {
            UpdateLauncher.CheckDownloadedPackage(requireContext(), false)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        ExtraCore.setValue(ZHExtraConstants.PAGE_OPACITY_CHANGE, true)
    }
}