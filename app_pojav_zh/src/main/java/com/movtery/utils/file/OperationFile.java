package com.movtery.utils.file;

import static com.movtery.utils.PojavZHTools.formatFileSize;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.widget.TextView;

import com.movtery.ui.dialog.DownloadDialog;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class OperationFile {
    private final Context context;
    private final Runnable runnable;
    private final OperationFileFunction operationFileFunction;
    private Future<?> currentTask;
    private ScheduledExecutorService scheduler;

    public OperationFile(Context context, Runnable runnable, OperationFileFunction operationFileFunction) {
        this.context = context;
        this.runnable = runnable;
        this.operationFileFunction = operationFileFunction;
    }

    public void operationFile(List<File> selectedFiles) {
        runOnUiThread(() -> {
            DownloadDialog dialog = new DownloadDialog(context);
            TextView textView = dialog.getTextView();
            textView.setText(context.getString(
                    R.string.zh_file_operation_file, "0B", "0B", 0));

            dialog.getCancelButton().setOnClickListener(view -> {
                cancelTask();
                dialog.dismiss();
            });

            AtomicLong totalFileSize = new AtomicLong(0);
            AtomicLong fileSize = new AtomicLong(0);
            AtomicLong fileCount = new AtomicLong(0);

            currentTask = PojavApplication.sExecutorService.submit(() -> {
                scheduler = Executors.newSingleThreadScheduledExecutor();
                Runnable printTask = () -> runOnUiThread(() -> textView.setText(context.getString(
                        R.string.zh_file_operation_file,
                        formatFileSize(fileSize.get()),
                        PojavZHTools.formatFileSize(totalFileSize.get()),
                        fileCount.get())));
                scheduler.scheduleWithFixedDelay(printTask, 0, 200, TimeUnit.MILLISECONDS);

                List<File> preDeleteFiles = new ArrayList<>();
                selectedFiles.forEach(selectedFile -> {
                    if (selectedFile.isDirectory()) {
                        if (currentTask.isCancelled()) {
                            return;
                        }
                        Collection<File> allFiles = FileUtils.listFiles(selectedFile, null, true);
                        allFiles.forEach(file1 -> {
                            if (currentTask.isCancelled()) {
                                return;
                            }
                            fileCount.addAndGet(1);
                            preDeleteFiles.add(file1);
                            fileSize.addAndGet(FileUtils.sizeOf(file1));
                        });
                    } else {
                        fileSize.addAndGet(FileUtils.sizeOf(selectedFile));
                    }
                    fileCount.addAndGet(1);
                    preDeleteFiles.add(selectedFile);
                });
                totalFileSize.set(fileSize.get());

                if (totalFileSize.get() > 10 * 1024 * 1024) {
                    runOnUiThread(dialog::show); //如果预处理文件总大小大于10MB，则显示弹窗，避免处理过快，弹窗一瞬间消失的问题
                }

                preDeleteFiles.forEach(file -> {
                    if (currentTask.isCancelled()) {
                        return;
                    }
                    fileSize.addAndGet(-FileUtils.sizeOf(file));
                    fileCount.getAndDecrement();
                    operationFileFunction.operationFile(file);
                });
                runOnUiThread(dialog::dismiss);
                scheduler.shutdown();
                finish();
            });
        });
    }

    private void cancelTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow();
            }
            finish();
        }
    }

    private void finish() {
        if (runnable != null) {
            PojavApplication.sExecutorService.execute(runnable);
        }
    }

    public interface OperationFileFunction {
        void operationFile(File file);
    }
}
