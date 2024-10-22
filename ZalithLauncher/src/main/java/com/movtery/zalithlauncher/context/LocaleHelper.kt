package com.movtery.zalithlauncher.context

import android.content.Context
import android.content.ContextWrapper
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class LocaleHelper(context: Context) : ContextWrapper(context) {
    companion object {
        fun setLocale(context: Context): ContextWrapper {
            LauncherPreferences.loadPreferences(context)
            return LocaleHelper(context)
        }
    }
}