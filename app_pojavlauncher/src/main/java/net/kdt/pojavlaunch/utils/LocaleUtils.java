package net.kdt.pojavlaunch.utils;


import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.*;
import android.content.res.*;
import android.os.Build;
import android.os.LocaleList;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.preference.*;
import java.util.*;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.*;

public class LocaleUtils extends ContextWrapper {

    public LocaleUtils(Context base) {
        super(base);
    }

    public static ContextWrapper setLocale(Context context) {
        int animationRate = DEFAULT_PREF.getInt("animationRate", 300);
        if (DEFAULT_PREF == null) {
            DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(context);
            LauncherPreferences.loadPreferences(context);
        }

        if(DEFAULT_PREF.getBoolean("force_english", false)){
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();

            configuration.setLocale(Locale.ENGLISH);
            Locale.setDefault(Locale.ENGLISH);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                LocaleList localeList = new LocaleList(Locale.ENGLISH);
                LocaleList.setDefault(localeList);
                configuration.setLocales(localeList);
            }

            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1){
                context = context.createConfigurationContext(configuration);
            }
        }

        if (animationRate != 300) {
            Animation cutInto = AnimationUtils.loadAnimation(context, net.kdt.pojavlaunch.R.anim.cut_into);
            Animation cutOut = AnimationUtils.loadAnimation(context, R.anim.cut_out);

            cutInto.setDuration(animationRate);
            cutOut.setDuration(animationRate);
        }

        return new LocaleUtils(context);
    }
}
