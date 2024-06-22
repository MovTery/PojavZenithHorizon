package com.movtery.pojavzh.utils.platform;

import android.app.ActivityManager;
import android.content.Context;

/**This class is modified from Fold Craft Launcher.*/
public class MemoryUtils {
    public static long getTotalDeviceMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return memInfo.totalMem;
    }

    public static long getUsedDeviceMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return memInfo.totalMem - memInfo.availMem;
    }

    public static long getFreeDeviceMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return memInfo.availMem;
    }
}