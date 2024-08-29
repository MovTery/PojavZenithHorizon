package net.kdt.pojavlaunch.modloaders.modpacks.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.mod.ModCache;
import com.movtery.pojavzh.feature.mod.ModCategory;
import com.movtery.pojavzh.feature.mod.ModFilters;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.feature.mod.ModMirror;
import com.movtery.pojavzh.feature.mod.SearchModSort;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.feature.mod.modpack.install.OnInstallStartListener;
import com.movtery.pojavzh.feature.mod.translate.ModPackTranslateManager;
import com.movtery.pojavzh.feature.mod.translate.ModTranslateManager;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
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
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.zip.ZipFile;

public class ModrinthApi implements ModpackApi{
    private final ApiHandler mApiHandler;
    public ModrinthApi(){
        mApiHandler = new ApiHandler(ModMirror.replaceMirrorInfoUrl("https://api.modrinth.com/v2"));
    }

    @Override
    public String getWebUrl(ModItem item) {
        return "https://modrinth.com/"+ (item.isModpack ? "modpack" : "mod") + "/" + item.id;
    }

    @Override
    public SearchResult searchMod(ModFilters modFilters, SearchResult previousPageResult) {
        ModCategory.Category categoryName = modFilters.getCategory();
        if (categoryName != ModCategory.Category.ALL && categoryName.getModrinthName() == null) return returnEmptyResult();
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
        facetString.append(String.format("[\"project_type:%s\"]", modFilters.isModpack() ? "modpack" : "mod"));
        if (modFilters.getMcVersion() != null && !modFilters.getMcVersion().isEmpty())
            facetString.append(String.format(",[\"versions:%s\"]", modFilters.getMcVersion()));

        //处理全部"categories"
        List<String> categoriesList = new ArrayList<>();
        if (modFilters.getModloader() != null) {
            categoriesList.add(modFilters.getModloader());
        }
        if (categoryName != ModCategory.Category.ALL) categoriesList.add(categoryName.getModrinthName());
        if (!categoriesList.isEmpty()) {
            StringJoiner categories = new StringJoiner(", ", "[", "]");
            for (String string : categoriesList) {
                categories.add(String.format("\"categories:%s\"", string));
            }
            facetString.append(",").append(categories);
        }

        facetString.append("]");
        params.put("facets", facetString.toString());
        params.put("query", modFilters.getName());
        params.put("limit", 50);
        params.put("index", SearchModSort.getModrinthIndexById(modFilters.getSort()));
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

            String iconUrl = fetchIconUrl(hit);

            String title = hit.get("title").getAsString();
            String subTitle = getSubTitle(title, modFilters.isModpack());

            modItems.add(new ModItem(
                    Constants.SOURCE_MODRINTH,
                    hit.get("project_type").getAsString().equals("modpack"),
                    hit.get("project_id").getAsString(),
                    title,
                    subTitle,
                    hit.get("description").getAsString(),
                    hit.get("downloads").getAsInt(),
                    getAllCategories(hit),
                    modLoaders.toArray(new ModLoaderList.ModLoader[]{}),
                    iconUrl));
        }
        if (modrinthSearchResult == null) modrinthSearchResult = new ModrinthSearchResult();
        modrinthSearchResult.previousOffset += responseHits.size();
        modrinthSearchResult.results = modItems.toArray(new ModItem[0]);
        modrinthSearchResult.totalResultCount = response.get("total_hits").getAsInt();
        return modrinthSearchResult;
    }

    @Override
    public ModDetail getModDetails(ModItem item, boolean force) {
        if (!force && ModCache.ModInfoCache.INSTANCE.containsKey(this, item.id)) return new ModDetail(item, ModCache.ModInfoCache.INSTANCE.get(this, item.id));

        JsonArray response = mApiHandler.get(String.format("project/%s/version", item.id), JsonArray.class);
        if (response == null) return null;

        List<ModVersionItem> modItems = new ArrayList<>();

        for (JsonElement element : response) {
            JsonObject version = element.getAsJsonObject();
            JsonObject filesJsonObject = version.getAsJsonArray("files").get(0).getAsJsonObject();
            //提取信息
            String downloadUrl = ModMirror.replaceMirrorDownloadUrl(filesJsonObject.get("url").getAsString());
            String filename = filesJsonObject.get("filename").getAsString();
            String name = version.get("name").getAsString();
            String versionTypeString = version.get("version_type").getAsString();
            String hash = getSha1Hash(filesJsonObject);
            //Mod加载器信息
            List<ModLoaderList.ModLoader> modloaderList = getModLoaderList(version.getAsJsonArray("loaders"));
            String[] mcVersionsArray = getMcVersions(version.getAsJsonArray("game_versions"));

            List<ModDependencies> modDependencies = getDependencies(item, version);

            modItems.add(new ModVersionItem(mcVersionsArray, filename, name,
                    modloaderList.toArray(new ModLoaderList.ModLoader[0]), modDependencies,
                    VersionType.getVersionType(versionTypeString), hash,
                    version.get("downloads").getAsInt(), downloadUrl));
        }

        ModCache.ModInfoCache.INSTANCE.put(this, item.id, modItems);
        return new ModDetail(item, modItems);
    }

    private String getSha1Hash(JsonObject filesJsonObject) {
        JsonObject hashesMap = filesJsonObject.getAsJsonObject("hashes");
        return (hashesMap != null && hashesMap.has("sha1")) ? hashesMap.get("sha1").getAsString() : null;
    }

    private List<ModLoaderList.ModLoader> getModLoaderList(JsonArray loaders) {
        List<ModLoaderList.ModLoader> modloaderList = new ArrayList<>();
        for (JsonElement loader : loaders) {
            ModLoaderList.addModLoaderToList(modloaderList, loader.getAsString());
        }
        return modloaderList;
    }

    private String[] getMcVersions(JsonArray gameVersionJson) {
        List<String> mcVersions = new ArrayList<>();
        for (JsonElement gameVersion : gameVersionJson) {
            mcVersions.add(gameVersion.getAsString());
        }
        return mcVersions.toArray(new String[0]);
    }

    private List<ModDependencies> getDependencies(ModItem item, JsonObject version) {
        JsonArray dependencies = version.get("dependencies").getAsJsonArray();
        List<ModDependencies> modDependencies = new ArrayList<>();
        if (!item.isModpack && dependencies.size() != 0) {
            for (JsonElement dependency : dependencies) {
                JsonObject object = dependency.getAsJsonObject();
                String projectId = object.get("project_id").getAsString();
                String dependencyType = object.get("dependency_type").getAsString();

                if (!ModCache.ModItemCache.INSTANCE.containsKey(this, projectId)) {
                    JsonObject hit = searchModFromID(projectId);

                    if (hit != null) {
                        JsonArray modLoaders = hit.get("loaders").getAsJsonArray();
                        List<ModLoaderList.ModLoader> modLoadersList = new ArrayList<>();
                        for (JsonElement loader : modLoaders) {
                            String string = loader.getAsString();
                            ModLoaderList.addModLoaderToList(modLoadersList, string);
                        }

                        String iconUrl = fetchIconUrl(hit);

                        String title = hit.get("title").getAsString();
                        String subTitle = getSubTitle(title, item.isModpack);

                        ModCache.ModItemCache.INSTANCE.put(this, projectId, new ModItem(
                                Constants.SOURCE_MODRINTH,
                                hit.get("project_type").getAsString().equals("modpack"),
                                projectId,
                                title,
                                subTitle,
                                hit.get("description").getAsString(),
                                hit.get("downloads").getAsInt(),
                                getAllCategories(hit),
                                modLoadersList.toArray(new ModLoaderList.ModLoader[]{}),
                                iconUrl
                        ));
                    }
                }

                ModItem cacheMod = ModCache.ModItemCache.INSTANCE.get(this, projectId);
                if (cacheMod != null) modDependencies.add(new ModDependencies(cacheMod, ModDependencies.getDependencyType(dependencyType)));
            }
        }

        return modDependencies;
    }

    private JsonObject searchModFromID(String id) {
        JsonObject jsonObject = mApiHandler.get("project/" + id, JsonObject.class);
        System.out.println(jsonObject);

        return jsonObject;
    }

    private String fetchIconUrl(JsonObject hit) {
        try {
            return hit.get("icon_url").getAsString();
        } catch (Exception e) {
            Logging.e("ModrinthAPI", Tools.printToString(e));
            return null;
        }
    }

    private String getSubTitle(String title, boolean isModPack) {
        String subTitle = null;
        if (ZHTools.areaChecks("zh")) {
            subTitle = isModPack ?
                    ModPackTranslateManager.INSTANCE.searchToChinese(title) :
                    ModTranslateManager.INSTANCE.searchToChinese(title);
        }
        return subTitle;
    }

    private Set<ModCategory.Category> getAllCategories(JsonObject hit) {
        Set<ModCategory.Category> list = new TreeSet<>();
        for (JsonElement categories : hit.get("categories").getAsJsonArray()) {
            String name = categories.getAsString();
            ModCategory.Category category = ModCategory.getCategoryFromModrinthName(name);
            if (category != null) list.add(category);
        }
        return list;
    }

    private SearchResult returnEmptyResult() {
        ModrinthSearchResult searchResult = new ModrinthSearchResult();
        searchResult.results = new ModItem[0];
        searchResult.totalResultCount = 0;
        return searchResult;
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
            Logging.i("ModLoader", "Forge");
            return new ModLoader(ModLoader.MOD_LOADER_FORGE, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("neoforge")) != null) {
            Logging.i("ModLoader", "NeoForge");
            return new ModLoader(ModLoader.MOD_LOADER_NEOFORGE, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("fabric-loader")) != null) {
            Logging.i("ModLoader", "Fabric");
            return new ModLoader(ModLoader.MOD_LOADER_FABRIC, modLoaderVersion, mcVersion);
        }
        if((modLoaderVersion = dependencies.get("quilt-loader")) != null) {
            Logging.i("ModLoader", "Quilt");
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
                Logging.i("ModrinthApi","manifest verification failed");
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
