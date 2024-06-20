package com.movtery.pojavzh.ui.subassembly.background;

import android.graphics.drawable.Drawable;

import com.movtery.pojavzh.utils.ZHTools;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class BackgroundManager {
    private static final Map<String, Drawable> backgroundDrawable = new ConcurrentHashMap<>();
    public static File FILE_BACKGROUND_PROPERTIES = new File(Tools.DIR_GAME_HOME, "background.properties");

    public static Drawable getBackgroundDrawable(String name, File imageFile) {
        boolean hasDrawable = backgroundDrawable.containsKey(name);
        if (hasDrawable) {
            return backgroundDrawable.get(name);
        } else {
            try {
                Drawable drawable = Drawable.createFromPath(imageFile.getAbsolutePath());
                backgroundDrawable.put(name, drawable);
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static Properties getProperties() {
        if (!FILE_BACKGROUND_PROPERTIES.exists()) {
            return getDefaultProperties();
        }
        Properties properties = new Properties();
        try (FileReader fileReader = new FileReader(FILE_BACKGROUND_PROPERTIES)) {
            properties.load(fileReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    private static Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty(BackgroundType.MAIN_MENU.name(), "null");
        properties.setProperty(BackgroundType.SETTINGS.name(), "null");
        properties.setProperty(BackgroundType.CUSTOM_CONTROLS.name(), "null");
        properties.setProperty(BackgroundType.IN_GAME.name(), "null");

        saveProperties(properties);
        return properties;
    }
    
    private static void saveProperties(Properties properties) {
        if (!ZHTools.DIR_BACKGROUND.exists()) ZHTools.mkdirs(ZHTools.DIR_BACKGROUND);

        try {
            properties.store(new FileWriter(FILE_BACKGROUND_PROPERTIES), "Pojav Zenith Horizon Background Properties File");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveProperties(Map<BackgroundType, String> map) {
        Properties properties = new Properties();
        properties.setProperty(BackgroundType.MAIN_MENU.name(),
                (map.get(BackgroundType.MAIN_MENU) == null ? "null" : map.get(BackgroundType.MAIN_MENU)));
        properties.setProperty(BackgroundType.SETTINGS.name(),
                (map.get(BackgroundType.SETTINGS) == null ? "null" : map.get(BackgroundType.SETTINGS)));
        properties.setProperty(BackgroundType.CUSTOM_CONTROLS.name(),
                (map.get(BackgroundType.CUSTOM_CONTROLS) == null ? "null" : map.get(BackgroundType.CUSTOM_CONTROLS)));
        properties.setProperty(BackgroundType.IN_GAME.name(),
                (map.get(BackgroundType.IN_GAME) == null ? "null" : map.get(BackgroundType.IN_GAME)));

        saveProperties(properties);
    }
}
