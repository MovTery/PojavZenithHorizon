package net.kdt.pojavlaunch.modloaders.modpacks.models;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.feature.mod.ModCategory;
import com.movtery.pojavzh.feature.mod.ModLoaderList;

import java.util.Arrays;
import java.util.Set;

public class ModItem extends ModSource {

    public String id;
    public String title;
    public String subTitle;
    public String description;
    public long downloadCount;
    public Set<ModCategory.Category> categories;
    public ModLoaderList.ModLoader[] modloaders;
    public String imageUrl;

    public ModItem(int apiSource,
                   boolean isModpack,
                   String id,
                   String title,
                   String subTitle,
                   String description,
                   long downloadCount,
                   Set<ModCategory.Category> categories,
                   ModLoaderList.ModLoader[] modloaders,
                   String imageUrl) {
        this.apiSource = apiSource;
        this.isModpack = isModpack;
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.downloadCount = downloadCount;
        this.categories = categories;
        this.modloaders = modloaders;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
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
