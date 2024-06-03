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

        long totalSize = FileUtils.sizeOfDirectory(Tools.DIR_CACHE);
        try {
            File[] files = Tools.DIR_CACHE.listFiles();
            if (files != null) {
                if (files.length == 0) {
                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_not_found), Toast.LENGTH_SHORT).show());
                    return;
                }
                for (File file : files) {
                    FileUtils.deleteQuietly(file);
                }

                runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_clean_up, PojavZHTools.formatFileSize(totalSize)), Toast.LENGTH_SHORT).show());
            }
        } finally {
            isCleaning = false;
        }
    }
}
