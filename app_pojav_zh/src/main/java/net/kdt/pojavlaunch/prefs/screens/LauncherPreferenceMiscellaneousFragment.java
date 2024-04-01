package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.PojavZHTools.updateChecker;

import android.app.Activity;
import android.os.Bundle;

import androidx.preference.Preference;

import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class LauncherPreferenceMiscellaneousFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceMiscellaneousFragment";
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_misc);
        Preference driverPreference = requirePreference("zinkPreferSystemDriver");
        if(!Tools.checkVulkanSupport(driverPreference.getContext().getPackageManager())) {
            driverPreference.setVisible(false);
        }

        setupNotificationRequestPreference();

        Preference updatePreference = requirePreference("zh_check_update");
        updatePreference.setOnPreferenceClickListener(preference -> {
            updateChecker(requireContext());
            return true;
        });
    }

    private void setupNotificationRequestPreference() {
        Preference mRequestNotificationPermissionPreference = requirePreference("notification_permission_request");
        Activity activity = getActivity();
        if(activity instanceof LauncherActivity) {
            LauncherActivity launcherActivity = (LauncherActivity)activity;
            mRequestNotificationPermissionPreference.setVisible(!launcherActivity.checkForNotificationPermission());
            mRequestNotificationPermissionPreference.setOnPreferenceClickListener(preference -> {
                launcherActivity.askForNotificationPermission(()->mRequestNotificationPermissionPreference.setVisible(false));
                return true;
            });
        }else{
            mRequestNotificationPermissionPreference.setVisible(false);
        }
    }
}
