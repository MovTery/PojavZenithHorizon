package com.movtery.utils;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ModLoaderList {
    private static final List<String> modloaderList = new ArrayList<>();

    public static List<String> getModloaderList() {
        if (modloaderList.isEmpty()) {
            setModloaderList();
        }
        return modloaderList;
    }

    public static String getModloaderName(@NonNull String modloader) {
        switch (modloader) {
            case "forge":
                return "Forge";
            case "fabric":
                return "Fabric";
            case "quilt":
                return "Quilt";
            case "neoforge":
                return "NeoForge";
            default:
                return "none";
        }
    }

    public static int getModloaderId(@NonNull String modloader) {
        switch (modloader) {
            case "forge":
                return 1;
            case "fabric":
                return 2;
            case "quilt":
                return 3;
            case "neoforge":
                return 4;
            case "none":
            default:
                return 0;
        }
    }

    public static String getModloaderNameById(int id) {
        switch (id) {
            case 1:
                return "Forge";
            case 2:
                return "Fabric";
            case 3:
                return "Quilt";
            case 4:
                return "NeoForge";
            default:
                return null;
        }
    }

    public static boolean isModloaderName(String modloader) {
        if (modloader == null || modloader.isEmpty()) return false;

        String name = modloader.toLowerCase();
        switch (name) {
            case "forge":
            case "fabric":
            case "quilt":
            case "neoforge":
                return true;
            case "none":
            default:
                return false;
        }
    }

    private static void setModloaderList() {
        modloaderList.clear();
        modloaderList.add(ResourceManager.getString(R.string.zh_unknown));
        modloaderList.add("forge");
        modloaderList.add("fabric");
        modloaderList.add("quilt");
        modloaderList.add("neoforge");
    }
}
