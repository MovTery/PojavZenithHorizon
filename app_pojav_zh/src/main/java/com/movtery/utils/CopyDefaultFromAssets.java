package com.movtery.utils;

import android.content.Context;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.IOException;

public class CopyDefaultFromAssets {
    public static void copyFromAssets(Context context) throws IOException {
        //默认控制布局
        if (checkDirectoryEmpty(Tools.CTRLMAP_PATH)) {
            Tools.copyAssetFile(context, "default.json", Tools.CTRLMAP_PATH, false);
        }
        //默认虚拟鼠标
        if (checkDirectoryEmpty(PojavZHTools.DIR_CUSTOM_MOUSE)) {
            Tools.copyAssetFile(context, "default_mouse.png", PojavZHTools.DIR_CUSTOM_MOUSE, false);
        }
    }

    public static boolean checkDirectoryEmpty(String dir) {
        File controlDir = new File(dir);
        File[] files = controlDir.listFiles();
        if (files != null) {
            return files.length == 0;
        } else {
            return true;
        }
    }
}
