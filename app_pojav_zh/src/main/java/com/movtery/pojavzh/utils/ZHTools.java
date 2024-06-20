package com.movtery.pojavzh.utils;

import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.Tools.getFileName;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.DocumentsContract;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.movtery.pojavzh.ui.subassembly.background.BackgroundManager;
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType;

import com.movtery.pojavzh.ui.dialog.EditTextDialog;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.feature.ResourceManager;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi;
import com.movtery.pojavzh.feature.mod.api.MCBBSApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathManager;

import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZHTools {
    public static String DIR_GAME_MODPACK = null;
    public static String DIR_GAME_DEFAULT;
    public static String DIR_CUSTOM_MOUSE;
    public static String DIR_LOGIN;
    public static File DIR_BACKGROUND;
    public static File DIR_APP_CACHE;
    public static File DIR_USER_ICON;
    public static File FILE_CUSTOM_MOUSE;
    public static File FILE_PROFILE_PATH;
    public static final String URL_GITHUB_RELEASE = "https://api.github.com/repos/HopiHopy/PojavZenithHorizon/releases/latest";
    public static final String URL_GITHUB_HOME = "https://api.github.com/repos/HopiHopy/PojavZenithHorizon/contents/";
    public static long LAST_UPDATE_CHECK_TIME = 0;

    private ZHTools() {
    }

    public static void initContextConstants(Context context) {
        ZHTools.FILE_PROFILE_PATH = new File(Tools.DIR_DATA, "/profile_path.json");
        ZHTools.DIR_GAME_DEFAULT = ProfilePathHome.getGameHome() + "/instance/default";
        ZHTools.DIR_CUSTOM_MOUSE = DIR_GAME_HOME + "/mouse";
        ZHTools.DIR_LOGIN = DIR_GAME_HOME + "/login";
        ZHTools.DIR_BACKGROUND = new File(DIR_GAME_HOME + "/background");
        ZHTools.DIR_APP_CACHE = context.getExternalCacheDir();
        ZHTools.DIR_USER_ICON = new File(Tools.DIR_CACHE, "/user_icon");

        if (!ZHTools.DIR_BACKGROUND.exists()) {
            mkdirs(ZHTools.DIR_BACKGROUND);
        }
    }

    public static boolean mkdir(File dir) {
        return dir.mkdir();
    }

    public static boolean mkdirs(File dir) {
        return dir.mkdirs();
    }

    public static void onBackPressed(FragmentActivity fragmentActivity) {
        fragmentActivity.getOnBackPressedDispatcher().onBackPressed();
    }

    public static boolean isEnglish() {
        LocaleList locales = ResourceManager.getResources().getConfiguration().getLocales();
        return locales.get(0).getLanguage().equals("en");
    }

    public static File copyFileInBackground(Context context, Uri fileUri, String rootPath) {
        String fileName = getFileName(context, fileUri);
        File outputFile = new File(rootPath, fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            Objects.requireNonNull(inputStream);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024 * 8]; //8kb
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return outputFile;
    }

    public static boolean containsDot(String input) {
        int dotIndex = input.indexOf('.');
        return dotIndex != -1;
    }

    /**
     * 在一段字符串中提取数字
     */
    public static int[] extractNumbers(String str, int quantity) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        int[] numbers = new int[quantity];

        int count = 0;
        while (matcher.find() && count < quantity) {
            numbers[count] = Integer.parseInt(matcher.group());
            count++;
        }

        return numbers;
    }

    public static List<Integer> extractNumbers(String str) {
        List<Integer> numbers = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(str);
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        return numbers;
    }

    public static void setTooltipText(View view, CharSequence tooltip) {
        TooltipCompat.setTooltipText(view, tooltip);
    }

    public synchronized static Drawable customMouse(Context context) {
        String customMouse = DEFAULT_PREF.getString("custom_mouse", null);
        if (customMouse == null) {
            FILE_CUSTOM_MOUSE = null;
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }
        FILE_CUSTOM_MOUSE = new File(DIR_CUSTOM_MOUSE, customMouse);

        // 鼠标：自定义鼠标图片
        if (FILE_CUSTOM_MOUSE.exists()) {
            return Drawable.createFromPath(FILE_CUSTOM_MOUSE.getAbsolutePath());
        } else {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }
    }

    public static void setBackgroundImage(Context context, BackgroundType backgroundType, View backgroundView) {
        backgroundView.setBackgroundColor(context.getResources().getColor(R.color.background_app, context.getTheme()));

        File backgroundImage = getBackgroundImage(backgroundType);
        if (backgroundImage == null) {
            return;
        }

        Drawable drawable = BackgroundManager.getBackgroundDrawable(backgroundImage.getName(), backgroundImage);
        if (drawable != null) {
            backgroundView.post(() -> backgroundView.setBackground(drawable));
        }
    }

    public static File getBackgroundImage(BackgroundType backgroundType) {
        Properties properties = BackgroundManager.getProperties();
        String pngName = (String) properties.get(backgroundType.name());
        if (pngName == null || pngName.equals("null")) return null;

        File backgroundImage = new File(ZHTools.DIR_BACKGROUND, pngName);
        if (!backgroundImage.exists() || !isImage(backgroundImage)) return null;
        return backgroundImage;
    }

    /***
     * 通过读取文件的头部信息来判断文件是否为图片
     * @param filePath 文件路径
     * @return 返回是否为图片
     */
    public static boolean isImage(File filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            byte[] header = new byte[4];
            if (input.read(header, 0, 4) != -1) {
                return (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) || //JPEG
                        (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) || //PNG
                        (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46) || //GIF
                        (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) || //BMP
                        ((header[0] == (byte) 0x49 && header[1] == (byte) 0x49 && header[2] == (byte) 0x2A && header[3] == (byte) 0x00) || //TIFF
                                (header[0] == (byte) 0x4D && header[1] == (byte) 0x4D && header[2] == (byte) 0x00 && header[3] == (byte) 0x2A)); //TIFF
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static void swapSettingsFragment(FragmentActivity fragmentActivity, Class<? extends Fragment> fragmentClass,
                                            @Nullable String fragmentTag, @Nullable Bundle bundle, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();

        if (PREF_ANIMATION)
            transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);
        if (addToBackStack) transaction.addToBackStack(fragmentClass.getName());
        transaction.setReorderingAllowed(true)
                .replace(R.id.zh_settings_fragment, fragmentClass, bundle, fragmentTag)
                .commit();
    }

    public static void addFragment(Fragment fragment, Class<? extends Fragment> fragmentClass,
                                    @Nullable String fragmentTag, @Nullable Bundle bundle) {
        FragmentTransaction transaction = fragment.requireActivity().getSupportFragmentManager().beginTransaction();

        if (PREF_ANIMATION)
            transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);
        transaction.setReorderingAllowed(true)
                .addToBackStack(fragmentClass.getName())
                .add(R.id.container_fragment, fragmentClass, bundle, fragmentTag)
                .hide(fragment)
                .commit();
    }

    public static void restartApp(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static File getGameDirPath(String gameDir) {
        if (gameDir != null) {
            if (gameDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
                return new File(gameDir.replace(Tools.LAUNCHERPROFILES_RTPREFIX, ProfilePathManager.getCurrentPath() + "/"));
            else
                return new File(ProfilePathManager.getCurrentPath(), gameDir);
        }
        return new File(DIR_GAME_DEFAULT);
    }

    public static File getLatestFile(File folder, int modifyTime) {
        if (!folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles((dir, name) -> !name.startsWith(".")); //排除隐藏文件
        if (files == null || files.length == 0) {
            return null;
        }

        List<File> fileList = Arrays.asList(files);
        fileList.sort(Comparator.comparingLong(File::lastModified).reversed());

        if (modifyTime > 0) {
            long difference = System.currentTimeMillis() - fileList.get(0).lastModified();
            long value = difference / (1000L * 60 * 60);

            if (value >= modifyTime) {
                return null;
            }
        }

        return fileList.get(0);
    }

    public static void shareFile(Context context, String fileName, String filePath) {
        Uri contentUri = DocumentsContract.buildDocumentUri(context.getString(R.string.storageProviderAuthorities), filePath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType("text/plain");

        Intent sendIntent = Intent.createChooser(shareIntent, fileName);
        context.startActivity(sendIntent);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void renameFileListener(Context context, Runnable runnable, File file, @NotNull String suffix) {
        String fileParent = file.getParent();
        String fileName = file.getName();

        EditTextDialog editTextDialog = new EditTextDialog(context, context.getString(R.string.zh_rename), null, getFileNameWithoutExtension(fileName, suffix), null);
        editTextDialog.setConfirm(v -> {
            String newName = editTextDialog.getEditBox().getText().toString().replace("/", "");

            if (Objects.equals(fileName, newName)) {
                editTextDialog.dismiss();
                return;
            }

            if (newName.isEmpty()) {
                editTextDialog.getEditBox().setError(context.getString(R.string.zh_file_rename_empty));
                return;
            }

            File newFile = new File(fileParent, newName + suffix);
            if (newFile.exists()) {
                editTextDialog.getEditBox().setError(context.getString(R.string.zh_file_rename_exitis));
                return;
            }

            boolean renamed = file.renameTo(newFile);
            if (renamed) {
                if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
            }

            editTextDialog.dismiss();
        });

        editTextDialog.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void renameFileListener(Context context, Runnable runnable, File file) {
        String fileParent = file.getParent();
        String fileName = file.getName();

        EditTextDialog editTextDialog = new EditTextDialog(context, context.getString(R.string.zh_rename), null, fileName, null);
        editTextDialog.setConfirm(v -> {
            String newName = editTextDialog.getEditBox().getText().toString().replace("/", "");

            if (Objects.equals(fileName, newName)) {
                editTextDialog.dismiss();
                return;
            }

            if (newName.isEmpty()) {
                editTextDialog.getEditBox().setError(context.getString(R.string.zh_file_rename_empty));
                return;
            }

            File newFile = new File(fileParent, newName);
            if (newFile.exists()) {
                editTextDialog.getEditBox().setError(context.getString(R.string.zh_file_rename_exitis));
                return;
            }

            boolean renamed = renameFile(file, newFile);
            if (renamed) {
                if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
            }

            editTextDialog.dismiss();
        });

        editTextDialog.show();
    }

    public static boolean renameFile(File origin, File target) {
        return origin.renameTo(target);
    }

    public static String getFileNameWithoutExtension(String fileName, String fileExtension) {
        int dotIndex;
        if (fileExtension == null) {
            dotIndex = fileName.lastIndexOf('.');
        } else {
            dotIndex = fileName.lastIndexOf(fileExtension);
        }
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }

    public static String formattingTime(String time) {
        int T = time.indexOf('T');
        int Z = time.indexOf('Z');
        if (T == -1 || Z == -1) return time;
        return time.substring(0, T) + " " + time.substring(T + 1, Z);
    }

    @SuppressLint("DefaultLocale")
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double value = bytes;
        //循环获取合适的单位
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", value, units[unitIndex]);
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //获取软件上一次更新时间
    public static String getLastUpdateTime(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            Date date = new Date(packageInfo.lastUpdateTime);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return simpleDateFormat.format(date);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //获取版本状态信息
    public static String getVersionStatus(Context context) {
        String branch = Objects.equals(context.getString(R.string.zh_branch_info), "main") ?
                context.getString(R.string.zh_about_version_status_main_branch) :
                context.getString(R.string.zh_about_version_status_other_branch);

        String status = Objects.equals(context.getString(R.string.zh_version_status), "debug") ?
                context.getString(R.string.zh_about_version_status_debug) :
                Objects.equals(context.getString(R.string.zh_version_status), "release") ?
                        context.getString(R.string.zh_about_version_status_release) :
                        context.getString(R.string.zh_unknown);

        return "[" + branch + "] " + status;
    }

    public static void setVisibilityAnim(View view, boolean shouldShow) {
        setVisibilityAnim(view, shouldShow, 300);
    }

    public static void setVisibilityAnim(View view, boolean shouldShow, int duration) {
        if (shouldShow && view.getVisibility() != View.VISIBLE) {
            fadeAnim(view, 0, 0f, 1f, duration, () -> view.setVisibility(View.VISIBLE));
        } else if (!shouldShow && view.getVisibility() != View.GONE) {
            fadeAnim(view, 0, view.getAlpha(), 0f, duration, () -> view.setVisibility(View.GONE));
        }
    }

    public static void fadeAnim(View view, long startDelay, float begin, float end, int duration, Runnable endAction) {
        if ((view.getVisibility() != View.VISIBLE && end == 0) || (view.getVisibility() == View.VISIBLE && end == 1)) {
            if (endAction != null) PojavApplication.sExecutorService.execute(endAction);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setAlpha(begin);
        view.animate()
                .alpha(end)
                .setStartDelay(startDelay)
                .setDuration(duration)
                .withEndAction(endAction);
    }

    public static ModLoader installModPack(Context context, int type, File zipFile) throws Exception {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)) {
            String zipName = zipFile.getName();
            String packName = zipName.substring(0, zipName.lastIndexOf('.'));
            ModLoader modLoader;
            switch (type) {
                case 1: //curseforge
                    ZipEntry curseforgeEntry = modpackZipFile.getEntry("manifest.json");
                    CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest.class);

                    modLoader = curseforgeModPack(context, zipFile, packName);
                    createProfiles(packName, curseManifest.name, modLoader.getVersionId());

                    return modLoader;
                case 2: //mcbbs
                    ZipEntry mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta");

                    MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta.class);

                    modLoader = mcbbsModPack(context, zipFile, packName);
                    if (modLoader != null)
                        createProfiles(packName, mcbbsPackMeta.name, modLoader.getVersionId());

                    return modLoader;
                case 3: // modrinth
                    ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(ZipUtils.getEntryStream(modpackZipFile, "modrinth.index.json")),
                            ModrinthIndex.class); // 用于获取创建实例所需的数据

                    modLoader = modrinthModPack(zipFile, packName);
                    createProfiles(packName, modrinthIndex.name, modLoader.getVersionId());

                    return modLoader;
                default:
                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_select_modpack_local_not_supported), Toast.LENGTH_SHORT).show());
                    return null;
            }
        } finally {
            DIR_GAME_MODPACK = null;
            FileUtils.deleteQuietly(zipFile); // 删除文件（虽然文件通常来说并不会很大）
        }
    }

    public static int determineModpack(File modpack) throws Exception {
        String zipName = modpack.getName();
        String suffix = zipName.substring(zipName.lastIndexOf('.'));
        try (ZipFile modpackZipFile = new ZipFile(modpack)) {
            if (suffix.equals(".zip")) {
                ZipEntry mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta");
                ZipEntry curseforgeEntry = modpackZipFile.getEntry("manifest.json");
                if (mcbbsEntry == null && curseforgeEntry != null) {
                    CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest.class);
                    if (verifyManifest(curseManifest)) return 1; //curseforge
                } else if (mcbbsEntry != null) {
                    MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta.class);
                    if (verifyMCBBSPackMeta(mcbbsPackMeta)) return 2; // mcbbs
                }
            } else if (suffix.equals(".mrpack")) {
                ZipEntry entry = modpackZipFile.getEntry("modrinth.index.json");
                if (entry != null) {
                    ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(entry)),
                            ModrinthIndex.class);
                    if (verifyModrinthIndex(modrinthIndex)) return 3; //modrinth
                }
            }
            return 0;
        }
    }

    private static ModLoader curseforgeModPack(Context context, File zipFile, String packName) throws Exception {
        CurseforgeApi curseforgeApi = new CurseforgeApi(context.getString(R.string.curseforge_api_key));
        return curseforgeApi.installCurseforgeZip(zipFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName));
    }

    private static ModLoader modrinthModPack(File zipFile, String packName) throws Exception {
        ModrinthApi modrinthApi = new ModrinthApi();
        return modrinthApi.installMrpack(zipFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName));
    }

    private static ModLoader mcbbsModPack(Context context, File zipFile, String packName) throws Exception {
        MCBBSApi mcbbsApi = new MCBBSApi();
        return mcbbsApi.installMCBBSZip(context, zipFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName));
    }

    private static void createProfiles(String modpackName, String profileName, String versionId) {
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "./custom_instances/" + modpackName;
        profile.name = profileName;
        profile.lastVersionId = versionId;

        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.write(ProfilePathManager.getCurrentProfile());
    }

    public static boolean verifyManifest(CurseManifest manifest) { //检测是否为curseforge整合包(通过manifest.json内的数据进行判断)
        if (!"minecraftModpack".equals(manifest.manifestType)) return false;
        if (manifest.manifestVersion != 1) return false;
        if (manifest.minecraft == null) return false;
        if (manifest.minecraft.version == null) return false;
        if (manifest.minecraft.modLoaders == null) return false;
        return manifest.minecraft.modLoaders.length >= 1;
    }

    public static boolean verifyModrinthIndex(ModrinthIndex modrinthIndex) { //检测是否为modrinth整合包(通过modrinth.index.json内的数据进行判断)
        if (!"minecraft".equals(modrinthIndex.game)) return false;
        if (modrinthIndex.formatVersion != 1) return false;
        return modrinthIndex.dependencies != null;
    }

    public static boolean verifyMCBBSPackMeta(MCBBSPackMeta mcbbsPackMeta) { //检测是否为MCBBS整合包(通过mcbbs.packmeta内的数据进行判断)
        if (!"minecraftModpack".equals(mcbbsPackMeta.manifestType)) return false;
        if (mcbbsPackMeta.manifestVersion != 2) return false;
        if (mcbbsPackMeta.addons == null) return false;
        if (mcbbsPackMeta.addons[0].id == null) return false;
        return (mcbbsPackMeta.addons[0].version != null);
    }

    public static boolean checkDate(int month, int day) {
        LocalDate currentDate;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
            return currentDate.getMonthValue() == month && currentDate.getDayOfMonth() == day;
        }
        return false;
    }

    public static String getSystemLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
    }

    public static String markdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static void getWebViewAfterProcessing(WebView view) {
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String[] color = new String[2];
                Configuration configuration = view.getResources().getConfiguration();
                boolean darkMode = (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                color[0] = darkMode ? "#333333" : "#CFCFCF";
                color[1] = darkMode ? "#ffffff" : "#0E0E0E";

                String css = "body { background-color: " + color[0] + "; color: " + color[1] + "; }" +
                        "a, a:link, a:visited, a:hover, a:active {" +
                        "  color: " + color[1] + ";" +
                        "  text-decoration: none;" +
                        "  pointer-events: none;" + //禁止链接的交互性
                        "}";

                //JavaScript代码，用于将CSS样式添加到WebView中
                String js = "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "if (style.styleSheet){" +
                        "  style.styleSheet.cssText = '" + css.replace("'", "\\'") + "';" +
                        "} else {" +
                        "  style.appendChild(document.createTextNode('" + css.replace("'", "\\'") + "'));" +
                        "}" +
                        "parent.appendChild(style);";

                view.evaluateJavascript(js, null);
            }
        });
    }
}
