package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.movtery.pojavzh.ui.fragment.CustomMouseFragment
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsBaseView
import com.movtery.pojavzh.utils.ZHTools
import fr.spse.gamepad_remapper.Remapper
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.fragments.GamepadMapperFragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class ControlSettingsFragment(val parent: FragmentWithAnim) : AbstractSettingsFragment(R.layout.settings_fragment_control) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view
        view.findViewById<SettingsBaseView>(R.id.zh_custom_mouse_layout)
            .setOnClickListener {
                ZHTools.swapFragmentWithAnim(
                    parent,
                    CustomMouseFragment::class.java,
                    CustomMouseFragment.TAG,
                    null
                )
            }
        view.findViewById<SettingsBaseView>(R.id.changeControllerBindings_layout)
            .setOnClickListener {
                ZHTools.swapFragmentWithAnim(
                    parent,
                    GamepadMapperFragment::class.java,
                    GamepadMapperFragment.TAG,
                    null
                )
            }
        view.findViewById<SettingsBaseView>(R.id.resetControllerBindings_layout)
            .setOnClickListener {
                Remapper.wipePreferences(context)
                Toast.makeText(context, R.string.preference_controller_map_wiped, Toast.LENGTH_SHORT)
                    .show()
            }

        val mGyroAvailable =
            (requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            ) != null
        view.findViewById<View>(R.id.enableGyro_category).visibility =
            if (mGyroAvailable) View.VISIBLE else View.GONE

        computeVisibility()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        computeVisibility()
    }

    private fun computeVisibility() {
        mainView?.apply {
            findViewById<View>(R.id.timeLongPressTrigger_layout).visibility =
                if (!LauncherPreferences.PREF_DISABLE_GESTURES) View.VISIBLE else View.GONE
        }
        mainView?.apply {
            findViewById<View>(R.id.gyroSensitivity_layout).visibility =
                if (LauncherPreferences.PREF_ENABLE_GYRO) View.VISIBLE else View.GONE
        }
        mainView?.apply {
            findViewById<View>(R.id.gyroSampleRate_layout).visibility =
                if (LauncherPreferences.PREF_ENABLE_GYRO) View.VISIBLE else View.GONE
        }
        mainView?.apply {
            findViewById<View>(R.id.gyroInvertX_layout).visibility =
                if (LauncherPreferences.PREF_ENABLE_GYRO) View.VISIBLE else View.GONE
        }
        mainView?.apply {
            findViewById<View>(R.id.gyroInvertY_layout).visibility =
                if (LauncherPreferences.PREF_ENABLE_GYRO) View.VISIBLE else View.GONE
        }
        mainView?.apply {
            findViewById<View>(R.id.gyroSmoothing_layout).visibility =
                if (LauncherPreferences.PREF_ENABLE_GYRO) View.VISIBLE else View.GONE
        }
    }
}