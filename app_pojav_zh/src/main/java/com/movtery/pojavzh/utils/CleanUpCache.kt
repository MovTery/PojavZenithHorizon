package com.movtery.pojavzh.utils;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanUpCache {
    private static boolean isCleaning = false;
    public static void start(Context context) {
        if (isCleaning) return;
        isCleaning = true;

        long totalSize = 0;
        int fileCount = 0;
        try {
            List<File> list = getList(Tools.DIR_CACHE.listFiles(), ZHTools.DIR_APP_CACHE.listFiles());

            for (File file : list) {
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
                    Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_clean_up, ZHTools.formatFileSize(finalTotalSize)), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.zh_clear_up_cache_not_found), Toast.LENGTH_SHORT).show();
                }
            });
        } finally {
            isCleaning = false;
        }
    }

    private static List<File> getList(File[]... filesArray) {
        List<File> filesList = new ArrayList<>();
        for (File[] fileArray : filesArray) {
            if (fileArray != null) {
                filesList.addAll(Arrays.asList(fileArray));
            }
        }

        return filesList;
    }
}
