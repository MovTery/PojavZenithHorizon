package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.View
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class ExperimentalSettingsFragment :
    AbstractSettingsFragment(R.layout.settings_fragment_experimental) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val experimentalCategory = bindCategory(view.findViewById(R.id.experimental_category))

        initSwitchView(
            bindSwitchView(
                experimentalCategory,
                "dump_shaders",
                LauncherPreferences.PREF_DUMP_SHADERS,
                view.findViewById(R.id.dump_shaders_layout),
                R.id.dump_shaders_title,
                R.id.dump_shaders_summary,
                R.id.dump_shaders
            )
        )

        initSwitchView(
            bindSwitchView(
                experimentalCategory,
                "bigCoreAffinity",
                LauncherPreferences.PREF_BIG_CORE_AFFINITY,
                view.findViewById(R.id.bigCoreAffinity_layout),
                R.id.bigCoreAffinity_title,
                R.id.bigCoreAffinity_summary,
                R.id.bigCoreAffinity
            )
        )
    }
}