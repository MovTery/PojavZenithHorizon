package com.movtery.utils;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.ResourceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ModLoaderList {
    private static final String[] MODLOADER_NAMES = {"Forge", "Fabric", "Quilt", "NeoForge"};
    private static final String[] MODLOADER_NAMES_LOWERCASE = {"forge", "fabric", "quilt", "neoforge"};
    private static final List<String> modloaderList = new ArrayList<>();
    private static final Map<String, String> modloaderNameMap = new HashMap<>();
    private static final Map<String, Integer> modloaderIdMap = new HashMap<>();

    public static List<String> getModloaderList() {
        if (modloaderList.isEmpty()) {
            setModloaderList();
        }
        return modloaderList;
    }

    private static void initNameMap() {
        modloaderNameMap.put(MODLOADER_NAMES_LOWERCASE[0], MODLOADER_NAMES[0]);
        modloaderNameMap.put(MODLOADER_NAMES_LOWERCASE[1], MODLOADER_NAMES[1]);
        modloaderNameMap.put(MODLOADER_NAMES_LOWERCASE[2], MODLOADER_NAMES[2]);
        modloaderNameMap.put(MODLOADER_NAMES_LOWERCASE[3], MODLOADER_NAMES[3]);
    }

    private static void initIdMap() {
        modloaderIdMap.put(MODLOADER_NAMES_LOWERCASE[0], 1);
        modloaderIdMap.put(MODLOADER_NAMES_LOWERCASE[1], 2);
        modloaderIdMap.put(MODLOADER_NAMES_LOWERCASE[2], 3);
        modloaderIdMap.put(MODLOADER_NAMES_LOWERCASE[3], 4);
    }

    public static String getModloaderName(@NonNull String modloader) {
        String string = modloader.toLowerCase();
        if (modloaderNameMap.isEmpty()) initNameMap();
        return modloaderNameMap.getOrDefault(string, "none");
    }

    public static int getModloaderId(@NonNull String modloader) {
        String string = modloader.toLowerCase();
        if (modloaderIdMap.isEmpty()) initIdMap();
        AtomicInteger returnValue = new AtomicInteger();
        Optional.ofNullable(modloaderIdMap.get(string))
                .ifPresent(returnValue::set);
        return returnValue.get();
    }

    public static String getModloaderNameByCurseId(int id) {
        switch (id) {
            case 1:
                return MODLOADER_NAMES[0];
            case 4:
                return MODLOADER_NAMES[1];
            case 5:
                return MODLOADER_NAMES[2];
            case 6:
                return MODLOADER_NAMES[3];
            default:
                return null;
        }
        //0=Any
        //1=Forge
        //2=Cauldron
        //3=LiteLoader
        //4=Fabric
        //5=Quilt
        //6=NeoForge
    }

    public static boolean notModloaderName(String modloader) {
        if (modloader == null || modloader.isEmpty()) return true;

        String name = modloader.toLowerCase();
        switch (name) {
            case "forge":
            case "fabric":
            case "quilt":
            case "neoforge":
                return false;
            case "none":
            default:
                return true;
        }
    }

    private static void setModloaderList() {
        modloaderList.clear();
        modloaderList.add(ResourceManager.getString(R.string.zh_unknown));
        modloaderList.add(MODLOADER_NAMES[0]);
        modloaderList.add(MODLOADER_NAMES[1]);
        modloaderList.add(MODLOADER_NAMES[2]);
        modloaderList.add(MODLOADER_NAMES[3]);
    }
}
