package com.movtery.background;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class BackgroundManager {
    public static File FILE_BACKGROUND_PROPERTIES = new File(Tools.DIR_GAME_HOME, "background.properties");

    public static Properties getProperties() {
        if (!FILE_BACKGROUND_PROPERTIES.exists()) {
            return getDefaultProperties();
        }
        Properties properties = new Properties();
        try (
                InputStream is = new FileInputStream(FILE_BACKGROUND_PROPERTIES)
        ) {
            properties.load(is);
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
        if (!PojavZHTools.DIR_BACKGROUND.exists()) PojavZHTools.DIR_BACKGROUND.mkdirs();

        try {
            properties.store(new FileWriter(FILE_BACKGROUND_PROPERTIES), "Pojav ZH Background Properties File");
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
