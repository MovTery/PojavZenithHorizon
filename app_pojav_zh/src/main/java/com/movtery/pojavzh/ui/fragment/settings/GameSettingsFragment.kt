package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.settings.wrapper.BaseSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.EditTextSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.movtery.pojavzh.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getFreeDeviceMemory
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getTotalDeviceMemory
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getUsedDeviceMemory
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.databinding.SettingsFragmentGameBinding
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog
import kotlin.math.min

class GameSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_game) {
    private lateinit var binding: SettingsFragmentGameBinding
    private val mVmInstallLauncher = registerForActivityResult(
        OpenDocumentWithExtension("xz")
    ) { data: Uri? ->
        if (data != null) Tools.installRuntimeFromUri(context, data)
    }
    private var mDialogScreen: MultiRTConfigDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentGameBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        SwitchSettingsWrapper(
            context,
            "autoSetGameLanguage",
            AllSettings.autoSetGameLanguage,
            binding.autoSetGameLanguageLayout,
            binding.autoSetGameLanguage
        )

        SwitchSettingsWrapper(
            context,
            "gameLanguageOverridden",
            AllSettings.gameLanguageOverridden,
            binding.gameLanguageOverriddenLayout,
            binding.gameLanguageOverridden
        )

        ListSettingsWrapper(
            context,
            "setGameLanguage",
            "system",
            binding.setGameLanguageLayout,
            binding.setGameLanguageTitle,
            binding.setGameLanguageValue,
            R.array.all_game_language, R.array.all_game_language_value
        )

        BaseSettingsWrapper(
            context,
            binding.installJreLayout
        ) {
            openMultiRTDialog()
        }

        EditTextSettingsWrapper(
            "javaArgs",
            AllSettings.javaArgs,
            binding.javaArgsLayout,
            binding.javaArgsEdittext
        )

        val deviceRam = Tools.getTotalDeviceMemory(context)
        val maxRAM = if (Architecture.is32BitsDevice() || deviceRam < 2048) min(
            1024.0,
            deviceRam.toDouble()
        ).toInt()
        else deviceRam - (if (deviceRam < 3064) 800 else 1024) //To have a minimum for the device to breathe

        SeekBarSettingsWrapper(
            context,
            "allocation",
            AllSettings.ramAllocation,
            binding.allocationLayout,
            binding.allocationTitle,
            binding.allocationSummary,
            binding.allocationValue,
            binding.allocation,
            "MB"
        ) { wrapper ->
            wrapper.seekbarView.max = maxRAM
            wrapper.seekbarView.progress = AllSettings.ramAllocation
            wrapper.setSeekBarValueTextView()

            updateMemoryInfo(context, wrapper.seekbarView.progress.toLong())
        }.apply {
            setOnSeekBarProgressChangeListener {
                updateMemoryInfo(
                    requireContext(),
                    seekbarView.progress.toLong()
                )
            }
        }

        SwitchSettingsWrapper(
            context,
            "java_sandbox",
            AllSettings.javaSandbox,
            binding.javaSandboxLayout,
            binding.javaSandbox
        )

        SwitchSettingsWrapper(
            context,
            "gameMenuShowMemory",
            AllSettings.gameMenuShowMemory,
            binding.gameMenuShowMemoryLayout,
            binding.gameMenuShowMemory
        ).setOnCheckedChangeListener { _, _, listener ->
            listener.onSave()
            openGameMenuMemory()
        }

        EditTextSettingsWrapper(
            "gameMenuMemoryText",
            AllSettings.gameMenuMemoryText,
            binding.gameMenuMemoryTextLayout,
            binding.gameMenuMemoryText
        ).setOnTextChangedListener {
            updateGameMenuMemoryText()
        }

        ListSettingsWrapper(
            context,
            "gameMenuLocation",
            "center",
            binding.gameMenuLocationLayout,
            binding.gameMenuLocationTitle,
            binding.gameMenuLocationValue,
            R.array.game_menu_location_names, R.array.game_menu_location_values
        )

        SeekBarSettingsWrapper(
            context,
            "gameMenuAlpha",
            AllSettings.gameMenuAlpha,
            binding.gameMenuAlphaLayout,
            binding.gameMenuAlphaTitle,
            binding.gameMenuAlphaSummary,
            binding.gameMenuAlphaValue,
            binding.gameMenuAlpha,
            "%"
        ).setOnSeekBarProgressChangeListener { progress ->
            setGameMenuAlpha(progress.toFloat() / 100F)
        }

        openGameMenuMemory()
        updateGameMenuMemoryText()
        setGameMenuAlpha(AllSettings.gameMenuAlpha.toFloat() / 100F)
    }

    private fun updateMemoryInfo(context: Context, seekValue: Long) {
        val value = seekValue * 1024 * 1024
        val freeDeviceMemory = getFreeDeviceMemory(context)

        val isMemorySizeExceeded = value > freeDeviceMemory

        var summary = getMemoryInfoText(context, freeDeviceMemory)
        if (isMemorySizeExceeded) summary =
            StringUtils.insertNewline(summary, getString(R.string.setting_java_memory_exceeded))

        Tools.runOnUiThread { binding.allocationMemory.text = summary }
    }

    private fun getMemoryInfoText(context: Context, freeDeviceMemory: Long): String {
        return getString(
            R.string.setting_java_memory_info,
            formatFileSize(getUsedDeviceMemory(context)),
            formatFileSize(getTotalDeviceMemory(context)),
            formatFileSize(freeDeviceMemory)
        )
    }

    private fun openMultiRTDialog() {
        mDialogScreen ?: run {
            mDialogScreen = MultiRTConfigDialog().apply {
                prepare(context, mVmInstallLauncher)
            }
        }
        mDialogScreen?.show()
    }

    private fun openGameMenuMemory() {
        binding.gameMenuPreview.memoryText.visibility = if (AllSettings.gameMenuShowMemory) View.VISIBLE else View.GONE
    }

    private fun setGameMenuAlpha(alpha: Float) {
        binding.gameMenuPreview.root.alpha = alpha
    }

    private fun updateGameMenuMemoryText() {
        val text = "${AllSettings.gameMenuMemoryText} 0MB/0MB"
        binding.gameMenuPreview.memoryText.text = text.trim()
    }
}