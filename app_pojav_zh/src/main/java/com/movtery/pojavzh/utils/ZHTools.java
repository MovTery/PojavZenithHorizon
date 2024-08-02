package com.movtery.pojavzh.utils;

import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.subassembly.background.BackgroundManager;
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType;
import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.image.ImageUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public class ZHTools {
    public static final String URL_GITHUB_RELEASE = "https://api.github.com/repos/MovTery/PojavZenithHorizon/releases/latest";
    public static final String URL_GITHUB_HOME = "https://api.github.com/repos/MovTery/PojavZenithHorizon/contents/";
    public static final String URL_GITHUB_POJAVLAUNCHER = "https://github.com/PojavLauncherTeam/PojavLauncher";
    public static final String URL_MINECRAFT = "https://www.minecraft.net/";
    public static final String URL_SUPPORT = "https://afdian.com/a/MovTery";
    public static String DIR_GAME_DEFAULT;
    public static String DIR_CUSTOM_MOUSE;
    public static String DIR_LOGIN;
    public static File DIR_BACKGROUND;
    public static File DIR_APP_CACHE;
    public static File DIR_USER_ICON;
    public static File FILE_CUSTOM_MOUSE;
    public static File FILE_PROFILE_PATH;
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
            FileTools.mkdirs(ZHTools.DIR_BACKGROUND);
        }
    }

    public static void onBackPressed(FragmentActivity fragmentActivity) {
        fragmentActivity.getOnBackPressedDispatcher().onBackPressed();
    }

    public static boolean isEnglish(Context context) {
        LocaleList locales = context.getResources().getConfiguration().getLocales();
        return locales.get(0).getLanguage().equals("en");
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

        if (PREF_ANIMATION) transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);

        if (addToBackStack) transaction.addToBackStack(fragmentClass.getName());
        transaction.setReorderingAllowed(true)
                .replace(R.id.zh_settings_fragment, fragmentClass, bundle, fragmentTag)
                .commit();
    }

    public static void swapFragmentWithAnim(Fragment fragment, Class<? extends Fragment> fragmentClass,
                                            @Nullable String fragmentTag, @Nullable Bundle bundle) {
        FragmentTransaction transaction = fragment.requireActivity().getSupportFragmentManager().beginTransaction();

        if (PREF_ANIMATION) transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);

        transaction.setReorderingAllowed(true).replace(R.id.container_fragment, fragmentClass, bundle, fragmentTag);
        transaction.addToBackStack(fragmentClass.getName());
        if (fragment instanceof FragmentWithAnim) {
            ((FragmentWithAnim) fragment).slideOut();
        }
        transaction.commit();
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
