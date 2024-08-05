package com.movtery.pojavzh.utils.platform

import android.app.ActivityManager
import android.content.Context

object MemoryUtils {
    private var activityManager: ActivityManager? = null

    private fun init(context: Context) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    @JvmStatic
    fun getTotalDeviceMemory(context: Context): Long {
        if (activityManager == null) init(context)

        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        return memInfo.totalMem
    }

    @JvmStatic
    fun getUsedDeviceMemory(context: Context): Long {
        if (activityManager == null) init(context)

        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        return memInfo.totalMem - memInfo.availMem
    }

    @JvmStatic
    fun getFreeDeviceMemory(context: Context): Long {
        if (activityManager == null) init(context)

        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        return memInfo.availMem
    }
}