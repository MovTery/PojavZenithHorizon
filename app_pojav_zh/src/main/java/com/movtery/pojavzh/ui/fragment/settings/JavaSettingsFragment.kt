package com.movtery.pojavzh.ui.fragment.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.movtery.pojavzh.ui.dialog.EditTextDialog
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
    private var allocationItem: SettingsViewWrapper? = null
    private var allocationMemory: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val javaCategory = bindCategory(view.findViewById(R.id.java_category))

        val installJre = bindView(
            javaCategory,
            view.findViewById(R.id.install_jre_layout),
            R.id.install_jre_title,
            R.id.install_jre_summary
        )
        installJre.mainView.setOnClickListener { openMultiRTDialog() }

        val javaArgs = bindView(
            javaCategory,
            view.findViewById(R.id.javaArgs_layout),
            R.id.javaArgs_title,
            R.id.javaArgs_summary
        )
        javaArgs.mainView.setOnClickListener {
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

        allocationItem = bindSeekBarView(
            javaCategory,
            "allocation",
            LauncherPreferences.PREF_RAM_ALLOCATION,
            "MB",
            view.findViewById(R.id.allocation_layout),
            R.id.allocation_title,
            R.id.allocation_summary,
            R.id.allocation,
            R.id.allocation_value
        )
        val deviceRam = Tools.getTotalDeviceMemory(requireContext())
        val maxRAM = if (Architecture.is32BitsDevice() || deviceRam < 2048) min(
            1024.0,
            deviceRam.toDouble()
        ).toInt()
        else deviceRam - (if (deviceRam < 3064) 800 else 1024) //To have a minimum for the device to breathe

        allocationItem?.apply {
            val seekBarView = getSeekBarView()
            seekBarView.max = maxRAM
            seekBarView.progress = LauncherPreferences.PREF_RAM_ALLOCATION
            setSeekBarValueTextView(
                getSeekBarValueView(),
                seekBarView.progress,
                getSeekBarValueSuffix()
            )
            initSeekBarView(this)

            allocationMemory = view.findViewById(R.id.allocation_memory)
            updateMemoryInfo(requireContext(), seekBarView.progress, allocationMemory!!)
        }

        initSwitchView(
            bindSwitchView(
                javaCategory,
                "java_sandbox",
                LauncherPreferences.PREF_JAVA_SANDBOX,
                view.findViewById(R.id.java_sandbox_layout),
                R.id.java_sandbox_title,
                R.id.java_sandbox_summary,
                R.id.java_sandbox
            )
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        updateMemoryInfo(
            requireContext(),
            allocationItem!!.getSeekBarView().progress,
            allocationMemory!!
        )
    }

    private fun updateMemoryInfo(context: Context, value: Int, textView: TextView) {
        val seekValue = value * 1024 * 1024
        val freeDeviceMemory = getFreeDeviceMemory(context)

        val isMemorySizeExceeded = seekValue > freeDeviceMemory

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