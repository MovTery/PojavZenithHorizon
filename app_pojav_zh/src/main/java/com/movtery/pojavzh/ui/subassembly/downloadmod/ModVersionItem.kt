package com.movtery.pojavzh.ui.subassembly.downloadmod;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class ModVersionItem {
    private final String[] versionId;
    private final String name;
    private final String title;
    private final String modloaders;
    private final String versionHash;
    private final int download;
    private final String downloadUrl;
    private final List<ModDependencies> modDependencies;
    private final VersionType.VersionTypeEnum versionType;

    public ModVersionItem(String[] versionId, String name, String title, String modloaders, List<ModDependencies> modDependencies, VersionType.VersionTypeEnum versionType, String versionHash, int download, String downloadUrl) {
        this.versionId = versionId;
        this.name = name;
        this.title = title;
        this.modloaders = modloaders;
        this.versionHash = versionHash;
        this.download = download;
        this.downloadUrl = downloadUrl;
        this.modDependencies = modDependencies;
        this.versionType = versionType;
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

    public VersionType.VersionTypeEnum getVersionType() {
        return versionType;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModVersionItem{" +
                "versionId=" + Arrays.toString(versionId) +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", modloaders='" + modloaders + '\'' +
                ", versionHash='" + versionHash + '\'' +
                ", download=" + download +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", modDependencies=" + modDependencies +
                ", versionType=" + versionType +
                '}';
    }
}
