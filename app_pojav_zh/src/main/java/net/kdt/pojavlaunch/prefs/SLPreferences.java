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
        for (String propertyName : properties.stringPropertyNames()) {
            String value = properties.getProperty(propertyName);
            if (propertyName.startsWith("Str_")) {
                editor.putString(propertyName.substring("Str_".length()), value);
            } else if (propertyName.startsWith("Int_")) {
                editor.putInt(propertyName.substring("Int_".length()), Integer.parseInt(value));
            } else if (propertyName.startsWith("Long_")) {
                editor.putLong(propertyName.substring("Long_".length()), Long.parseLong(value));
            } else if (propertyName.startsWith("Float_")) {
                editor.putFloat(propertyName.substring("Float_".length()), Float.parseFloat(value));
            } else if (propertyName.startsWith("Bool_")) {
                editor.putBoolean(propertyName.substring("Bool_".length()), Boolean.parseBoolean(value));
            }
        }
        editor.apply();
    }
}
