package net.kdt.pojavlaunch.modloaders.modpacks.models;

import androidx.annotation.NonNull;

public class ModItem extends ModSource {

    public String id;
    public String title;
    public String description;
    public long downloadCount;
    public String modloader;
    public String imageUrl;

    public ModItem(int apiSource, boolean isModpack, String id, String title, String description, long downloadCount, String modloader, String imageUrl) {
        this.apiSource = apiSource;
        this.isModpack = isModpack;
        this.id = id;
        this.title = title;
        this.description = description;
        this.downloadCount = downloadCount;
        this.modloader = modloader;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", downloadCount='" + downloadCount + '\'' +
                ", modloader='" + modloader + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", apiSource=" + apiSource +
                ", isModpack=" + isModpack +
                '}';
    }

    public String getIconCacheTag() {
        return apiSource+"_"+id;
    }
}
