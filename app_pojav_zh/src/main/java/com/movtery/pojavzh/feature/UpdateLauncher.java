package com.movtery.pojavzh.feature;

import static com.movtery.pojavzh.utils.file.FileTools.formatFileSize;
import static net.kdt.pojavlaunch.Architecture.ARCH_ARM;
import static net.kdt.pojavlaunch.Architecture.ARCH_ARM64;
import static net.kdt.pojavlaunch.Architecture.ARCH_X86;
import static net.kdt.pojavlaunch.Architecture.ARCH_X86_64;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.ui.dialog.ProgressDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.dialog.UpdateDialog;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.http.CallUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.BuildConfig;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public final class UpdateLauncher {
    private static final File sApkFile = new File(PathAndUrlManager.DIR_APP_CACHE, "cache.apk");
    public static long LAST_UPDATE_CHECK_TIME = 0;
    private final Context context;
    private final UpdateSource updateSource;
    private final String versionName, tagName, fileSizeString;
    private final long fileSize;
    private final String destinationFilePath;
    private final Call call;
    private ProgressDialog dialog;
    private Timer timer;

    public UpdateLauncher(Context context, String versionName, String tagName, long fileSize, UpdateSource updateSource) {
        this.context = context;
        this.updateSource = updateSource;
        this.versionName = versionName;
        this.tagName = tagName;
        this.fileSizeString = formatFileSize(fileSize);
        this.fileSize = fileSize;

        this.destinationFilePath = sApkFile.getAbsolutePath();
        this.call = new OkHttpClient().newCall(
                PathAndUrlManager.createRequestBuilder(getDownloadUrl()).build()
        ); //获取请求对象
    }

    public static void CheckDownloadedPackage(Context context, boolean ignore) {
        if (!Objects.equals(BuildConfig.BUILD_TYPE, "release")) return;

        if (sApkFile.exists()) {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(sApkFile.getAbsolutePath(), 0);

            if (packageInfo != null) {
                String packageName = packageInfo.packageName;
                int versionCode = packageInfo.versionCode;
                int thisVersionCode = ZHTools.getVersionCode();

                if (Objects.equals(packageName, ZHTools.getPackageName()) && versionCode > thisVersionCode) {
                    installApk(context, sApkFile);
                } else {
                    FileUtils.deleteQuietly(sApkFile);
                }
            } else {
                FileUtils.deleteQuietly(sApkFile);
            }
        } else {
            //如果安装包不存在，那么将自动获取更新
            UpdateLauncher.updateCheckerMainProgram(context, ignore);
        }
    }

    private static void installApk(Context context, File outputFile) {
        runOnUiThread(() -> new TipDialog.Builder(context)
                .setMessage(StringUtils.insertNewline(context.getString(R.string.update_success), outputFile.getAbsolutePath()))
                .setCenterMessage(false)
                .setCancelable(false)
                .setConfirmClickListener(() -> { //安装
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", outputFile);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                }).buildDialog());
    }

    public static synchronized void updateCheckerMainProgram(Context context, boolean ignore) {
        if (ZHTools.getCurrentTimeMillis() - LAST_UPDATE_CHECK_TIME <= 5000) return;
        LAST_UPDATE_CHECK_TIME = ZHTools.getCurrentTimeMillis();

        String token = context.getString(R.string.api_token);
        new CallUtils(new CallUtils.CallbackListener() {
            @Override
            public void onFailure(Call call) {
                showFailToast(context, context.getString(R.string.update_fail));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showFailToast(context, context.getString(R.string.update_fail_code, response.code()));
                    Logging.e("UpdateLauncher", "Unexpected code " + response.code());
                } else {
                    Objects.requireNonNull(response.body());
                    String responseBody = response.body().string(); //解析响应体
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String versionName = jsonObject.getString("name");

                        if (ignore && Objects.equals(versionName, AllSettings.Companion.getIgnoreUpdate()))
                            return; //忽略此版本

                        String tagName = jsonObject.getString("tag_name");
                        JSONArray assetsJson = jsonObject.getJSONArray("assets");
                        JSONObject firstAsset = assetsJson.getJSONObject(0);
                        long fileSize = firstAsset.getLong("size");
                        int githubVersion = 0;
                        try {
                            githubVersion = Integer.parseInt(tagName);
                        } catch (Exception e) {
                            Logging.e("Parse github version", Tools.printToString(e));
                        }

                        if (ZHTools.getVersionCode() < githubVersion) {
                            runOnUiThread(() -> {
                                UpdateDialog.UpdateInformation updateInformation = new UpdateDialog.UpdateInformation();
                                try {
                                    updateInformation.information(versionName,
                                            tagName,
                                            StringUtils.formattingTime(jsonObject.getString("created_at")),
                                            fileSize,
                                            jsonObject.getString("body"));
                                } catch (Exception e) {
                                    Logging.e("Init update information", Tools.printToString(e));
                                }
                                UpdateDialog updateDialog = new UpdateDialog(context, updateInformation);

                                updateDialog.show();
                            });
                        } else if (!ignore) {
                            runOnUiThread(() -> {
                                String nowVersionName = ZHTools.getVersionName();
                                runOnUiThread(() -> Toast.makeText(context,
                                        StringUtils.insertSpace(context.getString(R.string.update_without), nowVersionName),
                                        Toast.LENGTH_SHORT).show());
                            });
                        }
                    } catch (Exception e) {
                        Logging.e("Check Update", Tools.printToString(e));
                    }
                }
            }
        }, PathAndUrlManager.URL_GITHUB_RELEASE, token.equals("DUMMY") ? null : token).enqueue();
    }

    private static void showFailToast(Context context, String resString) {
        runOnUiThread(() -> Toast.makeText(context, resString, Toast.LENGTH_SHORT).show());
    }

    private static String getArchModel() {
        int arch = Tools.DEVICE_ARCHITECTURE;
        if (arch == ARCH_ARM64) return "arm64-v8a";
        if (arch == ARCH_ARM) return "armeabi-v7a";
        if (arch == ARCH_X86_64) return "x86_64";
        if (arch == ARCH_X86) return "x86";
        return null;
    }

    private String getDownloadUrl() {
        String fileUrl;
        String archModel = getArchModel();
        String githubUrl = "github.com/MovTery/PojavZenithHorizon/releases/download/" + tagName + "/" + "PojavZenithHorizon-" + versionName +
                (archModel != null ? String.format("-%s", archModel) : "") + ".apk";
        switch (updateSource) {
            case GHPROXY:
                fileUrl = "https://mirror.ghproxy.com/" + githubUrl;
                break;
            case GITHUB_RELEASE:
            default:
                fileUrl = "https://" + githubUrl;
                break;
        }
        return fileUrl;
    }

    public void start() {
        this.call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showFailToast(context, context.getString(R.string.update_fail));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showFailToast(context, context.getString(R.string.update_fail_code, response.code()));
                    throw new IOException("Unexpected code " + response);
                } else {
                    File outputFile = new File(UpdateLauncher.this.destinationFilePath);
                    Objects.requireNonNull(response.body());
                    try (InputStream inputStream = response.body().byteStream();
                         OutputStream outputStream = Files.newOutputStream(outputFile.toPath())
                    ) {
                        byte[] buffer = new byte[1024 * 1024];
                        int bytesRead;

                        runOnUiThread(() -> {
                            UpdateLauncher.this.dialog = new ProgressDialog(UpdateLauncher.this.context, () -> {
                                UpdateLauncher.this.stop();
                                return true;
                            });
                            UpdateLauncher.this.dialog.show();
                        });

                        final long[] downloadedSize = new long[1];
                        final long[] lastSize = {0};
                        final long[] lastTime = {ZHTools.getCurrentTimeMillis()};

                        //限制刷新速度
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                long size = downloadedSize[0];
                                long currentTime = ZHTools.getCurrentTimeMillis();
                                double timeElapsed = (currentTime - lastTime[0]) / 1000.0;
                                long sizeChange = size - lastSize[0];
                                long rate = (long) (sizeChange / timeElapsed);

                                lastSize[0] = size;
                                lastTime[0] = currentTime;

                                runOnUiThread(() -> {
                                    String formattedDownloaded = formatFileSize(size);
                                    UpdateLauncher.this.dialog.updateProgress(size, fileSize);
                                    UpdateLauncher.this.dialog.updateRate(rate > 0 ? rate : 0L);
                                    UpdateLauncher.this.dialog.updateText(String.format(context.getString(R.string.update_downloading), formattedDownloaded, fileSizeString));
                                });
                            }
                        }, 0, 120);

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedSize[0] += bytesRead;
                        }
                        finish(outputFile);
                    }
                }
            }
        });
    }

    private void finish(File outputFile) {
        runOnUiThread(UpdateLauncher.this.dialog::dismiss);
        timer.cancel();

        installApk(context, outputFile);
    }

    private void stop() {
        this.call.cancel();
        this.timer.cancel();
        FileUtils.deleteQuietly(sApkFile);
    }

    public enum UpdateSource {
        GITHUB_RELEASE, GHPROXY
    }
}
