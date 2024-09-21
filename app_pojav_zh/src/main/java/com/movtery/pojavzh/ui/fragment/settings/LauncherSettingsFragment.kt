package com.movtery.pojavzh.ui.fragment.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.UpdateLauncher
import com.movtery.pojavzh.ui.fragment.CustomBackgroundFragment
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsBaseView
import com.movtery.pojavzh.utils.CleanUpCache.Companion.start
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraCore

class LauncherSettingsFragment(val parent: FragmentWithAnim): AbstractSettingsFragment(R.layout.settings_fragment_launcher) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<SettingsBaseView>(R.id.zh_custom_background_layout)
            .setOnClickListener {
                ZHTools.swapFragmentWithAnim(
                    parent,
                    CustomBackgroundFragment::class.java,
                    CustomBackgroundFragment.TAG,
                    null
                )
            }

        view.findViewById<SettingsBaseView>(R.id.zh_clean_up_cache_layout)
            .setOnClickListener { start(requireContext()) }

        view.findViewById<SettingsBaseView>(R.id.zh_check_update_layout)
            .setOnClickListener { UpdateLauncher.CheckDownloadedPackage(requireContext(), false) }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        ExtraCore.setValue(ZHExtraConstants.PAGE_OPACITY_CHANGE, true)
    }
}