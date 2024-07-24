package com.movtery.pojavzh.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ProfilePathManager {
    private static final String defaultPath = Tools.DIR_GAME_HOME;

    public static void setCurrentPathId(String id) {
        DEFAULT_PREF.edit().putString("launcherProfile", id).apply();
    }

    public static String getCurrentPath() {
        //通过选中的id来获取当前路径
        String id = DEFAULT_PREF.getString("launcherProfile", "default");
        if (id.equals("default")) {
            return defaultPath;
        }

        if (ZHTools.FILE_PROFILE_PATH.exists()) {
            try {
                String read = Tools.read(ZHTools.FILE_PROFILE_PATH);
                JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();
                if (jsonObject.has(id)) {
                    ProfilePathJsonObject profilePathJsonObject = new Gson().fromJson(jsonObject.get(id), ProfilePathJsonObject.class);
                    return profilePathJsonObject.path;
                }
            } catch (IOException e) {
                Log.e("Read Profile", e.toString());
            }
        }
        return defaultPath;
    }

    public static File getCurrentProfile() {
        File file = new File(ProfilePathHome.getGameHome(), "launcher_profiles.json");
        if (!file.exists()) {
            try {
                Tools.copyAssetFile(PojavApplication.getContext(), "launcher_profiles.json", ProfilePathHome.getGameHome(), false);
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

        try (FileWriter fileWriter = new FileWriter(ZHTools.FILE_PROFILE_PATH)) {
            new Gson().toJson(jsonObject, fileWriter);
        } catch (IOException e) {
            Log.e("Write Profile", e.toString());
        }
    }
}
