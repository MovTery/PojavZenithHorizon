package com.movtery.ui.subassembly.downloadmod;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ModVersionGroup {
    private final String versionId;
    private final List<ModItem> modversionList;
    private boolean unfold;

    public ModVersionGroup(String versionId, List<ModItem> modversionList) {
        this.versionId = versionId;
        this.modversionList = modversionList;
    }

    public String getVersionId() {
        return versionId;
    }

    public List<ModItem> getModversionList() {
        return modversionList;
    }

    public boolean isUnfold() {
        return unfold;
    }

    public void setUnfold(boolean unfold) {
        this.unfold = unfold;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModVersionGroup{" +
                "versionId='" + versionId + '\'' +
                ", modversionList=" + modversionList +
                '}';
    }

    public static class ModItem {
        private final String[] versionId;
        private final String name;
        private final String title;
        private final String modloaders;
        private final String versionHash;
        private final int download;
        private final String downloadUrl;

        public ModItem(String[] versionId, String name, String title, String modloaders, String versionHash, int download, String downloadUrl) {
            this.versionId = versionId;
            this.name = name;
            this.title = title;
            this.modloaders = modloaders;
            this.versionHash = versionHash;
            this.download = download;
            this.downloadUrl = downloadUrl;
        }

        public String getName() {
            return name;
        }
        public String[] getVersionId() {
            return versionId;
        }
        public String getTitle() {
            return title;
        }
        public String getModloaders() {
            return modloaders;
        }
        public String getVersionHash() {
            return versionHash;
        }
        public int getDownload() {
            return download;
        }
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @NonNull
        @Override
        public String toString() {
            return "ModItem{" +
                    "versionId=" + Arrays.toString(versionId) +
                    ", title='" + title + '\'' +
                    ", modloaders='" + modloaders + '\'' +
                    ", download=" + download +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    '}';
        }
    }
}
