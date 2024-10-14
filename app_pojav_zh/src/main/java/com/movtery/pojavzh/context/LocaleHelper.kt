package com.movtery.pojavzh.context

import android.content.Context
import android.content.ContextWrapper
import android.os.LocaleList
import com.movtery.pojavzh.setting.AllSettings
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.util.Locale

class LocaleHelper(context: Context) : ContextWrapper(context) {
    companion object {
        fun setLocale(context: Context): ContextWrapper {
            LauncherPreferences.loadPreferences(context)
            return LocaleHelper(context)
        }
    }
}