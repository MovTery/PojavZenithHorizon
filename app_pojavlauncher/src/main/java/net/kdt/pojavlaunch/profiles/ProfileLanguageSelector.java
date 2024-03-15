package net.kdt.pojavlaunch.profiles;

import static net.kdt.pojavlaunch.Tools.getGameDirPath;

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

    public static String getOlderLanguage(String lang) {
        StringBuilder builder = new StringBuilder(lang);
        int underscoreIndex = lang.indexOf('_');

        if (underscoreIndex != -1) {
            for (int i = underscoreIndex; i < lang.length(); i++) {
                builder.setCharAt(i, Character.toUpperCase(lang.charAt(i)));
            } //只将下划线后面的字符转换为大写
        }

        return builder.toString();
    }

    public static int getVersion(String versionId) {
        int firstDotIndex = versionId.indexOf('.');
        int secondDotIndex = versionId.indexOf('.', firstDotIndex + 1);
        int version;

        if (firstDotIndex != -1) { // 官方版本
            if (secondDotIndex == -1) version = Integer.parseInt(versionId.substring(firstDotIndex + 1));
                else version = Integer.parseInt(versionId.substring(firstDotIndex + 1, secondDotIndex));
        } else version = 12;
        return version;
    }

    public static String getDigitsBeforeFirstLetter(String input) {
        // 正则表达式匹配数字字符
        Pattern pattern = Pattern.compile("^\\d*");
        Matcher matcher = pattern.matcher(input);

        return matcher.find() ? matcher.group() : "";
    }

    public static String getDigitsBetweenFirstAndSecondLetter(String input) {
        Pattern pattern = Pattern.compile("([a-zA-Z])(\\d*)([a-zA-Z])");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    public static boolean containsLetter(String input) {
        return input.matches(".*[a-zA-Z].*");
    }
    public static boolean containsDot(String input) {
        int dotIndex = input.indexOf('.');
        return dotIndex != -1;
    }

    public static String getLanguage(String versionId, String lang) {
        int version = 12;

        String optifineSuffix = "OptiFine"; // "1.20.4-OptiFine_HD_U_I7_pre3"
        String forgeSuffix = "forge"; // "1.20.2-forge-48.1.0"
        String fabricSuffix = "fabric-loader"; // "fabric-loader-0.15.7-1.20.4"
        String quiltSuffix = "quilt-loader"; // "quilt-loader-0.23.1-1.20.4"
        String regex = "^\\d+[a-zA-Z]\\d+[a-zA-Z]$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(versionId);

        if (containsLetter(versionId)) {
            if (versionId.contains(optifineSuffix) || versionId.contains(forgeSuffix)) { // OptiFine & Forge
                int lastIndex = versionId.indexOf('-');
                if (lastIndex != -1) {
                    version = getVersion(versionId.substring(0, lastIndex));
                }
            } else if (versionId.contains(fabricSuffix) || versionId.contains(quiltSuffix)) { // Fabric & Quilt
                int lastIndex = versionId.lastIndexOf('-');

                if (lastIndex != -1) {
                    version = getVersion(versionId.substring(lastIndex + 1));
                }
            } else if (matcher.matches()) { // 快照版本 "24w09a" "16w20a"
                int result1 = Integer.parseInt(getDigitsBeforeFirstLetter(versionId));
                int result2 = Integer.parseInt(getDigitsBetweenFirstAndSecondLetter(versionId));

                if(result1 < 16) {
                    return getOlderLanguage(lang);
                } else if (result1 == 16 & result2 <= 32) {
                    return getOlderLanguage(lang);
                }

                return lang;
            }
        } else if(containsDot(versionId)) {
            version = getVersion(versionId);
        }

        // 1.10 -
        if (version < 11) {
            return getOlderLanguage(lang);
        }

        return lang; // ? & 1.0
    }

    public static void setToChinese(MinecraftProfile minecraftProfile) {
        File optionFile = new File((getGameDirPath(minecraftProfile.gameDir)), "options.txt");
        ArrayList<String> options = new ArrayList<>();
        boolean foundMatch = false;
        String language = getLanguage(minecraftProfile.lastVersionId, LauncherPreferences.PREF_SWITCH_TO_CHINESE_LANGUAGE);

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
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }
}
