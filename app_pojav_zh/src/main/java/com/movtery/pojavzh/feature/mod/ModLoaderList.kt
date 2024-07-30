package com.movtery.pojavzh.feature.mod;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ModLoaderList {
    private enum ModLoader {
        FORGE(1, "Forge"),
        FABRIC(4, "Fabric"),
        QUILT(5, "Quilt"),
        NEO_FORGE(6, "NeoForge");

        private final int id;
        private final String name;

        ModLoader(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    // 不可变的 modloaderList
    private static final List<String> modloaderList = Collections.unmodifiableList(Arrays.asList(
            ModLoader.FORGE.getName(),
            ModLoader.FABRIC.getName(),
            ModLoader.QUILT.getName(),
            ModLoader.NEO_FORGE.getName()));

    private static final Map<String, String> modloaderNameMap;
    static {
        Map<String, String> tempMap = new HashMap<>();
        for (ModLoader modLoader : ModLoader.values()) {
            tempMap.put(modLoader.getName().toLowerCase(), modLoader.getName());
        }
        modloaderNameMap = Collections.unmodifiableMap(tempMap);
    }

    private static final Map<Integer, String> modloaderIdMap;
    static {
        Map<Integer, String> tempMap = new HashMap<>();
        for (ModLoader modLoader : ModLoader.values()) {
            tempMap.put(modLoader.getId(), modLoader.getName());
        }
        modloaderIdMap = Collections.unmodifiableMap(tempMap);
    }

    public static List<String> getModloaderList() {
        return modloaderList;
    }

    public static String getModloaderName(@NonNull String modloader) {
        return modloaderNameMap.getOrDefault(modloader.toLowerCase(), "none");
    }

    public static String getModloaderNameByCurseId(int id) {
        return modloaderIdMap.getOrDefault(id, null);
    }

    public static boolean notModloaderName(String modloader) {
        return modloader == null || modloader.isEmpty() || !modloaderNameMap.containsKey(modloader.toLowerCase());
    }
}
