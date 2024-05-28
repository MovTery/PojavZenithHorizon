package com.movtery.ui.subassembly.downloadmod;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ModVersionGroup {
    private final String versionId;
    private final List<ModVersionItem> modversionList;
    private boolean unfold;

    public ModVersionGroup(String versionId, List<ModVersionItem> modversionList) {
        this.versionId = versionId;
        this.modversionList = modversionList;
    }

    public String getVersionId() {
        return versionId;
    }

    public List<ModVersionItem> getModversionList() {
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

    public static class ModVersionItem {
        private final String[] versionId;
        private final String name;
        private final String title;
        private final String modloaders;
        private final String versionHash;
        private final int download;
        private final String downloadUrl;
        private final List<ModDependencies> modDependencies;

        public ModVersionItem(String[] versionId, String name, String title, String modloaders, List<ModDependencies> modDependencies, String versionHash, int download, String downloadUrl) {
            this.versionId = versionId;
            this.name = name;
            this.title = title;
            this.modloaders = modloaders;
            this.versionHash = versionHash;
            this.download = download;
            this.downloadUrl = downloadUrl;
            this.modDependencies = modDependencies;
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
        public List<ModDependencies> getModDependencies() {
            return modDependencies;
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
