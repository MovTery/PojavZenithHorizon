package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.Tools.getFileName;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.dialog.EditTextDialog;
import net.kdt.pojavlaunch.dialog.UpdateDialog;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.MCBBSApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import net.kdt.pojavlaunch.modloaders.modpacks.models.MCBBSPackMeta;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PojavZHTools {
    public static String DIR_GAME_MODPACK = null;
    public static String DIR_GAME_DEFAULT;
    public static String DIR_CUSTOM_MOUSE;
    public static String DIR_LOGIN;
    public static File FILE_CUSTOM_MOUSE;
    public static String URL_GITHUB_RELEASE = "https://api.github.com/repos/HopiHopy/PojavZH/releases/latest";
    public static String URL_GITHUB_HOME = "https://api.github.com/repos/HopiHopy/PojavZH/contents/";
    public static long LAST_UPDATE_CHECK_TIME = 0;

    private PojavZHTools() {
    }

    public static void initContextConstants() {
        PojavZHTools.DIR_GAME_DEFAULT = DIR_GAME_HOME + "/.minecraft/instance/default";
        PojavZHTools.DIR_CUSTOM_MOUSE = DIR_GAME_HOME + "/mouse";
        PojavZHTools.DIR_LOGIN = DIR_GAME_HOME + "/login";
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

    public static void customMouse(ImageView mouse, Context context) {
        FILE_CUSTOM_MOUSE = new File(DIR_CUSTOM_MOUSE, DEFAULT_PREF.getString("custom_mouse", "default_mouse.png"));

        // 鼠标：自定义鼠标图片
        if (FILE_CUSTOM_MOUSE.exists()) {
            Bitmap mouseBitmap = BitmapFactory.decodeFile(FILE_CUSTOM_MOUSE.getAbsolutePath());
            mouse.setImageBitmap(mouseBitmap);
        } else {
            mouse.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme()));
        }
    }

    public static Drawable customMouse(Context context) {
        FILE_CUSTOM_MOUSE = new File(DIR_CUSTOM_MOUSE, DEFAULT_PREF.getString("custom_mouse", "default_mouse.png"));

        // 鼠标：自定义鼠标图片
        if (FILE_CUSTOM_MOUSE.exists()) {
            Bitmap mouseBitmap = BitmapFactory.decodeFile(FILE_CUSTOM_MOUSE.getAbsolutePath());
            return new BitmapDrawable(mouseBitmap);
        } else {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }
    }

    public static Drawable getIcon(String pngFilePath, Context context) {
        Bitmap bitmap = BitmapFactory.decodeFile(pngFilePath);
        if (bitmap == null) {
            return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
        }

        float scale = Math.min(((float) 48 / bitmap.getWidth()), ((float) 48 / bitmap.getHeight())); //保持高宽比，缩放

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        bitmap.recycle();

        return new BitmapDrawable(context.getResources(), scaledBitmap);
    }

    public static boolean isImage(File file) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(fileExtension);
        return mimeType != null && mimeType.startsWith("image/");
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

    public static File getGameDirPath(String gameDir) {
        if (gameDir != null) {
            if (gameDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
                return new File(gameDir.replace(Tools.LAUNCHERPROFILES_RTPREFIX, DIR_GAME_HOME + "/"));
            else
                return new File(DIR_GAME_HOME, gameDir);
        }
        return new File(DIR_GAME_DEFAULT);
    }

    public static File getLatestFile(File folder) {
        if (!folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles((dir, name) -> !name.startsWith(".")); //排除隐藏文件
        if (files == null || files.length == 0) {
            return null;
        }

        List<File> fileList = Arrays.asList(files);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileList.sort(Comparator.comparingLong(File::lastModified).reversed());
        } else {
            return null;
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
    public static void renameFileListener(Context context, FileListView fileListView, File file) {
        String fileParent = file.getParent();
        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf('.')); //防止修改后缀名，先将后缀名分离出去

        EditTextDialog editTextDialog = new EditTextDialog(context, context.getString(R.string.zh_rename), null, fileName.substring(0, fileName.lastIndexOf(suffix)), null);
        editTextDialog.setConfirm(v -> {
            String newName = editTextDialog.getEditBox().getText().toString().replace("/", "");
            if (!newName.isEmpty()) {
                File newFile = new File(fileParent, newName + suffix);
                boolean renamed = file.renameTo(newFile);
                if (renamed) {
                    Toast.makeText(context, context.getString(R.string.zh_file_renamed) + file.getName() + " -> " + newName + suffix, Toast.LENGTH_SHORT).show();
                    fileListView.refreshPath();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.zh_file_rename_empty), Toast.LENGTH_SHORT).show();
            }

            editTextDialog.dismiss();
        });

        editTextDialog.show();
    }

    public static void updateChecker(Context context) {
        updateCheckerMainProgram(context, true);
    }

    public static void updateChecker(Context context, boolean ignore) {
        updateCheckerMainProgram(context, ignore);
    }

    public static synchronized void updateCheckerMainProgram(Context context, boolean ignore) {
        if (System.currentTimeMillis() - LAST_UPDATE_CHECK_TIME <= 5000) return;
        LAST_UPDATE_CHECK_TIME = System.currentTimeMillis();
        PojavApplication.sExecutorService.execute(() -> {
            int versionCode = getVersionCode(context);
            OkHttpClient client = new OkHttpClient();
            Request.Builder url = new Request.Builder()
                    .url(URL_GITHUB_RELEASE);
            if (!context.getString(R.string.zh_api_token).equals("DUMMY")) {
                url.header("Authorization", "token " + context.getString(R.string.zh_api_token));
            }
            Request request = url.build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_update_fail), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        Objects.requireNonNull(response.body());
                        String responseBody = response.body().string(); //解析响应体
                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String versionName = jsonObject.getString("name");

                            if (ignore && versionName.equals(DEFAULT_PREF.getString("ignoreUpdate", null)))
                                return; //忽略此版本

                            String tagName = jsonObject.getString("tag_name");
                            JSONArray assetsJson = jsonObject.getJSONArray("assets");
                            JSONObject firstAsset = assetsJson.getJSONObject(0);
                            long fileSize = firstAsset.getLong("size");
                            int githubVersion = 0;
                            try {
                                githubVersion = Integer.parseInt(tagName);
                            } catch (Exception ignored) {
                            }

                            if (versionCode < githubVersion) {
                                runOnUiThread(() -> {
                                    UpdateDialog.UpdateInformation updateInformation = new UpdateDialog.UpdateInformation();
                                    try {
                                        updateInformation.information(versionName,
                                                tagName,
                                                formattingTime(jsonObject.getString("created_at")),
                                                formatFileSize(fileSize),
                                                jsonObject.getString("body"));
                                    } catch (Exception ignored) {
                                    }
                                    UpdateDialog updateDialog = new UpdateDialog(context, updateInformation);

                                    updateDialog.show();
                                });
                            } else if (!ignore) {
                                runOnUiThread(() -> {
                                    String nowVersionName = getVersionName(context);
                                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_update_without) + " " + nowVersionName, Toast.LENGTH_SHORT).show());
                                });
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        });
    }

    public static String formattingTime(String time) {
        int T = time.indexOf('T');
        int Z = time.indexOf('Z');
        if (T == -1 || Z == -1) return time;
        return time.substring(0, T) + " " + time.substring(T + 1, Z);
    }

    @SuppressLint("DefaultLocale")
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        final double value = bytes / (1024.0 * 1024);
        return String.format("%.2f MB", value);
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
        if (shouldShow && view.getVisibility() != View.VISIBLE) {
            fadeAnim(view, 0f, 1f, 300, () -> view.setVisibility(View.VISIBLE));
        } else if (!shouldShow && view.getVisibility() != View.GONE) {
            fadeAnim(view, view.getAlpha(), 0f, 300, () -> view.setVisibility(View.GONE));
        }
    }

    public static void fadeAnim(View view, float begin, float end, int duration, Runnable endAction) {
        view.setAlpha(begin);
        view.animate()
                .alpha(end)
                .setDuration(duration)
                .withEndAction(endAction == null ? () -> {
                } : endAction);
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
        return curseforgeApi.installCurseforgeZip(zipFile, new File(DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static ModLoader modrinthModPack(File zipFile, String packName) throws Exception {
        ModrinthApi modrinthApi = new ModrinthApi();
        return modrinthApi.installMrpack(zipFile, new File(DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static ModLoader mcbbsModPack(Context context, File zipFile, String packName) throws Exception {
        MCBBSApi mcbbsApi = new MCBBSApi();
        return mcbbsApi.installMCBBSZip(context, zipFile, new File(DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static void createProfiles(String modpackName, String profileName, String versionId) {
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "./custom_instances/" + modpackName;
        profile.name = profileName;
        profile.lastVersionId = versionId;

        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.write();
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

    public static String getDefaultLanguage() {
        String country = Locale.getDefault().getCountry();
        switch (country) {
            case "HK":
                return "zh_hk";
            case "TW":
                return "zh_tw";
            case "CN":
            default:
                return "zh_cn";
        }
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
