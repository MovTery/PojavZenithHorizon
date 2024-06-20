package com.movtery.pojavzh.feature.mod;

import com.movtery.pojavzh.feature.ResourceManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;

import java.util.ArrayList;
import java.util.List;

public class SearchModPlatform {
    public static final List<String> indexList = new ArrayList<>();

    public static List<String> getIndexList() {
        if (indexList.isEmpty()) {
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_platform_both));
            indexList.add("Modrinth");
            indexList.add("CurseForge");
        }
        return indexList;
    }

    public static int getIndex(SearchFilters.ApiPlatform platform) {
        switch (platform) {
            case MODRINTH:
                return 1;
            case CURSEFORGE:
                return 2;
            case BOTH:
            default:
                return 0;
        }
    }

    public static SearchFilters.ApiPlatform getPlatform(int index) {
        switch (index) {
            case 1:
                return SearchFilters.ApiPlatform.MODRINTH;
            case 2:
                return SearchFilters.ApiPlatform.CURSEFORGE;
            case 0:
            default:
                return SearchFilters.ApiPlatform.BOTH;
        }
    }
}
