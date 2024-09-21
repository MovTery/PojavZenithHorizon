package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsBaseView
import com.movtery.pojavzh.ui.fragment.settings.view.SettingsSeekBarView
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getFreeDeviceMemory
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getTotalDeviceMemory
import com.movtery.pojavzh.utils.platform.MemoryUtils.Companion.getUsedDeviceMemory
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import kotlin.math.min

class JavaSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_java) {
    private val mVmInstallLauncher = registerForActivityResult(
        OpenDocumentWithExtension("xz")
    ) { data: Uri? ->
        if (data != null) Tools.installRuntimeFromUri(context, data)
    }
    private var mDialogScreen: MultiRTConfigDialog? = null
    private var allocation: SettingsSeekBarView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<SettingsBaseView>(R.id.install_jre_layout)
            .setOnClickListener { openMultiRTDialog() }

        view.findViewById<SettingsBaseView>(R.id.javaArgs_layout)
            .setOnClickListener {
                EditTextDialog.Builder(requireContext())
                    .setTitle(R.string.mcl_setting_title_javaargs)
                    .setMessage(R.string.mcl_setting_subtitle_javaargs)
                    .setEditText(LauncherPreferences.DEFAULT_PREF.getString("javaArgs", ""))
                    .setConfirmListener { editBox: EditText ->
                        LauncherPreferences.DEFAULT_PREF.edit()
                            .putString("javaArgs", editBox.text.toString())
                            .apply()
                        true
                    }.buildDialog()
            }

        allocation = view.findViewById(R.id.allocation_layout)
        val deviceRam = Tools.getTotalDeviceMemory(requireContext())
        val maxRAM = if (Architecture.is32BitsDevice() || deviceRam < 2048) min(
            1024.0,
            deviceRam.toDouble()
        ).toInt()
        else deviceRam - (if (deviceRam < 3064) 800 else 1024) //To have a minimum for the device to breathe

        allocation?.apply {
            setMax(maxRAM)
            updateMemoryInfo()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        updateMemoryInfo()
    }

    private fun updateMemoryInfo() {
        allocation?.apply {
            val value = getProgress().toLong() * 1024 * 1024
            val freeDeviceMemory = getFreeDeviceMemory(context)

            val isMemorySizeExceeded = value > freeDeviceMemory

            var summary = getMemoryInfoText(context, freeDeviceMemory)
            if (isMemorySizeExceeded) summary =
                StringUtils.insertNewline(summary, getString(R.string.zh_setting_java_memory_exceeded))

            Tools.runOnUiThread { setInfo(summary) }
        }
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