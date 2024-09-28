package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

class MiscellaneousSettingsFragment :
    AbstractSettingsFragment(R.layout.settings_fragment_miscellaneous) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "arc_capes",
            AllSettings.arcCapes,
            view.findViewById(R.id.arc_capes_layout),
            view.findViewById(R.id.arc_capes)
        )

        SwitchSettingsWrapper(
            context,
            "verifyManifest",
            AllSettings.verifyManifest,
            view.findViewById(R.id.verifyManifest_layout),
            view.findViewById(R.id.verifyManifest)
        )

        val zinkPreferSystemDriver = SwitchSettingsWrapper(
            context,
            "zinkPreferSystemDriver",
            AllSettings.zinkPreferSystemDriver,
            view.findViewById(R.id.zinkPreferSystemDriver_layout),
            view.findViewById(R.id.zinkPreferSystemDriver)
        )
        if (!Tools.checkVulkanSupport(context.packageManager)) {
            zinkPreferSystemDriver.setGone()
        }

        SwitchSettingsWrapper(
            context,
            "force_english",
            AllSettings.forceEnglish,
            view.findViewById(R.id.force_english_layout),
            view.findViewById(R.id.force_english)
        ).setRequiresReboot()

        val notificationPermissionRequest = SwitchSettingsWrapper(
            context,
            "notification_permission_request",
            false,
            view.findViewById(R.id.notification_permission_request_layout),
            view.findViewById(R.id.notification_permission_request)
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