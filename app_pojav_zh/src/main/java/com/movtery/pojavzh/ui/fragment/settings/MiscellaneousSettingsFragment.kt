package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.SettingsFragmentMiscellaneousBinding

class MiscellaneousSettingsFragment :
    AbstractSettingsFragment(R.layout.settings_fragment_miscellaneous) {
    private lateinit var binding: SettingsFragmentMiscellaneousBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentMiscellaneousBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "arc_capes",
            AllSettings.arcCapes,
            binding.arcCapesLayout,
            binding.arcCapes
        )

        SwitchSettingsWrapper(
            context,
            "verifyManifest",
            AllSettings.verifyManifest,
            binding.verifyManifestLayout,
            binding.verifyManifest
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
        }

        SwitchSettingsWrapper(
            context,
            "force_english",
            AllSettings.forceEnglish,
            binding.forceEnglishLayout,
            binding.forceEnglish
        ).setRequiresReboot()

        val notificationPermissionRequest = SwitchSettingsWrapper(
            context,
            "notification_permission_request",
            false,
            binding.notificationPermissionRequestLayout,
            binding.notificationPermissionRequest
        )
        setupNotificationRequestPreference(notificationPermissionRequest)
    }

    private fun setupNotificationRequestPreference(notificationPermissionRequest: SwitchSettingsWrapper) {
        val activity = requireActivity()
        if (activity is LauncherActivity) {
            if (activity.checkForNotificationPermission()) notificationPermissionRequest.setGone()
            notificationPermissionRequest.switchView.setOnCheckedChangeListener { _, _ ->
                activity.askForNotificationPermission {
                    notificationPermissionRequest.mainView.visibility = View.GONE
                }
            }
        } else {
            notificationPermissionRequest.mainView.visibility = View.GONE
        }
    }
}