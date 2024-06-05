package com.movtery.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.movtery.feature.ResourceManager;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ProfilePathManager {
    private static final String defaultPath = Tools.DIR_GAME_HOME;

    public static void setCurrentPath(String path) {
        DEFAULT_PREF.edit().putString("launcherProfilePath", path).apply();
    }

    public static String getCurrentPath() {
        return DEFAULT_PREF.getString("launcherProfilePath", defaultPath);
    }

    public static File getCurrentProfile() {
        File file = new File(ProfilePathHome.getGameHome(), "launcher_profiles.json");
        if (!file.exists()) {
            try {
                Tools.copyAssetFile(ResourceManager.getContext(), "launcher_profiles.json", ProfilePathHome.getGameHome(), false);
            } catch (IOException e) {
                return new File(defaultPath, "launcher_profiles.json");
            }
        }
        return file;
    }

    public static void save(List<ProfileItem> items) {
        JsonObject jsonObject = new JsonObject();

        for (ProfileItem item : items) {
            if (item.id.equals("default")) continue;

            ProfilePathJsonObject profilePathJsonObject = new ProfilePathJsonObject();
            profilePathJsonObject.title = item.title;
            profilePathJsonObject.path = item.path;
            jsonObject.add(item.id, new Gson().toJsonTree(profilePathJsonObject));
        }

        try (FileWriter fileWriter = new FileWriter(PojavZHTools.FILE_PROFILE_PATH)) {
            new Gson().toJson(jsonObject, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
