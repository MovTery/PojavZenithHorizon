package com.movtery.zalithlauncher.launch

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.movtery.zalithlauncher.feature.accounts.AccountUtils
import com.movtery.zalithlauncher.feature.accounts.AccountsManager
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.dialog.LifecycleAwareTipDialog
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import com.movtery.zalithlauncher.utils.ZHTools
import com.movtery.zalithlauncher.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.JMinecraftVersionList
import net.kdt.pojavlaunch.Logger
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.plugins.FFmpegPlugin
import net.kdt.pojavlaunch.services.GameService.LocalBinder
import net.kdt.pojavlaunch.utils.JREUtils
import net.kdt.pojavlaunch.utils.OldVersionsUtils
import net.kdt.pojavlaunch.value.MinecraftAccount
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile

class LaunchGame {
    companion object {
        @Throws(Throwable::class)
        @JvmStatic
        fun runGame(activity: AppCompatActivity, serverBinder: LocalBinder, minecraftProfile: MinecraftProfile, versionID: String, version: JMinecraftVersionList.Version) {
            Tools.LOCAL_RENDERER ?: run { Tools.LOCAL_RENDERER = AllSettings.renderer }

            if (!Tools.checkRendererCompatible(activity, Tools.LOCAL_RENDERER)) {
                val renderersList = Tools.getCompatibleRenderers(activity)
                val firstCompatibleRenderer = renderersList.rendererIds[0]
                Logging.w("runGame", "Incompatible renderer ${Tools.LOCAL_RENDERER} will be replaced with $firstCompatibleRenderer")
                Tools.LOCAL_RENDERER = firstCompatibleRenderer
                Tools.releaseCache()
            }

            val customArgs = minecraftProfile.javaArgs?.takeIf { it.isNotBlank() }
                ?: AllSettings.javaArgs?.takeIf { it.isNotBlank() }
                ?: ""
            val account = AccountsManager.getInstance().currentAccount
            printLauncherInfo(
                versionID,
                customArgs.takeIf { it.isNotBlank() } ?: "NONE",
                minecraftProfile.javaDir ?: AllSettings.defaultRuntime?.takeIf { it.isNotBlank() } ?: "NONE",
                account
            )
            JREUtils.redirectAndPrintJRELog()
            LauncherProfiles.load()

            val requiredJavaVersion = version.javaVersion?.majorVersion ?: 8
            launch(activity, account, minecraftProfile, versionID, requiredJavaVersion, customArgs)
            //Note that we actually stall in the above function, even if the game crashes. But let's be safe.
            activity.runOnUiThread { serverBinder.isActive = false }
        }

        private fun printLauncherInfo(
            gameVersion: String,
            javaArguments: String,
            javaRuntime: String,
            account: MinecraftAccount
        ) {
            fun formatJavaRuntimeString(): String {
                val prefix = Tools.LAUNCHERPROFILES_RTPREFIX
                return if (javaRuntime.startsWith(prefix)) javaRuntime.removePrefix(prefix)
                else javaRuntime
            }
            fun getLoginType(): String {
                return if (account.isMicrosoft) "Microsoft"
                else if (AccountUtils.isOtherLoginAccount(account)) "Other"
                else "Local"
            }

            Logger.appendToLog("--------- Start launching the game")
            Logger.appendToLog("Info: Launcher version: ${ZHTools.getVersionName()} (${ZHTools.getVersionCode()})")
            Logger.appendToLog("Info: Architecture: ${Architecture.archAsString(Tools.DEVICE_ARCHITECTURE)}")
            Logger.appendToLog("Info: Device model: ${StringUtils.insertSpace(Build.MANUFACTURER, Build.MODEL)}")
            Logger.appendToLog("Info: API version: ${Build.VERSION.SDK_INT}")
            Logger.appendToLog("Info: Selected Minecraft version: $gameVersion")
            Logger.appendToLog("Info: Custom Java arguments: $javaArguments")
            Logger.appendToLog("Info: Java Runtime: ${formatJavaRuntimeString()}")
            Logger.appendToLog("Info: Account: ${account.username} (${getLoginType()})")
        }

        @Throws(Throwable::class)
        @JvmStatic
        private fun launch(
            activity: AppCompatActivity,
            account: MinecraftAccount,
            minecraftProfile: MinecraftProfile,
            versionId: String,
            versionJavaRequirement: Int,
            customArgs: String
        ) {
            checkMemory(activity)

            val runtime = MultiRTUtils.forceReread(
                Tools.pickRuntime(
                activity,
                minecraftProfile,
                versionJavaRequirement))

            val versionInfo = Tools.getVersionInfo(versionId)
            LauncherProfiles.load()
            val gameDirPath = Tools.getGameDirPath(minecraftProfile)

            //预处理
            Tools.disableSplash(gameDirPath)
            OldVersionsUtils.selectOpenGlVersion(versionInfo)
            val launchClassPath = Tools.generateLaunchClassPath(versionInfo, versionId)

            val launchArgs = LaunchArgs(
                activity,
                account,
                gameDirPath,
                versionId,
                versionInfo,
                runtime,
                launchClassPath
            ).getAllArgs()

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
                val builder = TipDialog.Builder(activity)
                    .setTitle(R.string.generic_warning)
                    .setMessage(activity.getString(stringId, freeDeviceMemory, AllSettings.ramAllocation))
                    .setCenterMessage(false)
                    .setShowCancel(false)
                if (LifecycleAwareTipDialog.haltOnDialog(activity.lifecycle, builder)) return
                // If the dialog's lifecycle has ended, return without
                // actually launching the game, thus giving us the opportunity
                // to start after the activity is shown again
            }
        }
    }
}