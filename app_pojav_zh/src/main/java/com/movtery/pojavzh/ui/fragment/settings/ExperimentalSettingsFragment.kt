package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import net.kdt.pojavlaunch.R

class ExperimentalSettingsFragment :
    AbstractSettingsFragment(R.layout.settings_fragment_experimental) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "dump_shaders",
            AllSettings.dumpShaders,
            view.findViewById(R.id.dump_shaders_layout),
            view.findViewById(R.id.dump_shaders)
        )

        SwitchSettingsWrapper(
            context,
            "bigCoreAffinity",
            AllSettings.bigCoreAffinity,
            view.findViewById(R.id.bigCoreAffinity_layout),
            view.findViewById(R.id.bigCoreAffinity)
        )
    }
}