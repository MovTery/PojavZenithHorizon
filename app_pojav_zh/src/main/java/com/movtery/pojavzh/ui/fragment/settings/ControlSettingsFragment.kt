package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import net.kdt.pojavlaunch.databinding.SettingsFragmentControlBinding
import net.kdt.pojavlaunch.fragments.GamepadMapperFragment

class ControlSettingsFragment() : AbstractSettingsFragment(R.layout.settings_fragment_control) {
    private lateinit var binding: SettingsFragmentControlBinding
    private var parentFragment: FragmentWithAnim? = null

    constructor(parentFragment: FragmentWithAnim?) : this() {
        this.parentFragment = parentFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentControlBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        SwitchSettingsWrapper(
            context,
            "disableGestures",
            AllSettings.disableGestures,
            binding.disableGesturesLayout,
            binding.disableGestures
        )

        SwitchSettingsWrapper(
            context,
            "disableDoubleTap",
            AllSettings.disableDoubleTap,
            binding.disableDoubleTapLayout,
            binding.disableDoubleTap
        )

        SeekBarSettingsWrapper(
            context,
            "timeLongPressTrigger",
            AllSettings.timeLongPressTrigger,
            binding.timeLongPressTriggerLayout,
            binding.timeLongPressTriggerTitle,
            binding.timeLongPressTriggerSummary,
            binding.timeLongPressTriggerValue,
            binding.timeLongPressTrigger,
            "ms"
        )

        SeekBarSettingsWrapper(
            context,
            "buttonscale",
            AllSettings.buttonscale,
            binding.buttonscaleLayout,
            binding.buttonscaleTitle,
            binding.buttonscaleSummary,
            binding.buttonscaleValue,
            binding.buttonscale,
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "buttonAllCaps",
            AllSettings.buttonAllCaps,
            binding.buttonAllCapsLayout,
            binding.buttonAllCaps
        )

        SeekBarSettingsWrapper(
            context,
            "mousescale",
            AllSettings.mouseScale,
            binding.mousescaleLayout,
            binding.mousescaleTitle,
            binding.mousescaleSummary,
            binding.mousescaleValue,
            binding.mousescale,
            "%"
        )

        SeekBarSettingsWrapper(
            context,
            "mousespeed",
            AllSettings.mouseSpeed,
            binding.mousespeedLayout,
            binding.mousespeedTitle,
            binding.mousespeedSummary,
            binding.mousespeedValue,
            binding.mousespeed,
            "%"
        )

        SwitchSettingsWrapper(
            context,
            "mouse_start",
            AllSettings.virtualMouseStart,
            binding.mouseStartLayout,
            binding.mouseStart
        )

        BaseSettingsWrapper(
            context,
            binding.customMouseLayout
        ) {
            parentFragment?.apply {
                ZHTools.swapFragmentWithAnim(
                    this,
                    CustomMouseFragment::class.java,
                    CustomMouseFragment.TAG,
                    null
                )
            }
        }

        SwitchSettingsWrapper(
            context,
            "enableGyro",
            AllSettings.enableGyro,
            binding.enableGyroLayout,
            binding.enableGyro
        )

        SeekBarSettingsWrapper(
            context,
            "gyroSensitivity",
            (AllSettings.gyroSensitivity * 100).toInt(),
            binding.gyroSensitivityLayout,
            binding.gyroSensitivityTitle,
            binding.gyroSensitivitySummary,
            binding.gyroSensitivityValue,
            binding.gyroSensitivity,
            "%"
        )

        SeekBarSettingsWrapper(
            context,
            "gyroSampleRate",
            AllSettings.gyroSampleRate,
            binding.gyroSampleRateLayout,
            binding.gyroSampleRateTitle,
            binding.gyroSampleRateSummary,
            binding.gyroSampleRateValue,
            binding.gyroSampleRate,
            "ms"
        )

        SwitchSettingsWrapper(
            context,
            "gyroSmoothing",
            AllSettings.gyroSmoothing,
            binding.gyroSmoothingLayout,
            binding.gyroSmoothing
        )

        SwitchSettingsWrapper(
            context,
            "gyroInvertX",
            AllSettings.gyroInvertX,
            binding.gyroInvertXLayout,
            binding.gyroInvertX
        )

        SwitchSettingsWrapper(
            context,
            "gyroInvertY",
            AllSettings.gyroInvertY,
            binding.gyroInvertYLayout,
            binding.gyroInvertY
        )

        BaseSettingsWrapper(
            context,
            binding.changeControllerBindingsLayout
        ) {
            parentFragment?.apply {
                ZHTools.swapFragmentWithAnim(
                    this,
                    GamepadMapperFragment::class.java,
                    GamepadMapperFragment.TAG,
                    null
                )
            }
        }

        BaseSettingsWrapper(
            context,
            binding.resetControllerBindingsLayout
        ) {
            Remapper.wipePreferences(context)
            Toast.makeText(context, R.string.setting_controller_map_wiped, Toast.LENGTH_SHORT)
                .show()
        }

        SeekBarSettingsWrapper(
            context,
            "gamepad_deadzone_scale",
            (AllSettings.deadzoneScale * 100F).toInt(),
            binding.gamepadDeadzoneScaleLayout,
            binding.gamepadDeadzoneScaleTitle,
            binding.gamepadDeadzoneScaleSummary,
            binding.gamepadDeadzoneScaleValue,
            binding.gamepadDeadzoneScale,
            "%"
        )

        val mGyroAvailable =
            (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager).getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
            ) != null
        binding.enableGyroCategory.visibility = if (mGyroAvailable) View.VISIBLE else View.GONE

        computeVisibility()
    }

    override fun onChange() {
        super.onChange()
        computeVisibility()
    }

    private fun computeVisibility() {
        binding.apply {
            setViewVisibility(
                timeLongPressTriggerLayout,
                !AllSettings.disableGestures
            )
            setViewVisibility(gyroSensitivityLayout, AllSettings.enableGyro)
            setViewVisibility(gyroSampleRateLayout, AllSettings.enableGyro)
            setViewVisibility(gyroInvertXLayout, AllSettings.enableGyro)
            setViewVisibility(gyroInvertYLayout, AllSettings.enableGyro)
            setViewVisibility(gyroSmoothingLayout, AllSettings.enableGyro)
        }
    }

    private fun setViewVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}