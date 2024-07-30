package com.movtery.pojavzh.feature.mod.models;

public class MCBBSPackMeta {
    public String author;
    public String version;
    public String description;
    public String fileApi;

    public MCBBSFile[] files;
    public MCBBSAddons[] addons;
    public MCBBSLaunchInfo launchInfo;
    public String manifestType;
    public int manifestVersion;
    public String name;

    public static class MCBBSFile {
        public String hash;
        public String path;
        public boolean force;
        public String type;
    }

    public static class MCBBSAddons {
        public String id;
        public String version;
    }

    public static class MCBBSLaunchInfo {
        public int minMemory;
        public String[] launchArgument;
        public String[] javaArgument;
    }
}
