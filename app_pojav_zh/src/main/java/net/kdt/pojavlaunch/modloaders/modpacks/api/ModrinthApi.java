package net.kdt.pojavlaunch.modloaders.modpacks.api;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.feature.mod.SearchModSort;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.feature.mod.modpack.install.OnInstallStartListener;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchResult;
import net.kdt.pojavlaunch.progresskeeper.DownloaderProgressWrapper;
import net.kdt.pojavlaunch.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.zip.ZipFile;

public class ModrinthApi implements ModpackApi{
    private final ApiHandler mApiHandler;
    public ModrinthApi(){
        mApiHandler = new ApiHandler("https://api.modrinth.com/v2");
    }

    @Override
    public SearchResult searchMod(SearchFilters searchFilters, SearchResult previousPageResult) {
        ModrinthSearchResult modrinthSearchResult = (ModrinthSearchResult) previousPageResult;

        // Fixes an issue where the offset being equal or greater than total_hits is ignored
        if (modrinthSearchResult != null && modrinthSearchResult.previousOffset >= modrinthSearchResult.totalResultCount) {
            ModrinthSearchResult emptyResult = new ModrinthSearchResult();
            emptyResult.results = new ModItem[0];
            emptyResult.totalResultCount = modrinthSearchResult.totalResultCount;
            emptyResult.previousOffset = modrinthSearchResult.previousOffset;
            return emptyResult;
        }

        // Build the facets filters
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder facetString = new StringBuilder();
        facetString.append("[");
        facetString.append(String.format("[\"project_type:%s\"]", searchFilters.isModpack ? "modpack" : "mod"));
        if(searchFilters.mcVersion != null && !searchFilters.mcVersion.isEmpty())
            facetString.append(String.format(",[\"versions:%s\"]", searchFilters.mcVersion));
        if (searchFilters.modloaders != null && !searchFilters.modloaders.isEmpty()) {
            StringJoiner categories = new StringJoiner(", ", "[", "]");
            for (String modloader : searchFilters.modloaders) {
                categories.add(String.format("\"categories:%s\"", modloader));
            }
            facetString.append(",").append(categories);
        }
        facetString.append("]");
        params.put("facets", facetString.toString());
        params.put("query", searchFilters.name);
        params.put("limit", 50);
        params.put("index", SearchModSort.getModrinthIndexById(searchFilters.sort));
        if(modrinthSearchResult != null)
            params.put("offset", modrinthSearchResult.previousOffset);

        JsonObject response = mApiHandler.get("search", params, JsonObject.class);
        if(response == null) return null;
        JsonArray responseHits = response.getAsJsonArray("hits");
        if(responseHits == null) return null;

        List<ModItem> modItems = new ArrayList<>();
        for (int i = 0; i < responseHits.size(); ++i) {
            JsonObject hit = responseHits.get(i).getAsJsonObject();

            JsonArray categories = hit.get("categories").getAsJsonArray();
            List<ModLoaderList.ModLoader> modLoaders = new ArrayList<>();
            boolean isDataPack = false;
            for (JsonElement category : categories) {
                String string = category.getAsString();

                if (Objects.equals(string, "datapack")) {
                    isDataPack = true; //老是能搜到数据包我不理解...
                }

                ModLoaderList.addModLoaderToList(modLoaders, string);
            }

            if (isDataPack) {
                continue;
            }

            modItems.add(new ModItem(
                    Constants.SOURCE_MODRINTH,
                    hit.get("project_type").getAsString().equals("modpack"),
                    hit.get("project_id").getAsString(),
                    hit.get("title").getAsString(),
                    hit.get("description").getAsString(),
                    hit.get("downloads").getAsInt(),
                    modLoaders.toArray(new ModLoaderList.ModLoader[]{}),
                    hit.get("icon_url").getAsString()));
        }
        if (modrinthSearchResult == null) modrinthSearchResult = new ModrinthSearchResult();
        modrinthSearchResult.previousOffset += responseHits.size();
        modrinthSearchResult.results = modItems.toArray(new ModItem[0]);
        modrinthSearchResult.totalResultCount = response.get("total_hits").getAsInt();
        return modrinthSearchResult;
    }

    @Override
    public ModDetail getModDetails(ModItem item) {

        JsonArray response = mApiHandler.get(String.format("project/%s/version", item.id), JsonArray.class);
        if (response == null) return null;
        System.out.println(response);

        List<ModVersionItem> modItems = new ArrayList<>();
        Map<String, ModItem> dependenciesModMap = new HashMap<>();

        for (int i = 0; i < response.size(); ++i) {
            JsonObject version = response.get(i).getAsJsonObject();
            JsonObject filesJsonObject = version.get("files").getAsJsonArray().get(0).getAsJsonObject();
            //提取信息
            String downloadUrl = filesJsonObject.get("url").getAsString();
            String filename = filesJsonObject.get("filename").getAsString();
            String name = version.get("name").getAsString();
            String versionTypeString = version.get("version_type").getAsString();
            //Mod加载器信息
            JsonArray loaders = version.get("loaders").getAsJsonArray();
            List<ModLoaderList.ModLoader> modloaderList = new ArrayList<>();
            for (JsonElement loader : loaders) {
                String loaderName = loader.getAsString();
                ModLoaderList.addModLoaderToList(modloaderList, loaderName);
            }

            // Assume there may not be hashes, in case the API changes
            String hash;
            JsonObject hashesMap = version.getAsJsonArray("files").get(0).getAsJsonObject()
                    .get("hashes").getAsJsonObject();
            if (hashesMap == null || hashesMap.get("sha1") == null) {
                hash = null;
            } else {
                hash = hashesMap.get("sha1").getAsString();
            }

            JsonArray gameVersionJson = version.get("game_versions").getAsJsonArray();

            List<String> mcVersions = new ArrayList<>();
            for (JsonElement gameVersions : gameVersionJson) {
                mcVersions.add(gameVersions.getAsString());
            }
            String[] mcVersionsArray = new String[mcVersions.size()];
            mcVersions.toArray(mcVersionsArray);

            JsonArray dependencies = version.get("dependencies").getAsJsonArray();
            List<ModDependencies> modDependencies = new ArrayList<>();
            if (!item.isModpack && dependencies.size() != 0) {
                for (JsonElement dependency : dependencies) {
                    JsonObject object = dependency.getAsJsonObject();
                    String projectId = object.get("project_id").getAsString();
                    String dependencyType = object.get("dependency_type").getAsString();

                    ModItem items = null;
                    if (!dependenciesModMap.containsKey(projectId)) {
                        JsonObject hit = mApiHandler.get("project/" + projectId, JsonObject.class);

                        if (hit != null) {
                            JsonArray modLoaders = hit.get("loaders").getAsJsonArray();
                            List<ModLoaderList.ModLoader> modLoadersList = new ArrayList<>();
                            for (JsonElement loader : modLoaders) {
                                String string = loader.getAsString();
                                ModLoaderList.addModLoaderToList(modLoadersList, string);
                            }
                            items = new ModItem(
                                    Constants.SOURCE_MODRINTH,
                                    hit.get("project_type").getAsString().equals("modpack"),
                                    projectId,
                                    hit.get("title").getAsString(),
                                    hit.get("description").getAsString(),
                                    hit.get("downloads").getAsInt(),
                                    modLoadersList.toArray(new ModLoaderList.ModLoader[]{}),
                                    hit.get("icon_url").getAsString()
                            );
                        }
                        dependenciesModMap.put(projectId, items);
                    } else {
                        items = dependenciesModMap.get(projectId);
                    }

                    if (items != null) {
                        modDependencies.add(new ModDependencies(items, ModDependencies.getDependencyType(dependencyType)));
                    }
                }
            }

            modItems.add(new ModVersionItem(mcVersionsArray,
                    filename,
                    name,
                    modloaderList.toArray(new ModLoaderList.ModLoader[]{}),
                    modDependencies,
                    VersionType.getVersionType(versionTypeString),
                    hash,
                    version.get("downloads").getAsInt(),
                    downloadUrl));
        }

        return new ModDetail(item, modItems);
    }

    @Override
    public ModLoader installMod(boolean isModPack, String modsPath, ModDetail modDetail, ModVersionItem modVersionItem) throws IOException{
        if (isModPack) {
            return ModpackInstaller.installModpack(modDetail, modVersionItem, (modpackFile, instanceDestination) -> installMrpack(modpackFile, instanceDestination, null));
        } else {
            return ModpackInstaller.installMod(modDetail, modsPath, modVersionItem);
        }
    }

    private static ModLoader createInfo(ModrinthIndex modrinthIndex) {
        if(modrinthIndex == null) return null;
        Map<String, String> dependencies = modrinthIndex.dependencies;
        String mcVersion = dependencies.get("minecraft");
        if(mcVersion == null) return null;
        String modLoaderVersion;
        if((modLoaderVersion = dependencies.get("forge")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_FORGE, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("neoforge")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_NEOFORGE, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("fabric-loader")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_FABRIC, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("quilt-loader")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_QUILT, modLoaderVersion, mcVersion);
        }
        return null;
    }

    public ModLoader installMrpack(File mrpackFile, File instanceDestination, OnInstallStartListener onInstallStartListener) throws IOException {
        try (ZipFile modpackZipFile = new ZipFile(mrpackFile)){
            ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(modpackZipFile, "modrinth.index.json")),
                    ModrinthIndex.class);
            if(!ModPackUtils.verifyModrinthIndex(modrinthIndex)) {
                Log.i("ModrinthApi","manifest verification failed");
                return null;
            }
            if (onInstallStartListener != null) onInstallStartListener.onStart();
            ModDownloader modDownloader = new ModDownloader(instanceDestination);
            for(ModrinthIndex.ModrinthIndexFile indexFile : modrinthIndex.files) {
                modDownloader.submitDownload(indexFile.fileSize, indexFile.path, indexFile.hashes.sha1, indexFile.downloads);
            }
            modDownloader.awaitFinish(new DownloaderProgressWrapper(R.string.modpack_download_downloading_mods, ProgressLayout.INSTALL_MODPACK));
            ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.modpack_download_applying_overrides, 1, 2);
            ZipUtils.zipExtract(modpackZipFile, "overrides/", instanceDestination);
            ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 50, R.string.modpack_download_applying_overrides, 2, 2);
            ZipUtils.zipExtract(modpackZipFile, "client-overrides/", instanceDestination);
            return createInfo(modrinthIndex);
        }
    }

    static class ModrinthSearchResult extends SearchResult {
        int previousOffset;
    }
}
