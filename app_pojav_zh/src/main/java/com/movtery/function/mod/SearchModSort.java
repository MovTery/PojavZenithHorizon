package com.movtery.function.mod;

import net.kdt.pojavlaunch.R;
import com.movtery.function.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class SearchModSort {
    public static final int CURSEFORGE_SORT_INDEX_RELEVANT = 1;
    public static final int CURSEFORGE_SORT_INDEX_DOWNLOADS = 6;
    public static final int CURSEFORGE_SORT_INDEX_POPULARITY = 2;
    public static final int CURSEFORGE_SORT_INDEX_RECENTLY_CREATED = 11;
    public static final int CURSEFORGE_SORT_INDEX_RECENTLY_UPDATED = 3;
    public static List<String> indexList = new ArrayList<>();

    public static List<String> getIndexList() {
        if (indexList.isEmpty()) {
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_sort_by_relevant)); //相关 0
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_sort_by_total_downloads)); //总下载 1
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_sort_by_popularity)); //人气 2
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_sort_by_recently_created)); //最近创建 3
            indexList.add(ResourceManager.getString(R.string.zh_profile_mods_search_sort_by_recently_updated)); //最近更新 4
        }
        return indexList;
    }

    public static String getModrinthIndexById(int id) {
        switch (id) {
            case 1:
                return "downloads";
            case 2:
                return "follows";
            case 3:
                return "newest";
            case 4:
                return "updated";
            case 0:
            default:
                return "relevance";
        }
    }

    public static int getCurseforgeIndexById(int id) {
        switch (id) {
            case 1:
                return CURSEFORGE_SORT_INDEX_DOWNLOADS;
            case 2:
                return CURSEFORGE_SORT_INDEX_POPULARITY;
            case 3:
                return CURSEFORGE_SORT_INDEX_RECENTLY_CREATED;
            case 4:
                return CURSEFORGE_SORT_INDEX_RECENTLY_UPDATED;
            case 0:
            default:
                return CURSEFORGE_SORT_INDEX_RELEVANT;
        }
    }
}
