package com.movtery.pojavzh.ui.fragment.settings

import android.os.Bundle
import android.view.View
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsSwitchView
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

class MiscellaneousSettingsFragment :
    AbstractSettingsFragment(R.layout.settings_fragment_miscellaneous) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val zinkPreferSystemDriver = view.findViewById<SettingsSwitchView>(R.id.zinkPreferSystemDriver_layout)
        if (!Tools.checkVulkanSupport(requireContext().packageManager)) {
            zinkPreferSystemDriver.visibility = View.GONE
        }

        val notificationPermissionRequest = view.findViewById<SettingsSwitchView>(R.id.notification_permission_request_layout)
        val activity = requireActivity()
        if (activity is LauncherActivity) {
            notificationPermissionRequest.visibility =
                if (!activity.checkForNotificationPermission()) View.VISIBLE else View.GONE
            notificationPermissionRequest.addOnCheckedChangeListener { _, _ ->
                activity.askForNotificationPermission {
                    notificationPermissionRequest.visibility = View.GONE
                }
            }
        } else {
            notificationPermissionRequest.visibility = View.GONE
        }
    }
}