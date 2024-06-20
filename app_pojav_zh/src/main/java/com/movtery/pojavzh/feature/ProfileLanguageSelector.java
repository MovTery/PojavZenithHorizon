package com.movtery.pojavzh.feature;

import static com.movtery.pojavzh.utils.ZHTools.containsDot;
import static com.movtery.pojavzh.utils.ZHTools.extractNumbers;
import static com.movtery.pojavzh.utils.ZHTools.getGameDirPath;
import static net.kdt.pojavlaunch.Tools.read;

import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Logger;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileLanguageSelector {
    private ProfileLanguageSelector() {
    }

    private static String getOlderLanguage(String lang) {
        StringBuilder builder = new StringBuilder(lang);
        int underscoreIndex = lang.indexOf('_');

        if (underscoreIndex != -1) {
            for (int i = underscoreIndex; i < lang.length(); i++) {
                builder.setCharAt(i, Character.toUpperCase(lang.charAt(i)));
            } //只将下划线后面的字符转换为大写
        }

        return builder.toString();
    }

    private static int getVersion(String versionId) throws NumberFormatException {
        int firstDotIndex = versionId.indexOf('.');
        int secondDotIndex = versionId.indexOf('.', firstDotIndex + 1);
        int version;

        if (firstDotIndex != -1) { // 官方版本
            if (secondDotIndex == -1) version = Integer.parseInt(versionId.substring(firstDotIndex + 1));
                else version = Integer.parseInt(versionId.substring(firstDotIndex + 1, secondDotIndex));
        } else version = 12;
        return version;
    }

    private static String getLanguage(String versionName, String rawLang) {
        if (versionName == null || rawLang == null) return null;
        String lang = rawLang;
        if (rawLang.equals("system")) lang = ZHTools.getSystemLanguage();

        JMinecraftVersionList.Version version;
        try {
            version = Tools.GLOBAL_GSON.fromJson(read(ProfilePathHome.getVersionsHome() + "/" + versionName + "/" + versionName + ".json"), JMinecraftVersionList.Version.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String versionId = version.id;

        String regex = "^\\d+[a-zA-Z]\\d+[a-zA-Z]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(versionId);

        if(containsDot(versionId)) {
            try {
                int ver = getVersion(versionId);

                // 1.10 -
                if (ver < 11) {
                    return getOlderLanguage(lang);
                }

                return lang; // ? & 1.0
            } catch (NumberFormatException e) {
                return lang;
            }
        } else if (matcher.matches()) { // 快照版本 "24w09a" "16w20a"
            try {
                int[] result = extractNumbers(versionId, 2);

                if(result[0] < 16) {
                    return getOlderLanguage(lang);
                } else if (result[0] == 16 & result[1] <= 32) {
                    return getOlderLanguage(lang);
                }

                return lang;
            } catch (NumberFormatException e) {
                return lang;
            }
        }

        return lang;
    }

    public static void setToChinese(MinecraftProfile minecraftProfile) {
        File optionFile = new File((getGameDirPath(minecraftProfile.gameDir)), "options.txt");
        ArrayList<String> options = new ArrayList<>();
        boolean foundMatch = false;
        String language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_GAME_LANGUAGE);

        try (BufferedReader optionFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(optionFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = optionFileReader.readLine()) != null) {
                //使用正则表达式匹配“lang: xxx”格式
                Pattern pattern = Pattern.compile("lang:(\\S+)");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    foundMatch = true;
                }

                options.add(line);
            }
        } catch (IOException e) {
            Logger.appendToLog(e.toString());
            return;
        }

        if (!foundMatch) {
            options.add("lang:" + language);
            try (BufferedWriter optionFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(optionFile), StandardCharsets.UTF_8))) {
                for (String option : options) {
                    optionFileWriter.write(option);
                    optionFileWriter.newLine();
                }
            } catch (IOException e) {
                Logger.appendToLog(e.toString());
            }
        }
    }
}
