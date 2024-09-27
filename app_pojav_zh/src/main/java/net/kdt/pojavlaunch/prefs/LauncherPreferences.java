package net.kdt.pojavlaunch.prefs;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;

import static net.kdt.pojavlaunch.Architecture.is32BitsDevice;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;

import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.unpack.Jre;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.setting.LegacySettingsSync;
import com.movtery.pojavzh.setting.Settings;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.*;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.utils.JREUtils;

public class LauncherPreferences {
	public static int PREF_NOTCH_SIZE = 0;
    public static final String PREF_VERSION_REPOS = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static void loadPreferences(Context ctx) {
        //Required for the data folder.
        PathAndUrlManager.initContextConstants(ctx);

        LegacySettingsSync.check(ctx);

        String argLwjglLibname = "-Dorg.lwjgl.opengl.libname=";
        String javaArgs = AllSettings.Companion.getJavaArgs();
        if (javaArgs != null) {
            for (String arg : JREUtils.parseJavaArguments(javaArgs)) {
                if (arg.startsWith(argLwjglLibname)) {
                    // purge arg
                    Settings.Manager.Companion.put("javaArgs", javaArgs.replace(arg, "")).save();
                }
            }
        }

        reloadRuntime();
    }

    public static void reloadRuntime() {
        if (!Settings.Manager.Companion.contains("defaultRuntime") && !MultiRTUtils.getRuntimes().isEmpty()) {
            //设置默认运行环境
            Settings.Manager.Companion.put("defaultRuntime", Jre.JRE_8.getJreName()).save();
        }
    }

    /**
     * This functions aims at finding the best default RAM amount,
     * according to the RAM amount of the physical device.
     * Put not enough RAM ? Minecraft will lag and crash.
     * Put too much RAM ?
     * The GC will lag, android won't be able to breathe properly.
     * @param ctx Context needed to get the total memory of the device.
     * @return The best default value found.
     */
    public static int findBestRAMAllocation(Context ctx){
        int deviceRam = Tools.getTotalDeviceMemory(ctx);
        if (deviceRam < 1024) return 300;
        if (deviceRam < 1536) return 450;
        if (deviceRam < 2048) return 600;
        // Limit the max for 32 bits devices more harshly
        if (is32BitsDevice()) return 700;

        if (deviceRam < 3064) return 936;
        if (deviceRam < 4096) return 1148;
        if (deviceRam < 6144) return 1536;
        return 2048; //Default RAM allocation for 64 bits
    }

    /** Compute the notch size to avoid being out of bounds */
    public static void computeNotchSize(Activity activity) {
        if (Build.VERSION.SDK_INT < P) return;
        try {
            final Rect cutout;
            if(SDK_INT >= Build.VERSION_CODES.S){
                cutout = activity.getWindowManager().getCurrentWindowMetrics().getWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            } else {
                cutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            }

            // Notch values are rotation sensitive, handle all cases
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) LauncherPreferences.PREF_NOTCH_SIZE = cutout.height();
            else if (orientation == Configuration.ORIENTATION_LANDSCAPE) LauncherPreferences.PREF_NOTCH_SIZE = cutout.width();
            else LauncherPreferences.PREF_NOTCH_SIZE = Math.min(cutout.width(), cutout.height());

        }catch (Exception e){
            Logging.i("NOTCH DETECTION", "No notch detected, or the device if in split screen mode");
            LauncherPreferences.PREF_NOTCH_SIZE = -1;
        }
        Tools.updateWindowSize(activity);
    }
}
