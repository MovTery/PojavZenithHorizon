package com.movtery.zalithlauncher.setting

import com.movtery.zalithlauncher.utils.PathAndUrlManager
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class AllSettings {
    companion object {
        // Video
        val renderer: String?
            get() = Settings.Manager.getString("renderer", "opengles2")

        val ignoreNotch: Boolean
            get() = Settings.Manager.getBoolean("ignoreNotch", true)

        val resolutionRatio: Int
            get() = Settings.Manager.getInt("resolutionRatio", 100)

        val sustainedPerformance: Boolean
            get() = Settings.Manager.getBoolean("sustainedPerformance", false)

        val alternateSurface: Boolean
            get() = Settings.Manager.getBoolean("alternate_surface", false)

        val forceVsync: Boolean
            get() = Settings.Manager.getBoolean("force_vsync", false)

        val vsyncInZink: Boolean
            get() = Settings.Manager.getBoolean("vsync_in_zink", false)

        val zinkPreferSystemDriver: Boolean
            get() = Settings.Manager.getBoolean("zinkPreferSystemDriver", false)

        // Control
        val disableGestures: Boolean
            get() = Settings.Manager.getBoolean("disableGestures", false)

        val disableDoubleTap: Boolean
            get() = Settings.Manager.getBoolean("disableDoubleTap", false)

        val timeLongPressTrigger: Int
            get() = Settings.Manager.getInt("timeLongPressTrigger", 300)

        val buttonscale: Int
            get() = Settings.Manager.getInt("buttonscale", 100)

        val buttonAllCaps: Boolean
            get() = Settings.Manager.getBoolean("buttonAllCaps", true)

        val mouseScale: Int
            get() = Settings.Manager.getInt("mousescale", 100)

        val mouseSpeed: Int
            get() = Settings.Manager.getInt("mousespeed", 100)

        val virtualMouseStart: Boolean
            get() = Settings.Manager.getBoolean("mouse_start", true)

        val customMouse: String?
            get() = Settings.Manager.getString("custom_mouse", null)

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

        val deadzoneScale: Float
            get() = Settings.Manager.getInt("gamepad_deadzone_scale", 100) / 100f

        // Game
        val autoSetGameLanguage: Boolean
            get() = Settings.Manager.getBoolean("autoSetGameLanguage", true)

        val gameLanguageOverridden: Boolean
            get() = Settings.Manager.getBoolean("gameLanguageOverridden", false)

        val setGameLanguage: String?
            get() = Settings.Manager.getString("setGameLanguage", "system")

        val javaArgs: String?
            get() = Settings.Manager.getString("javaArgs", "")

        val ramAllocation: Int
            get() = Settings.Manager.getInt("allocation", LauncherPreferences.findBestRAMAllocation(PojavApplication.getContext()))

        val javaSandbox: Boolean
            get() = Settings.Manager.getBoolean("java_sandbox", true)

        val gameMenuShowMemory: Boolean
            get() = Settings.Manager.getBoolean("gameMenuShowMemory", false)

        val gameMenuMemoryText: String?
            get() = Settings.Manager.getString("gameMenuMemoryText", "M:")

        val gameMenuLocation: String?
            get() = Settings.Manager.getString("gameMenuLocation", "center")

        val gameMenuAlpha: Int
            get() = Settings.Manager.getInt("gameMenuAlpha", 100)

        // Launcher
        val checkLibraries: Boolean
            get() = Settings.Manager.getBoolean("checkLibraries", true)

        val verifyManifest: Boolean
            get() = Settings.Manager.getBoolean("verifyManifest", true)

        val resourceImageCache: Boolean
            get() = Settings.Manager.getBoolean("resourceImageCache", false)

        val downloadSource: String?
            get() = Settings.Manager.getString("downloadSource", "default")

        val modInfoSource: String?
            get() = Settings.Manager.getString("modInfoSource", "original")

        val modDownloadSource: String?
            get() = Settings.Manager.getString("modDownloadSource", "original")

        val launcherTheme: String?
            get() = Settings.Manager.getString("launcherTheme", "system")

        val animation: Boolean
            get() = Settings.Manager.getBoolean("animation", true)

        val animationSpeed: Int
            get() = Settings.Manager.getInt("animationSpeed", 600)

        val pageOpacity: Int
            get() = Settings.Manager.getInt("pageOpacity", 100)

        val enableLogOutput: Boolean
            get() = Settings.Manager.getBoolean("enableLogOutput", false)

        val quitLauncher: Boolean
            get() = Settings.Manager.getBoolean("quitLauncher", true)

        // Experimental
        val dumpShaders: Boolean
            get() = Settings.Manager.getBoolean("dump_shaders", false)

        val bigCoreAffinity: Boolean
            get() = Settings.Manager.getBoolean("bigCoreAffinity", false)

        // Other
        val currentProfile: String?
            get() = Settings.Manager.getString("currentProfile", "")

        val launcherProfile: String?
            get() = Settings.Manager.getString("launcherProfile", "default")

        val defaultCtrl: String?
            get() = Settings.Manager.getString("defaultCtrl", PathAndUrlManager.FILE_CTRLDEF_FILE)

        val defaultRuntime: String?
            get() = Settings.Manager.getString("defaultRuntime", "")

        val skipNotificationPermissionCheck: Boolean
            get() = Settings.Manager.getBoolean("skipNotificationPermissionCheck", false)

        val localAccountReminders: Boolean
            get() = Settings.Manager.getBoolean("localAccountReminders", true)

        val ignoreUpdate: String?
            get() = Settings.Manager.getString("ignoreUpdate", null)

        val noticeNumbering: Int
            get() = Settings.Manager.getInt("noticeNumbering", 0)

        val noticeDefault: Boolean
            get() = Settings.Manager.getBoolean("noticeDefault", false)

        val buttonSnapping: Boolean
            get() = Settings.Manager.getBoolean("buttonSnapping", true)

        val buttonSnappingDistance: Int
            get() = Settings.Manager.getInt("buttonSnappingDistance", 8)
    }
}