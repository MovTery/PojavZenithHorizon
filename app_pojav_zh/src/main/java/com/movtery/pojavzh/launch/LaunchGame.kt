package com.movtery.pojavzh.launch

import androidx.appcompat.app.AppCompatActivity
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.setting.AllSettings
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.lifecycle.LifecycleAwareAlertDialog
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.plugins.FFmpegPlugin
import net.kdt.pojavlaunch.utils.JREUtils
import net.kdt.pojavlaunch.utils.OldVersionsUtils
import net.kdt.pojavlaunch.value.MinecraftAccount
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import kotlin.jvm.Throws

class LaunchGame {
    companion object {
        @Throws(Throwable::class)
        @JvmStatic
        fun launch(activity: AppCompatActivity,
                   account: MinecraftAccount,
                   minecraftProfile: MinecraftProfile,
                   versionId: String,
                   versionJavaRequirement: Int) {
            checkMemory(activity)

            val runtime = MultiRTUtils.forceReread(
                Tools.pickRuntime(
                activity,
                minecraftProfile,
                versionJavaRequirement))

            val versionInfo = Tools.getVersionInfo(versionId)
            LauncherProfiles.load(ProfilePathManager.currentProfile)
            val gameDirPath = Tools.getGameDirPath(minecraftProfile)

            //预处理
            Tools.disableSplash(gameDirPath)
            OldVersionsUtils.selectOpenGlVersion(versionInfo)
            val launchClassPath = Tools.generateLaunchClassPath(versionInfo, versionId)

            val launchArgs = LaunchArgs.getAllArgs(account, gameDirPath, versionId, versionInfo, runtime, launchClassPath)
            val customArgs = AllSettings.javaArgs.takeIf { it.isNullOrBlank() } ?: minecraftProfile.javaArgs

            FFmpegPlugin.discover(activity)
            JREUtils.launchJavaVM(activity, runtime, gameDirPath, launchArgs, customArgs)
        }

        private fun checkMemory(activity: AppCompatActivity) {
            var freeDeviceMemory = Tools.getFreeDeviceMemory(activity)
            val freeAddressSpace =
                if (Architecture.is32BitsDevice())
                    Tools.getMaxContinuousAddressSpaceSize()
                else -1
            Logging.i("MemStat",
                "Free RAM: $freeDeviceMemory Addressable: $freeAddressSpace")

            val stringId: Int = if (freeDeviceMemory > freeAddressSpace && freeAddressSpace != -1) {
                freeDeviceMemory = freeAddressSpace
                R.string.address_memory_warning_msg
            } else R.string.memory_warning_msg

            if (AllSettings.ramAllocation > freeDeviceMemory) {
                val creator = LifecycleAwareAlertDialog.DialogCreator { _, builder ->
                    builder.setMessage(activity.getString(stringId, freeDeviceMemory, AllSettings.ramAllocation))
                        .setPositiveButton(android.R.string.ok, null)
                }
                if (LifecycleAwareAlertDialog.haltOnDialog(activity.lifecycle, activity, creator)) return
                // If the dialog's lifecycle has ended, return without
                // actually launching the game, thus giving us the opportunity
                // to start after the activity is shown again
            }
        }
    }
}