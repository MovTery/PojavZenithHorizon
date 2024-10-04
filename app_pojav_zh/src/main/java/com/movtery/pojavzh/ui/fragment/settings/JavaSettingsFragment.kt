package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.setting.Settings
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.fragment.settings.wrapper.BaseSettingsWrapper
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
import net.kdt.pojavlaunch.databinding.SettingsFragmentJavaBinding
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog
import kotlin.math.min

class JavaSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_java) {
    private lateinit var binding: SettingsFragmentJavaBinding
    private val mVmInstallLauncher = registerForActivityResult(
        OpenDocumentWithExtension("xz")
    ) { data: Uri? ->
        if (data != null) Tools.installRuntimeFromUri(context, data)
    }
    private var mDialogScreen: MultiRTConfigDialog? = null
    private var allocationItem: SeekBarSettingsWrapper? = null
    private var allocationMemory: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentJavaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        BaseSettingsWrapper(
            context,
            binding.installJreLayout
        ) {
            openMultiRTDialog()
        }

        BaseSettingsWrapper(
            context,
            binding.javaArgsLayout
        ) {
            EditTextDialog.Builder(context)
                .setTitle(R.string.mcl_setting_title_javaargs)
                .setMessage(R.string.mcl_setting_subtitle_javaargs)
                .setEditText(AllSettings.javaArgs)
                .setConfirmListener { editBox: EditText ->
                    Settings.Manager.put("javaArgs", editBox.text.toString())
                        .save()
                    true
                }.buildDialog()
        }

        val deviceRam = Tools.getTotalDeviceMemory(context)
        val maxRAM = if (Architecture.is32BitsDevice() || deviceRam < 2048) min(
            1024.0,
            deviceRam.toDouble()
        ).toInt()
        else deviceRam - (if (deviceRam < 3064) 800 else 1024) //To have a minimum for the device to breathe

        allocationItem = SeekBarSettingsWrapper(
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

            allocationMemory = binding.allocationMemory
            updateMemoryInfo(context, wrapper.seekbarView.progress.toLong(), allocationMemory!!)
        }

        SwitchSettingsWrapper(
            context,
            "java_sandbox",
            AllSettings.javaSandbox,
            binding.javaSandboxLayout,
            binding.javaSandbox
        )
    }

    override fun onChange() {
        super.onChange()
        updateMemoryInfo(
            requireContext(),
            allocationItem!!.seekbarView.progress.toLong(),
            allocationMemory!!
        )
    }

    private fun updateMemoryInfo(context: Context, seekValue: Long, textView: TextView) {
        val value = seekValue * 1024 * 1024
        val freeDeviceMemory = getFreeDeviceMemory(context)

        val isMemorySizeExceeded = value > freeDeviceMemory

        var summary = getMemoryInfoText(context, freeDeviceMemory)
        if (isMemorySizeExceeded) summary =
            StringUtils.insertNewline(summary, getString(R.string.zh_setting_java_memory_exceeded))

        Tools.runOnUiThread { textView.text = summary }
    }

    private fun getMemoryInfoText(context: Context, freeDeviceMemory: Long): String {
        return getString(
            R.string.zh_setting_java_memory_info,
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
}