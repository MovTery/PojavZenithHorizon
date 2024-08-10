package net.kdt.pojavlaunch.modloaders.modpacks.models;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.mod.ModLoaderList;

import java.util.Arrays;

public class ModItem extends ModSource {

    public String id;
    public String title;
    public String description;
    public long downloadCount;
    public ModLoaderList.ModLoader[] modloaders;
    public String imageUrl;

    public ModItem(int apiSource, boolean isModpack, String id, String title, String description, long downloadCount, ModLoaderList.ModLoader[] modloaders, String imageUrl) {
        this.apiSource = apiSource;
        this.isModpack = isModpack;
        this.id = id;
        this.title = title;
        this.description = description;
        this.downloadCount = downloadCount;
        this.modloaders = modloaders;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", downloadCount=" + downloadCount +
                ", modloaders=" + Arrays.toString(modloaders) +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String getIconCacheTag() {
        return apiSource+"_"+id;
    }
}
