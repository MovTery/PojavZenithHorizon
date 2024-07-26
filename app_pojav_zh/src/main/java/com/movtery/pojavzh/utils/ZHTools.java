package com.movtery.pojavzh.utils;

import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.Tools.getFileName;
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
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.DocumentsContract;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
import net.kdt.pojavlaunch.Tools;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.utils.image.ImageUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
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

public class ZHTools {
    public static String DIR_GAME_DEFAULT;
    public static String DIR_CUSTOM_MOUSE;
    public static String DIR_LOGIN;
    public static File DIR_BACKGROUND;
    public static File DIR_APP_CACHE;
    public static File DIR_USER_ICON;
    public static File FILE_CUSTOM_MOUSE;
    public static File FILE_PROFILE_PATH;
    public static final String URL_GITHUB_RELEASE = "https://api.github.com/repos/MovTery/PojavZenithHorizon/releases/latest";
    public static final String URL_GITHUB_HOME = "https://api.github.com/repos/MovTery/PojavZenithHorizon/contents/";
    public static final String URL_GITHUB_POJAVLAUNCHER = "https://github.com/PojavLauncherTeam/PojavLauncher";
    public static final String URL_MINECRAFT = "https://www.minecraft.net/";
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

    public static boolean isEnglish(Context context) {
        LocaleList locales = context.getResources().getConfiguration().getLocales();
        return locales.get(0).getLanguage().equals("en");
    }

    public static File copyFileInBackground(Context context, Uri fileUri, String rootPath) {
        String fileName = getFileName(context, fileUri);
        File outputFile = new File(rootPath, fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            Objects.requireNonNull(inputStream);
            Files.copy(inputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
        if (!backgroundImage.exists() || !ImageUtils.isImage(backgroundImage)) return null;
        return backgroundImage;
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
        alarmManager.set(AlarmManager.RTC, getCurrentTimeMillis() + 1000, restartIntent);

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

    public static File getLatestFile(String folderPath, int modifyTime) {
        if (folderPath == null) return null;
        return getLatestFile(new File(folderPath), modifyTime);
    }

    public static File getLatestFile(File folder, long modifyTime) {
        if (folder == null || !folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles((dir, name) -> !name.startsWith("."));
        if (files == null || files.length == 0) {
            return null;
        }

        List<File> fileList = Arrays.asList(files);
        fileList.sort(Comparator.comparingLong(File::lastModified).reversed());

        if (modifyTime > 0) {
            long difference = (getCurrentTimeMillis() - fileList.get(0).lastModified()) / 1000; //转换为秒
            if (difference >= modifyTime) {
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

        new EditTextDialog.Builder(context)
                .setTitle(R.string.zh_rename)
                .setEditText(getFileNameWithoutExtension(fileName, suffix))
                .setConfirmListener(editBox -> {
                    String newName = editBox.getText().toString().replace("/", "");

                    if (Objects.equals(fileName, newName)) {
                        return true;
                    }

                    if (newName.isEmpty()) {
                        editBox.setError(context.getString(R.string.zh_file_rename_empty));
                        return false;
                    }

                    File newFile = new File(fileParent, newName + suffix);
                    if (newFile.exists()) {
                        editBox.setError(context.getString(R.string.zh_file_rename_exitis));
                        return false;
                    }

                    boolean renamed = file.renameTo(newFile);
                    if (renamed) {
                        if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
                    }

                    return true;
                }).buildDialog();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void renameFileListener(Context context, Runnable runnable, File file) {
        String fileParent = file.getParent();
        String fileName = file.getName();

        new EditTextDialog.Builder(context)
                .setTitle(R.string.zh_rename)
                .setEditText(fileName)
                .setConfirmListener(editBox -> {
                    String newName = editBox.getText().toString().replace("/", "");

                    if (Objects.equals(fileName, newName)) {
                        return true;
                    }

                    if (newName.isEmpty()) {
                        editBox.setError(context.getString(R.string.zh_file_rename_empty));
                        return false;
                    }

                    File newFile = new File(fileParent, newName);
                    if (newFile.exists()) {
                        editBox.setError(context.getString(R.string.zh_file_rename_exitis));
                        return false;
                    }

                    boolean renamed = renameFile(file, newFile);
                    if (renamed) {
                        if (runnable != null) PojavApplication.sExecutorService.execute(runnable);
                    }

                    return true;
                }).buildDialog();
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
        return StringUtils.insertSpace(time.substring(0, T), time.substring(T + 1, Z));
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

    public static boolean checkDate(int month, int day) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getMonthValue() == month && currentDate.getDayOfMonth() == day;
    }

    public static boolean areaChecks() {
        return getSystemLanguageName().equals("zh");
    }

    public static String getSystemLanguageName() {
        return Locale.getDefault().getLanguage();
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

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
