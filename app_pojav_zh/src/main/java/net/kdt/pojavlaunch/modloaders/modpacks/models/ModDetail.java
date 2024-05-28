package net.kdt.pojavlaunch.modloaders.modpacks.models;

import com.movtery.ui.subassembly.downloadmod.ModVersionGroup;

import java.util.List;

public class ModDetail extends ModItem {
    /* SHA 1 hashes, null if a hash is unavailable */
    public List<ModVersionGroup.ModVersionItem> modVersionItems;

    public ModDetail(ModItem item, List<ModVersionGroup.ModVersionItem> modVersionItems) {
        super(item.apiSource, item.isModpack, item.id, item.title, item.description, item.downloadCount, item.modloader, item.imageUrl);
        this.modVersionItems = modVersionItems;
    }
}
