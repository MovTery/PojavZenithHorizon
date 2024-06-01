package com.movtery.feature;

import static com.movtery.utils.PojavZHTools.formatFileSize;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.movtery.ui.dialog.DownloadDialog;

import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    public UpdateLauncher(Context context, String tagName, String fileSize, UpdateSource updateSource) {
        this.context = context;
        this.updateSource = updateSource;
        this.apkFile = new File(context.getExternalFilesDir(null), "PojavZH.apk");
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
                fileUrl = "https://mirror.ghproxy.com/github.com/HopiHopy/PojavZH/releases/download/" + tagName + "/PojavZH.apk";
                break;
            case GITHUB_RELEASE:
            default:
                fileUrl = "https://github.com/HopiHopy/PojavZH/releases/download/" + tagName + "/PojavZH.apk";
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
                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        Runnable printTask = () -> runOnUiThread(() -> {
                            String formattedDownloaded = formatFileSize(downloadedSize[0]);
                            TextView textView = UpdateLauncher.this.downloadTipTextView.findViewById(R.id.zh_download_upload_textView);
                            textView.setText(String.format(context.getString(R.string.zh_update_downloading), formattedDownloaded, fileSize));
                        });
                        scheduler.scheduleWithFixedDelay(printTask, 0, 200, TimeUnit.MILLISECONDS);

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            downloadedSize[0] += bytesRead;
                        }
                        scheduler.shutdown();
                        finish(outputFile);
                    }
                }
            }
        });
    }

    private void finish(File outputFile) {
        runOnUiThread(UpdateLauncher.this.downloadDialog::dismiss);

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

    private void stop() {
        if (this.call == null) return;
        this.call.cancel();
        FileUtils.deleteQuietly(this.apkFile);
    }

    public enum UpdateSource {
        GITHUB_RELEASE, GHPROXY
    }
}
