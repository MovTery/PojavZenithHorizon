package net.kdt.pojavlaunch.prefs;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.SharedPreferences;

import net.kdt.pojavlaunch.PojavZHTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Set;

public class SLPreferences {
    public static Set<String> getPrefs(){
        return DEFAULT_PREF.getAll().keySet();
    }

    public static synchronized void save(String name) throws Exception {
        //创建一个新的属性文件用于存储所有的偏好设置
        File prefsFile = new File(PojavZHTools.DIR_PREFS, "/" + name + ".prefs");

        Properties properties = new Properties();
        for (String pref : getPrefs()) {
            Object o = DEFAULT_PREF.getAll().get(pref);
            properties.setProperty(checkType(o) + pref, String.valueOf(o));
        }

        properties.store(new FileWriter(prefsFile), "PojavZH Prefs");
    }

    private static String checkType(Object value) {
        if (value instanceof String) return "Str_";
        if (value instanceof Integer) return "Int_";
        if (value instanceof Long) return "Long_";
        if (value instanceof Float) return "Float_";
        if (value instanceof Boolean) return "Bool_";
        return "null_";
    }

    public static synchronized void load(File prefsFile) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader(prefsFile));

        //更新SharedPreferences
        SharedPreferences.Editor editor = DEFAULT_PREF.edit();
        for (String pref : properties.stringPropertyNames()) {
            String value = properties.getProperty(pref);
            if (value.startsWith("Str_")) {
                editor.putString(pref, value);
            } else if (value.startsWith("Int_")) {
                editor.putInt(pref, Integer.parseInt(value));
            } else if (value.startsWith("Long_")) {
                editor.putLong(pref, Long.parseLong(value));
            } else if (value.startsWith("Float_")) {
                editor.putFloat(pref, Float.parseFloat(value));
            } else if (value.startsWith("Bool_")) {
                editor.putBoolean(pref, Boolean.parseBoolean(value));
            }
        }
        editor.apply();
    }
}
