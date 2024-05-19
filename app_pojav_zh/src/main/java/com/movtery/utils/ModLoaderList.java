package com.movtery.utils;

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

    private static void setModloaderList() {
        modloaderList.clear();
        modloaderList.add(ResourceManager.getString(R.string.zh_unknown));
        modloaderList.add("forge");
        modloaderList.add("fabric");
        modloaderList.add("quilt");
        modloaderList.add("neoforge");
    }
}
