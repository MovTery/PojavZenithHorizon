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

            return if (AllSettings.forceEnglish) {
                val resources = context.resources
                val configuration = resources.configuration

                val locale = Locale.ENGLISH
                configuration.setLocale(locale)
                Locale.setDefault(locale)

                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)

                LocaleHelper(context.createConfigurationContext(configuration))
            } else {
                LocaleHelper(context)
            }
        }
    }
}