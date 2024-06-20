package com.movtery.pojavzh.feature;

import static com.movtery.pojavzh.utils.ZHTools.formatFileSize;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.movtery.pojavzh.ui.dialog.DownloadDialog;
import com.movtery.pojavzh.ui.dialog.UpdateDialog;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private DownloadDialog downloadDialog;
    private TextView downloadTipTextView;
    private final File apkFile;
    private final String tagName, fileSize;
    private String destinationFilePath;
    private Call call;
    private Timer timer;

    public UpdateLauncher(Context context, String tagName, String fileSize, UpdateSource updateSource) {
        this.context = context;
        this.updateSource = updateSource;
        this.apkFile = new File(ZHTools.DIR_APP_CACHE, "PojavZH.apk");
        this.tagName = tagName;
        this.fileSize = fileSize;
        init();
    }

    private void init() {
        this.destinationFilePath = this.apkFile.getAbsolutePath();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getFileUrl())
                .build();
        this.call = client.newCall(request); //获取请求对象
    }

    private String getFileUrl() {
        String fileUrl;
        switch (updateSource) {
            case GHPROXY:
                fileUrl = "https://mirror.ghproxy.com/github.com/HopiHopy/PojavZenithHorizon/releases/download/" + tagName + "/PojavZH.apk";
                break;
            case GITHUB_RELEASE:
            default:
                fileUrl = "https://github.com/HopiHopy/PojavZenithHorizon/releases/download/" + tagName + "/PojavZH.apk";
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
                         OutputStream outputStream = new FileOutputStream(outputFile)
                    ) {
                        byte[] buffer = new byte[1024 * 1024];
                        int bytesRead;

                        runOnUiThread(() -> {
                            UpdateLauncher.this.downloadDialog = new DownloadDialog(UpdateLauncher.this.context);
                            UpdateLauncher.this.downloadTipTextView = UpdateLauncher.this.downloadDialog.getTextView();

                            UpdateLauncher.this.downloadDialog.getCancelButton().setOnClickListener(view -> {
                                UpdateLauncher.this.stop();
                                UpdateLauncher.this.downloadDialog.dismiss();
                            });
                            UpdateLauncher.this.downloadDialog.show();
                        });

                        final long[] downloadedSize = new long[1];

                        //限制刷新速度
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    String formattedDownloaded = formatFileSize(downloadedSize[0]);
                                    TextView textView = UpdateLauncher.this.downloadTipTextView.findViewById(R.id.zh_download_upload_textView);
                                    textView.setText(String.format(context.getString(R.string.zh_update_downloading), formattedDownloaded, fileSize));
                                });
                            }
                        }, 0, 80);

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
        runOnUiThread(UpdateLauncher.this.downloadDialog::dismiss);
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
        File downloadedFile = new File(ZHTools.DIR_APP_CACHE, "PojavZH.apk");

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
        runOnUiThread(() -> {
            DialogInterface.OnClickListener install = (dialogInterface, i) -> { //安装
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", outputFile);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.zh_tip))
                    .setMessage(context.getString(R.string.zh_update_success) + outputFile.getAbsolutePath())
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.global_yes), install)
                    .setNegativeButton(context.getString(android.R.string.cancel), null)
                    .show();
        });
    }

    public static synchronized void updateCheckerMainProgram(Context context, boolean ignore) {
        if (System.currentTimeMillis() - ZHTools.LAST_UPDATE_CHECK_TIME <= 5000) return;
        ZHTools.LAST_UPDATE_CHECK_TIME = System.currentTimeMillis();
        PojavApplication.sExecutorService.execute(() -> {
            int versionCode = ZHTools.getVersionCode(context);
            OkHttpClient client = new OkHttpClient();
            Request.Builder url = new Request.Builder()
                    .url(ZHTools.URL_GITHUB_RELEASE);
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
                                                ZHTools.formattingTime(jsonObject.getString("created_at")),
                                                formatFileSize(fileSize),
                                                jsonObject.getString("body"));
                                    } catch (Exception ignored) {
                                    }
                                    UpdateDialog updateDialog = new UpdateDialog(context, updateInformation);

                                    updateDialog.show();
                                });
                            } else if (!ignore) {
                                runOnUiThread(() -> {
                                    String nowVersionName = ZHTools.getVersionName(context);
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
}
