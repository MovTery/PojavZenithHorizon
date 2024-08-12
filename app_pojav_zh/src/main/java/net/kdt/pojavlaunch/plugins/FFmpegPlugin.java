package net.kdt.pojavlaunch.plugins;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.movtery.pojavzh.feature.log.Logging;

public class FFmpegPlugin {
    public static boolean isAvailable = false;
    public static String libraryPath;
    public static void discover(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo ffmpegPluginInfo = manager.getPackageInfo("net.kdt.pojavlaunch.ffmpeg", PackageManager.GET_SHARED_LIBRARY_FILES);
            libraryPath = ffmpegPluginInfo.applicationInfo.nativeLibraryDir;
            isAvailable = true;
        }catch (Exception e) {
            Logging.i("FFmpegPlugin", "Failed to discover plugin", e);
        }
    }
}
