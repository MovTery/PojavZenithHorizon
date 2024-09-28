package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.CustomMouseFragment
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.ui.fragment.settings.wrapper.BaseSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.movtery.pojavzh.utils.ZHTools
import fr.spse.gamepad_remapper.Remapper
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.fragments.GamepadMapperFragment

class ControlSettingsFragment(val parent: FragmentWithAnim) :
    AbstractSettingsFragment(R.layout.settings_fragment_control) {
    private var mainView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        mainView = view

        SwitchSettingsWrapper(
            context,
            "disableGestures",
            AllSettings.disableGestures,
            view.findViewById(R.id.disableGestures_layout),
            view.findViewById(R.id.disableGestures)
        )

        SwitchSettingsWrapper(
            context,
            "disableDoubleTap",
            AllSettings.disableDoubleTap,
            view.findViewById(R.id.disableDoubleTap_layout),
            view.findViewById(R.id.disableDoubleTap)
        )

        SeekBarSettingsWrapper(
            context,
            "timeLongPressTrigger",
            AllSettings.timeLongPressTrigger,
            view.findViewById(R.id.timeLongPressTrigger_layout),
            view.findViewById(R.id.timeLongPressTrigger_title),
            view.findViewById(R.id.timeLongPressTrigger_summary),
            view.findViewById(R.id.timeLongPressTrigger_value),
            view.findViewById(R.id.timeLongPressTrigger),
            "ms"
        )

        SeekBarSettingsWrapper(
            context,
            "buttonscale",
            AllSettings.buttonscale,
            view.findViewById(R.id.buttonscale_layout),
            view.findViewById(R.id.buttonscale_title),
            view.findViewById(R.id.buttonscale_summary),
            view.findViewById(R.id.buttonscale_value),
            view.findViewById(R.id.buttonscale),
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "buttonAllCaps",
            AllSettings.buttonAllCaps,
            view.findViewById(R.id.buttonAllCaps_layout),
            view.findViewById(R.id.buttonAllCaps)
        )

        SeekBarSettingsWrapper(
            context,
            "mousescale",
            AllSettings.mouseScale,
            view.findViewById(R.id.mousescale_layout),
            view.findViewById(R.id.mousescale_title),
            view.findViewById(R.id.mousescale_summary),
            view.findViewById(R.id.mousescale_value),
            view.findViewById(R.id.mousescale),
            "%"
        )

        SeekBarSettingsWrapper(
            context,
            "mousespeed",
            AllSettings.mouseSpeed,
            view.findViewById(R.id.mousespeed_layout),
            view.findViewById(R.id.mousespeed_title),
            view.findViewById(R.id.mousespeed_summary),
            view.findViewById(R.id.mousespeed_value),
            view.findViewById(R.id.mousespeed),
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "mouse_start",
            AllSettings.virtualMouseStart,
            view.findViewById(R.id.mouse_start_layout),
            view.findViewById(R.id.mouse_start)
        )

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.zh_custom_mouse_layout)
        ) {
            ZHTools.swapFragmentWithAnim(
                parent,
                CustomMouseFragment::class.java,
                CustomMouseFragment.TAG,
                null
            )
        }

        SwitchSettingsWrapper(
            context,
            "enableGyro",
            AllSettings.enableGyro,
            view.findViewById(R.id.enableGyro_layout),
            view.findViewById(R.id.enableGyro)
        )

        SeekBarSettingsWrapper(
            context,
            "gyroSensitivity",
            (AllSettings.gyroSensitivity * 100).toInt(),
            view.findViewById(R.id.gyroSensitivity_layout),
            view.findViewById(R.id.gyroSensitivity_title),
            view.findViewById(R.id.gyroSensitivity_summary),
            view.findViewById(R.id.gyroSensitivity_value),
            view.findViewById(R.id.gyroSensitivity),
            "%"
        )

        SeekBarSettingsWrapper(
            context,
            "gyroSampleRate",
            AllSettings.gyroSampleRate,
            view.findViewById(R.id.gyroSampleRate_layout),
            view.findViewById(R.id.gyroSampleRate_title),
            view.findViewById(R.id.gyroSampleRate_summary),
            view.findViewById(R.id.gyroSampleRate_value),
            view.findViewById(R.id.gyroSampleRate),
            "ms"
        )

        SwitchSettingsWrapper(
            context,
            "gyroSmoothing",
            AllSettings.gyroSmoothing,
            view.findViewById(R.id.gyroSmoothing_layout),
            view.findViewById(R.id.gyroSmoothing)
        )

        SwitchSettingsWrapper(
            context,
            "gyroInvertX",
            AllSettings.gyroInvertX,
            view.findViewById(R.id.gyroInvertX_layout),
            view.findViewById(R.id.gyroInvertX)
        )

        SwitchSettingsWrapper(
            context,
            "gyroInvertY",
            AllSettings.gyroInvertY,
            view.findViewById(R.id.gyroInvertY_layout),
            view.findViewById(R.id.gyroInvertY)
        )

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.changeControllerBindings_layout)
        ) {
            ZHTools.swapFragmentWithAnim(
                parent,
                GamepadMapperFragment::class.java,
                GamepadMapperFragment.TAG,
                null
            )
        }

        BaseSettingsWrapper(
            context,
            view.findViewById(R.id.resetControllerBindings_layout)
        ) {
            Remapper.wipePreferences(context)
            Toast.makeText(context, R.string.preference_controller_map_wiped, Toast.LENGTH_SHORT)
                .show()
        }

        SeekBarSettingsWrapper(
            context,
            "gamepad_deadzone_scale",
            (AllSettings.deadzoneScale * 100F).toInt(),
            view.findViewById(R.id.gamepad_deadzone_scale_layout),
            view.findViewById(R.id.gamepad_deadzone_scale_title),
            view.findViewById(R.id.gamepad_deadzone_scale_summary),
            view.findViewById(R.id.gamepad_deadzone_scale_value),
            view.findViewById(R.id.gamepad_deadzone_scale),
            "%"
        )

        val mGyroAvailable =
            (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            ) != null
        view.findViewById<View>(R.id.enableGyro_category)
            .visibility = if (mGyroAvailable) View.VISIBLE else View.GONE

        computeVisibility()
    }

    override fun onChange() {
        super.onChange()
        computeVisibility()
    }

    private fun computeVisibility() {
        mainView?.apply {
            setViewVisibility(
                findViewById(R.id.timeLongPressTrigger_layout),
                !AllSettings.disableGestures
            )
            setViewVisibility(findViewById(R.id.gyroSensitivity_layout), AllSettings.enableGyro)
            setViewVisibility(findViewById(R.id.gyroSampleRate_layout), AllSettings.enableGyro)
            setViewVisibility(findViewById(R.id.gyroInvertX_layout), AllSettings.enableGyro)
            setViewVisibility(findViewById(R.id.gyroInvertY_layout), AllSettings.enableGyro)
            setViewVisibility(findViewById(R.id.gyroSmoothing_layout), AllSettings.enableGyro)
        }
    }

    private fun setViewVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}