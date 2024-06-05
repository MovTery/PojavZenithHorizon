package com.movtery.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movtery.feature.ResourceManager;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilePathManager {
    private static final String defaultPath = Tools.DIR_GAME_HOME;
    public static final Map<String, ProfileItem> mData = new HashMap<>();

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

    public static List<ProfileItem> loadDataFromFile() {
        List<ProfileItem> data = new ArrayList<>();
        mData.clear();

        try {
            String json;
            if (PojavZHTools.FILE_PROFILE_PATH.exists()) {
                json = Tools.read(PojavZHTools.FILE_PROFILE_PATH);
                if (json.isEmpty()) {
                    saveDefault();
                    return data;
                }
            } else {
                saveDefault();
                return data;
            }

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                ProfilePathJsonObject profilePathId = new Gson().fromJson(jsonObject.get(key), ProfilePathJsonObject.class);
                ProfileItem item = new ProfileItem(key, profilePathId.title, profilePathId.path);
                mData.put(key, item);
                data.add(item);
            }

            return data;
        } catch (Exception ignored) {
            return data;
        }
    }

    public static void save(List<ProfileItem> items) {
        items.remove(0);

        JsonObject jsonObject = new JsonObject();

        for (ProfileItem item : items) {
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

    public static void saveDefault() {
        save(new ArrayList<>());
    }

    public static void delete(String id) {
        if (id != null && !id.isEmpty()) {
            mData.remove(id);

            List<ProfileItem> items = new ArrayList<>();
            mData.forEach((k, v) -> items.add(v));

            save(items);
        }
    }
}
