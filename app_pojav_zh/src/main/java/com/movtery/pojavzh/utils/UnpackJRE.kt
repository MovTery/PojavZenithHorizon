package com.movtery.pojavzh.utils

import android.content.res.AssetManager
import android.util.Log
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.IOException

class UnpackJRE {
    companion object {
        @JvmStatic
        fun unpackAllJre(assetManager: AssetManager) {
            PojavApplication.sExecutorService.execute {
                checkInternalRuntime(assetManager, InternalRuntime.JRE_8)
                checkInternalRuntime(assetManager, InternalRuntime.JRE_17)
                checkInternalRuntime(assetManager, InternalRuntime.JRE_21)
                LauncherPreferences.reloadRuntime()
            }
        }

        private fun checkInternalRuntime(assetManager: AssetManager, internalRuntime: InternalRuntime) {
            val launcherRuntimeVersion: String
            val installedRuntimeVersion =
                MultiRTUtils.readInternalRuntimeVersion(internalRuntime.jreName)
            try {
                launcherRuntimeVersion =
                    Tools.read(assetManager.open(internalRuntime.jrePath + "/version"))
            } catch (exc: IOException) {
                return
            }
            if (launcherRuntimeVersion != installedRuntimeVersion) {
                unpackInternalRuntime(assetManager, internalRuntime, launcherRuntimeVersion)
            }
        }

        private fun unpackInternalRuntime(
            assetManager: AssetManager,
            internalRuntime: InternalRuntime,
            version: String
        ) {
            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                    assetManager.open(internalRuntime.jrePath + "/universal.tar.xz"),
                    assetManager.open(
                        internalRuntime.jrePath + "/bin-" + Architecture.archAsString(
                            Tools.DEVICE_ARCHITECTURE
                        ) + ".tar.xz"
                    ),
                    internalRuntime.jreName, version
                )
                MultiRTUtils.postPrepare(internalRuntime.jreName)
            } catch (e: IOException) {
                Log.e("UnpackJREAuto", "Internal JRE unpack failed", e)
            }
        }
    }

    enum class InternalRuntime(val majorVersion: Int, val jreName: String, val jrePath: String) {
        JRE_8(8, "Internal-8", "components/jre-8"),
        JRE_17(17, "Internal-17", "components/jre-17"),
        JRE_21(21, "Internal-21", "components/jre-21")
    }
}