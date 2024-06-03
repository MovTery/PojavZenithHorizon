package com.movtery.utils;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class CleanUpCache {
    private static boolean isCleaning = false;
    public static void start(Context context) {
        if (isCleaning) return;
        isCleaning = true;

        long totalSize = 0;
        int fileCount = 0;
        try {
            File[] files = Tools.DIR_CACHE.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals("user_icon")) continue;

                    ++fileCount;

                    if (file.isDirectory()) {
                        totalSize += FileUtils.sizeOfDirectory(file);
                    } else {
                        totalSize += FileUtils.sizeOf(file);
                    }

                    FileUtils.deleteQuietly(file);
                }

                int finalFileCount = fileCount;
                long finalTotalSize = totalSize;
                runOnUiThread(() -> {
                    if (finalFileCount != 0) {
                        Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_clean_up, PojavZHTools.formatFileSize(finalTotalSize)), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_not_found), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_not_found), Toast.LENGTH_SHORT).show());
            }
        } finally {
            isCleaning = false;
        }
    }
}
