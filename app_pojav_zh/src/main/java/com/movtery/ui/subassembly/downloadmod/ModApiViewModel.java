package com.movtery.ui.subassembly.downloadmod;

import androidx.lifecycle.ViewModel;

import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

public class ModApiViewModel extends ViewModel {
    private ModpackApi modApi;
    private ModItem modItem;
    private boolean isModpack;
    private String modsPath;

    public ModpackApi getModApi() {
        return modApi;
    }

    public void setModApi(ModpackApi modApi) {
        this.modApi = modApi;
    }

    public ModItem getModItem() {
        return modItem;
    }

    public void setModItem(ModItem modItem) {
        this.modItem = modItem;
    }

    public boolean isModpack() {
        return isModpack;
    }

    public void setModpack(boolean modpack) {
        isModpack = modpack;
    }

    public String getModsPath() {
        return modsPath;
    }

    public void setModsPath(String modsPath) {
        this.modsPath = modsPath;
    }
}
