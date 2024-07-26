package com.movtery.pojavzh.feature;

import static com.movtery.pojavzh.utils.ZHTools.formatFileSize;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.movtery.pojavzh.ui.dialog.ProgressDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.dialog.UpdateDialog;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.http.CallUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;

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
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLauncher {
    private final Context context;
    private final UpdateSource updateSource;
    private ProgressDialog dialog;
    private final File apkFile;
    private final String versionName, tagName, fileSizeString;
    private final long fileSize;
    private String destinationFilePath;
    private Call call;
    private Timer timer;

    public UpdateLauncher(Context context, String versionName, String tagName, long fileSize, UpdateSource updateSource) {
        this.context = context;
        this.updateSource = updateSource;
        this.apkFile = new File(ZHTools.DIR_APP_CACHE, "cache.apk");
        this.versionName = versionName;
        this.tagName = tagName;
        this.fileSizeString = formatFileSize(fileSize);
        this.fileSize = fileSize;
        init();
    }

    private void init() {
        this.destinationFilePath = this.apkFile.getAbsolutePath();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getDownloadUrl())
                .build();
        this.call = client.newCall(request); //获取请求对象
    }

    private String getDownloadUrl() {
        String fileUrl;
        String githubUrl = "github.com/MovTery/PojavZenithHorizon/releases/download/" + tagName + "/" + "PojavZenithHorizon-" + versionName + ".apk";
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
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(UpdateLauncher.this.context, context.getString(R.string.zh_update_fail), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
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

                        //限制刷新速度
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                long size = downloadedSize[0];
                                runOnUiThread(() -> {
                                    String formattedDownloaded = formatFileSize(size);
                                    UpdateLauncher.this.dialog.updateProgress(size, fileSize);
                                    UpdateLauncher.this.dialog.updateText(String.format(context.getString(R.string.zh_update_downloading), formattedDownloaded, fileSizeString));
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
        if (this.call == null) return;
        this.call.cancel();
        this.timer.cancel();
        FileUtils.deleteQuietly(this.apkFile);
    }

    public enum UpdateSource {
        GITHUB_RELEASE, GHPROXY
    }

    public static void CheckDownloadedPackage(Context context, boolean ignore) {
        File downloadedFile = new File(ZHTools.DIR_APP_CACHE, "cache.apk");

        if (downloadedFile.exists()) {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadedFile.getAbsolutePath(), 0);

            if (packageInfo != null) {
                String packageName = packageInfo.packageName;
                int versionCode = packageInfo.versionCode;

                int thisVersionCode = ZHTools.getVersionCode(context);
                DEFAULT_PREF.edit().putInt("launcherVersionCode", thisVersionCode).apply();

                if (Objects.equals(packageName, "net.kdt.pojavlaunch.zh") && versionCode > thisVersionCode) {
                    installApk(context, downloadedFile);
                } else {
                    FileUtils.deleteQuietly(downloadedFile);
                }
            } else {
                FileUtils.deleteQuietly(downloadedFile);
            }
        } else {
            //如果安装包不存在，那么将自动获取更新
            UpdateLauncher.updateCheckerMainProgram(context, ignore);
        }
    }

    private static void installApk(Context context, File outputFile) {
        runOnUiThread(() -> new TipDialog.Builder(context)
                .setMessage(StringUtils.insertSpace(context.getString(R.string.zh_update_success), outputFile.getAbsolutePath()))
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
        if (ZHTools.getCurrentTimeMillis() - ZHTools.LAST_UPDATE_CHECK_TIME <= 5000) return;
        ZHTools.LAST_UPDATE_CHECK_TIME = ZHTools.getCurrentTimeMillis();

        String token = context.getString(R.string.zh_api_token);
        new CallUtils(new CallUtils.CallbackListener() {
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
                        } catch (Exception e) {
                            Log.e("Parse github version", e.toString());
                        }

                        if (ZHTools.getVersionCode(context) < githubVersion) {
                            runOnUiThread(() -> {
                                UpdateDialog.UpdateInformation updateInformation = new UpdateDialog.UpdateInformation();
                                try {
                                    updateInformation.information(versionName,
                                            tagName,
                                            ZHTools.formattingTime(jsonObject.getString("created_at")),
                                            fileSize,
                                            jsonObject.getString("body"));
                                } catch (Exception e) {
                                    Log.e("Init update information", e.toString());
                                }
                                UpdateDialog updateDialog = new UpdateDialog(context, updateInformation);

                                updateDialog.show();
                            });
                        } else if (!ignore) {
                            runOnUiThread(() -> {
                                String nowVersionName = ZHTools.getVersionName(context);
                                runOnUiThread(() -> Toast.makeText(context,
                                        StringUtils.insertSpace(context.getString(R.string.zh_update_without), nowVersionName),
                                        Toast.LENGTH_SHORT).show());
                            });
                        }
                    } catch (Exception e) {
                        Log.e("Check Update", e.toString());
                    }
                }
            }
        }, ZHTools.URL_GITHUB_RELEASE, token.equals("DUMMY") ? null : token).start();
    }
}
