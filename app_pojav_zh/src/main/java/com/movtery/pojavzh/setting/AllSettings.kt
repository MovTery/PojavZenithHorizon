package com.movtery.pojavzh.setting

import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class AllSettings {
    companion object {
        val currentProfile: String?
            get() = Settings.Manager.getString("currentProfile", "")

        val launcherProfile: String?
            get() = Settings.Manager.getString("launcherProfile", "default")

        val renderer: String?
            get() = Settings.Manager.getString("renderer", "opengles2")

        val buttonscale: Int
            get() = Settings.Manager.getInt("buttonscale", 100)

        val ignoreNotch: Boolean
            get() = Settings.Manager.getBoolean("ignoreNotch", true)

        val timeLongPressTrigger: Int
            get() = Settings.Manager.getInt("timeLongPressTrigger", 300)

        val defaultCtrl: String?
            get() = Settings.Manager.getString("defaultCtrl", PathAndUrlManager.FILE_CTRLDEF_FILE)

        val forceEnglish: Boolean
            get() = Settings.Manager.getBoolean("force_english", false)

        val checkLibraries: Boolean
            get() = Settings.Manager.getBoolean("checkLibraries", true)

        val disableGestures: Boolean
            get() = Settings.Manager.getBoolean("disableGestures", false)

        val disableDoubleTap: Boolean
            get() = Settings.Manager.getBoolean("disableDoubleTap", false)

        val ramAllocation: Int
            get() = Settings.Manager.getInt("allocation", LauncherPreferences.findBestRAMAllocation(PojavApplication.getContext()))

        val javaArgs: String?
            get() = Settings.Manager.getString("javaArgs", "")

        val defaultRuntime: String?
            get() = Settings.Manager.getString("defaultRuntime", "")

        val sustainedPerformance: Boolean
            get() = Settings.Manager.getBoolean("sustainedPerformance", false)

        val virtualMouseStart: Boolean
            get() = Settings.Manager.getBoolean("mouse_start", true)

        val arcCapes: Boolean
            get() = Settings.Manager.getBoolean("arc_capes", false)

        val alternateSurface: Boolean
            get() = Settings.Manager.getBoolean("alternate_surface", false)

        val javaSandbox: Boolean
            get() = Settings.Manager.getBoolean("java_sandbox", true)

        val resolutionRatio: Int
            get() = Settings.Manager.getInt("resolutionRatio", 100)

        val enableGyro: Boolean
            get() = Settings.Manager.getBoolean("enableGyro", false)

        val gyroSensitivity: Float
            get() = Settings.Manager.getInt("gyroSensitivity", 100) / 100f

        val gyroSampleRate: Int
            get() = Settings.Manager.getInt("gyroSampleRate", 16)

        val gyroSmoothing: Boolean
            get() = Settings.Manager.getBoolean("gyroSmoothing", true)

        val gyroInvertX: Boolean
            get() = Settings.Manager.getBoolean("gyroInvertX", false)

        val gyroInvertY: Boolean
            get() = Settings.Manager.getBoolean("gyroInvertY", false)

        val forceVsync: Boolean
            get() = Settings.Manager.getBoolean("force_vsync", false)

        val buttonAllCaps: Boolean
            get() = Settings.Manager.getBoolean("buttonAllCaps", true)

        val mouseScale: Int
            get() = Settings.Manager.getInt("mousescale", 100)

        val mouseSpeed: Int
            get() = Settings.Manager.getInt("mousespeed", 100)

        val customMouse: String?
            get() = Settings.Manager.getString("custom_mouse", null)

        val dumpShaders: Boolean
            get() = Settings.Manager.getBoolean("dump_shaders", false)

        val deadzoneScale: Float
            get() = Settings.Manager.getInt("gamepad_deadzone_scale", 100) / 100f

        val bigCoreAffinity: Boolean
            get() = Settings.Manager.getBoolean("bigCoreAffinity", false)

        val zinkPreferSystemDriver: Boolean
            get() = Settings.Manager.getBoolean("zinkPreferSystemDriver", false)

        val downloadSource: String?
            get() = Settings.Manager.getString("downloadSource", "default")

        val verifyManifest: Boolean
            get() = Settings.Manager.getBoolean("verifyManifest", true)

        val skipNotificationPermissionCheck: Boolean
            get() = Settings.Manager.getBoolean("skipNotificationPermissionCheck", false)

        val vsyncInZink: Boolean
            get() = Settings.Manager.getBoolean("vsync_in_zink", false)

        val autoSetGameLanguage: Boolean
            get() = Settings.Manager.getBoolean("autoSetGameLanguage", true)

        val gameLanguageOverridden: Boolean
            get() = Settings.Manager.getBoolean("gameLanguageOverridden", false)

        val setGameLanguage: String?
            get() = Settings.Manager.getString("setGameLanguage", "system")

        val localAccountReminders: Boolean
            get() = Settings.Manager.getBoolean("localAccountReminders", true)

        val launcherTheme: String?
            get() = Settings.Manager.getString("launcherTheme", "system")

        val enableLogOutput: Boolean
            get() = Settings.Manager.getBoolean("enableLogOutput", false)

        val ignoreUpdate: String?
            get() = Settings.Manager.getString("ignoreUpdate", null)

        val noticeNumbering: Int
            get() = Settings.Manager.getInt("noticeNumbering", 0)

        val noticeDefault: Boolean
            get() = Settings.Manager.getBoolean("noticeDefault", false)

        val animation: Boolean
            get() = Settings.Manager.getBoolean("animation", true)

        val animationSpeed: Int
            get() = Settings.Manager.getInt("animationSpeed", 600)

        val pageOpacity: Int
            get() = Settings.Manager.getInt("pageOpacity", 100)

        val quitLauncher: Boolean
            get() = Settings.Manager.getBoolean("quitLauncher", true)

        val buttonSnapping: Boolean
            get() = Settings.Manager.getBoolean("buttonSnapping", true)

        val buttonSnappingDistance: Int
            get() = Settings.Manager.getInt("buttonSnappingDistance", 8)

        val modInfoSource: String?
            get() = Settings.Manager.getString("modInfoSource", "original")

        val modDownloadSource: String?
            get() = Settings.Manager.getString("modDownloadSource", "original")

    }
}