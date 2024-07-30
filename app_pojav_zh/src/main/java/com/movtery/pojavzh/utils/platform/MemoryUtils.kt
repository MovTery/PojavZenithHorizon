package com.movtery.pojavzh.utils.platform;

import android.app.ActivityManager;
import android.content.Context;

/**This class is modified from Fold Craft Launcher.*/
public class MemoryUtils {
    private static ActivityManager activityManager;

    private static void init(Context context) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public static long getTotalDeviceMemory(Context context) {
        if (activityManager == null) init(context);

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem;
    }

    public static long getUsedDeviceMemory(Context context) {
        if (activityManager == null) init(context);

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem - memInfo.availMem;
    }

    public static long getFreeDeviceMemory(Context context) {
        if (activityManager == null) init(context);

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.availMem;
    }
}