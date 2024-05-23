package com.movtery.feature.mod.models;

public class MCBBSPackMeta {
    public String author;
    public String description;
    public String fileApi;

    public MCBBSFile[] files;
    public MCBBSAddons[] addons;
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
}
