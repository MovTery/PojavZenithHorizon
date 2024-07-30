package com.movtery.pojavzh.ui.subassembly.downloadmod;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

public class ModDependencies implements Comparable<ModDependencies> {
    public ModItem item;
    public DependencyType dependencyType;

    public ModDependencies(ModItem item, DependencyType dependencyType) {
        this.item = item;
        this.dependencyType = dependencyType;
    }

    public static DependencyType getDependencyType(String type) {
        switch (type) {                               //1 = EmbeddedLibrary
            case "optional":                          //2 = OptionalDependency
            case "2":                                 //3 = RequiredDependency
                return DependencyType.OPTIONAL;       //4 = Tool
            case "incompatible":                      //5 = Incompatible
            case "5":                                 //6 = Include
                return DependencyType.INCOMPATIBLE;
            case "embedded":
            case "1":
                return DependencyType.EMBEDDED;
            case "4":
                return DependencyType.TOOL;
            case "6":
                return DependencyType.INCLUDE;

            default:
            case "required":
            case "3":
                return DependencyType.REQUIRED;
        }
    }

    public static String getTextFromType(Context context, DependencyType type) {
        switch (type) {
            case OPTIONAL:
                return context.getString(R.string.zh_profile_mods_dependencies_optional);
            case INCOMPATIBLE:
                return context.getString(R.string.zh_profile_mods_dependencies_incompatible);
            case EMBEDDED:
                return context.getString(R.string.zh_profile_mods_dependencies_embedded);
            case TOOL:
                return context.getString(R.string.zh_profile_mods_dependencies_tool);
            case INCLUDE:
                return context.getString(R.string.zh_profile_mods_dependencies_include);
            default:
            case REQUIRED:
                return context.getString(R.string.zh_profile_mods_dependencies_required);
        }
    }

    @Override
    public int compareTo(ModDependencies o) {
        return this.dependencyType.compareTo(o.dependencyType);
    }

    @NonNull
    @Override
    public String toString() {
        return "ModDependencies{" +
                "item=" + item.title +
                ", dependencyType=" + dependencyType +
                '}';
    }

    public enum DependencyType {
        REQUIRED, OPTIONAL, INCOMPATIBLE, EMBEDDED, //相同
        TOOL, INCLUDE //仅curseforge
    }

    public static class SelectedMod {
        public Fragment fragment;
        public String modName;
        public ModpackApi api;
        public boolean isModpack;
        public String modsPath;

        public SelectedMod(Fragment fragment, String modName, ModpackApi api, boolean isModpack, String modsPath) {
            this.fragment = fragment;
            this.modName = modName;
            this.api = api;
            this.isModpack = isModpack;
            this.modsPath = modsPath;
        }
    }
}
