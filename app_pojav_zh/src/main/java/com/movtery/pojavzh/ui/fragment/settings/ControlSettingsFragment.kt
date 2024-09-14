package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.movtery.pojavzh.ui.fragment.CustomMouseFragment
import com.movtery.pojavzh.utils.ZHTools
import fr.spse.gamepad_remapper.Remapper
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.fragments.GamepadMapperFragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class ControlSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_control) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view

        val customControlsCategory = bindCategory(view.findViewById(R.id.custom_controls_category))
        val controlsCategory = bindCategory(view.findViewById(R.id.controls_category))
        val mouseCategory = bindCategory(view.findViewById(R.id.mouse_category))
        val gyroCategory = bindCategory(view.findViewById(R.id.enableGyro_category))
        val controllerCategory = bindCategory(view.findViewById(R.id.controller_category))

        initSwitchView(
            bindSwitchView(
                customControlsCategory,
                "disableGestures",
                LauncherPreferences.PREF_DISABLE_GESTURES,
                view.findViewById(R.id.disableGestures_layout),
                R.id.disableGestures_title,
                R.id.disableGestures_summary,
                R.id.disableGestures
            )
        )

        initSwitchView(
            bindSwitchView(
                customControlsCategory,
                "disableDoubleTap",
                LauncherPreferences.PREF_DISABLE_SWAP_HAND,
                view.findViewById(R.id.disableDoubleTap_layout),
                R.id.disableDoubleTap_title,
                R.id.disableDoubleTap_summary,
                R.id.disableDoubleTap
            )
        )
        initSeekBarView(
            bindSeekBarView(
                customControlsCategory,
                "timeLongPressTrigger",
                LauncherPreferences.PREF_LONGPRESS_TRIGGER,
                "ms",
                view.findViewById(R.id.timeLongPressTrigger_layout),
                R.id.timeLongPressTrigger_title,
                R.id.timeLongPressTrigger_summary,
                R.id.timeLongPressTrigger,
                R.id.timeLongPressTrigger_value
            )
        )
        initSeekBarView(
            bindSeekBarView(
                controlsCategory,
                "buttonscale",
                LauncherPreferences.PREF_BUTTONSIZE.toInt(),
                "%",
                view.findViewById(R.id.buttonscale_layout),
                R.id.buttonscale_title,
                R.id.buttonscale_summary,
                R.id.buttonscale,
                R.id.buttonscale_value
            )
        )
        initSwitchView(
            bindSwitchView(
                controlsCategory,
                "buttonAllCaps",
                LauncherPreferences.PREF_BUTTON_ALL_CAPS,
                view.findViewById(R.id.buttonAllCaps_layout),
                R.id.buttonAllCaps_title,
                R.id.buttonAllCaps_summary,
                R.id.buttonAllCaps
            )
        )
        initSeekBarView(
            bindSeekBarView(
                mouseCategory,
                "mousescale",
                LauncherPreferences.DEFAULT_PREF.getInt("mousescale", 100),
                "%",
                view.findViewById(R.id.mousescale_layout),
                R.id.mousescale_title,
                R.id.mousescale_summary,
                R.id.mousescale,
                R.id.mousescale_value
            )
        )
        initSeekBarView(
            bindSeekBarView(
                mouseCategory,
                "mousespeed",
                LauncherPreferences.DEFAULT_PREF.getInt("mousespeed", 100),
                "%",
                view.findViewById(R.id.mousespeed_layout),
                R.id.mousespeed_title,
                R.id.mousespeed_summary,
                R.id.mousespeed,
                R.id.mousespeed_value
            )
        )
        initSwitchView(
            bindSwitchView(
                mouseCategory,
                "mouse_start",
                LauncherPreferences.PREF_VIRTUAL_MOUSE_START,
                view.findViewById(R.id.mouse_start_layout),
                R.id.mouse_start_title,
                R.id.mouse_start_summary,
                R.id.mouse_start
            )
        )
        val customMouse = bindView(
            mouseCategory,
            view.findViewById(R.id.zh_custom_mouse_layout),
            R.id.zh_custom_mouse_title,
            R.id.zh_custom_mouse_summary
        )
        customMouse.mainView.setOnClickListener {
            ZHTools.swapFragmentWithAnim(
                this,
                CustomMouseFragment::class.java,
                CustomMouseFragment.TAG,
                null
            )
        }
        initSwitchView(
            bindSwitchView(
                gyroCategory,
                "enableGyro",
                LauncherPreferences.PREF_ENABLE_GYRO,
                view.findViewById(R.id.enableGyro_layout),
                R.id.enableGyro_title,
                R.id.enableGyro_summary,
                R.id.enableGyro
            )
        )
        initSeekBarView(
            bindSeekBarView(
                gyroCategory,
                "gyroSensitivity",
                (LauncherPreferences.PREF_GYRO_SENSITIVITY * 100).toInt(),
                "%",
                view.findViewById(R.id.gyroSensitivity_layout),
                R.id.gyroSensitivity_title,
                R.id.gyroSensitivity_summary,
                R.id.gyroSensitivity,
                R.id.gyroSensitivity_value
            )
        )
        initSeekBarView(
            bindSeekBarView(
                gyroCategory,
                "gyroSampleRate",
                LauncherPreferences.PREF_GYRO_SAMPLE_RATE,
                "ms",
                view.findViewById(R.id.gyroSampleRate_layout),
                R.id.gyroSampleRate_title,
                R.id.gyroSampleRate_summary,
                R.id.gyroSampleRate,
                R.id.gyroSampleRate_value
            )
        )
        initSwitchView(
            bindSwitchView(
                gyroCategory,
                "gyroSmoothing",
                LauncherPreferences.PREF_GYRO_SMOOTHING,
                view.findViewById(R.id.gyroSmoothing_layout),
                R.id.gyroSmoothing_title,
                R.id.gyroSmoothing_summary,
                R.id.gyroSmoothing
            )
        )
        initSwitchView(
            bindSwitchView(
                gyroCategory,
                "gyroInvertX",
                LauncherPreferences.PREF_GYRO_INVERT_X,
                view.findViewById(R.id.gyroInvertX_layout),
                R.id.gyroInvertX_title,
                R.id.gyroInvertX_summary,
                R.id.gyroInvertX
            )
        )
        initSwitchView(
            bindSwitchView(
                gyroCategory,
                "gyroInvertY",
                LauncherPreferences.PREF_GYRO_INVERT_Y,
                view.findViewById(R.id.gyroInvertY_layout),
                R.id.gyroInvertY_title,
                R.id.gyroInvertY_summary,
                R.id.gyroInvertY
            )
        )
        val changeControllerBindings = bindView(
            controllerCategory,
            view.findViewById(R.id.changeControllerBindings_layout),
            R.id.changeControllerBindings_title,
            R.id.changeControllerBindings_summary
        )
        changeControllerBindings.mainView.setOnClickListener {
            ZHTools.swapFragmentWithAnim(
                this,
                GamepadMapperFragment::class.java,
                GamepadMapperFragment.TAG,
                null
            )
        }

        val resetControllerBindings = bindView(
            controllerCategory,
            view.findViewById(R.id.resetControllerBindings_layout),
            R.id.resetControllerBindings_title,
            R.id.resetControllerBindings_summary
        )
        resetControllerBindings.mainView.setOnClickListener {
            Remapper.wipePreferences(context)
            Toast.makeText(context, R.string.preference_controller_map_wiped, Toast.LENGTH_SHORT)
                .show()
        }
        initSeekBarView(
            bindSeekBarView(
                controllerCategory,
                "gamepad_deadzone_scale",
                (LauncherPreferences.PREF_DEADZONE_SCALE * 100F).toInt(),
                "%",
                view.findViewById(R.id.gamepad_deadzone_scale_layout),
                R.id.gamepad_deadzone_scale_title,
                R.id.gamepad_deadzone_scale_summary,
                R.id.gamepad_deadzone_scale,
                R.id.gamepad_deadzone_scale_value
            )
        )

        val mGyroAvailable =
            (requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            ) != null
        gyroCategory.setVisibility(mGyroAvailable)

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