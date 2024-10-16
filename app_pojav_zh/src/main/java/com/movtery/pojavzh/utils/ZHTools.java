package com.movtery.pojavzh.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;

import net.kdt.pojavlaunch.BuildConfig;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public final class ZHTools {
    private ZHTools() {
    }

    public static void onBackPressed(FragmentActivity fragmentActivity) {
        fragmentActivity.getOnBackPressedDispatcher().onBackPressed();
    }

    public static boolean isEnglish(Context context) {
        LocaleList locales = context.getResources().getConfiguration().getLocales();
        return locales.get(0).getLanguage().equals("en");
    }

    public static void setTooltipText(ImageView... views) {
        for (ImageView view : views) {
            setTooltipText(view, view.getContentDescription());
        }
    }

    public static void setTooltipText(View view, CharSequence tooltip) {
        TooltipCompat.setTooltipText(view, tooltip);
    }

    public synchronized static Drawable customMouse(Context context) {
        File mouseFile = getCustomMouse();
        if (mouseFile == null) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }

        // 鼠标：自定义鼠标图片
        if (mouseFile.exists()) {
            return Drawable.createFromPath(mouseFile.getAbsolutePath());
        } else {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }
    }

    public static File getCustomMouse() {
        String customMouse = AllSettings.Companion.getCustomMouse();
        if (customMouse == null) {
            return null;
        }
        return new File(PathAndUrlManager.DIR_CUSTOM_MOUSE, customMouse);
    }

    public static void swapFragmentWithAnim(Fragment fragment, Class<? extends Fragment> fragmentClass,
                                            @Nullable String fragmentTag, @Nullable Bundle bundle) {
        FragmentTransaction transaction = fragment.requireActivity().getSupportFragmentManager().beginTransaction();

        boolean animation = AllSettings.Companion.getAnimation();
        if (animation) transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);

        transaction.setReorderingAllowed(true).replace(R.id.container_fragment, fragmentClass, bundle, fragmentTag);
        transaction.addToBackStack(fragmentClass.getName());
        if (animation && fragment instanceof FragmentWithAnim) {
            ((FragmentWithAnim) fragment).slideOut();
        }
        transaction.commit();
    }

    public static void addFragment(Fragment fragment, Class<? extends Fragment> fragmentClass,
                                   @Nullable String fragmentTag, @Nullable Bundle bundle) {
        FragmentTransaction transaction = fragment.requireActivity().getSupportFragmentManager().beginTransaction();

        if (AllSettings.Companion.getAnimation()) transaction.setCustomAnimations(R.anim.cut_into, R.anim.cut_out, R.anim.cut_into, R.anim.cut_out);

        transaction.setReorderingAllowed(true)
                .addToBackStack(fragmentClass.getName())
                .add(R.id.container_fragment, fragmentClass, bundle, fragmentTag)
                .hide(fragment)
                .commit();
    }

    public static void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static File getGameDirPath(String gameDir) {
        if (gameDir != null) {
            if (gameDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
                return new File(gameDir.replace(Tools.LAUNCHERPROFILES_RTPREFIX, ProfilePathManager.getCurrentPath() + "/"));
            else
                return new File(ProfilePathManager.getCurrentPath(), gameDir);
        }
        return new File(PathAndUrlManager.DIR_GAME_DEFAULT);
    }

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getPackageName() {
        return BuildConfig.APPLICATION_ID;
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
        String branch = Objects.equals(context.getString(R.string.branch_info), "main") ?
                context.getString(R.string.about_version_status_main_branch) :
                context.getString(R.string.about_version_status_other_branch);

        String status;
        if (getVersionName().contains("pre-release")) status = context.getString(R.string.about_version_status_pre_release);
        else if (Objects.equals(BuildConfig.BUILD_TYPE, "release")) status = context.getString(R.string.version_release);
        else status = context.getString(R.string.about_version_status_debug);

        return "[" + branch + "] " + status;
    }

    public static boolean checkDate(int month, int day) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getMonthValue() == month && currentDate.getDayOfMonth() == day;
    }

    public static boolean areaChecks(String area) {
        return getSystemLanguageName().equals(area);
    }

    public static String getSystemLanguageName() {
        return Locale.getDefault().getLanguage();
    }

    public static String getSystemLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry().toLowerCase();
    }

    public static boolean isAdrenoGPU() {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Logging.e("CheckVendor", "Failed to get EGL display");
            return false;
        }

        if (!EGL14.eglInitialize(eglDisplay, null, 0, null, 0)) {
            Logging.e("CheckVendor", "Failed to initialize EGL");
            return false;
        }

        int[] eglAttributes = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, eglAttributes, 0, configs, 0, 1, numConfigs, 0) || numConfigs[0] == 0) {
            EGL14.eglTerminate(eglDisplay);
            Logging.e("CheckVendor", "Failed to choose an EGL config");
            return false;
        }

        int[] contextAttributes = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,  // OpenGL ES 2.0
                EGL14.EGL_NONE
        };

        EGLContext context = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        if (context == EGL14.EGL_NO_CONTEXT) {
            EGL14.eglTerminate(eglDisplay);
            Logging.e("CheckVendor", "Failed to create EGL context");
            return false;
        }

        if (!EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, context)) {
            EGL14.eglDestroyContext(eglDisplay, context);
            EGL14.eglTerminate(eglDisplay);
            Logging.e("CheckVendor", "Failed to make EGL context current");
            return false;
        }

        String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
        String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
        boolean isAdreno = (vendor != null && renderer != null &&
                vendor.equalsIgnoreCase("Qualcomm") &&
                renderer.toLowerCase().contains("adreno"));

        // Cleanup
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(eglDisplay, context);
        EGL14.eglTerminate(eglDisplay);

        Logging.d("CheckVendor", "Running on Adreno GPU: " + isAdreno);
        return isAdreno;
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
