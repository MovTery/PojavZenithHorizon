package net.kdt.pojavlaunch.modloaders.modpacks.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.movtery.pojavzh.utils.MCVersionRegex;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchResult;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.GsonJsonUtils;
import net.kdt.pojavlaunch.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class CurseforgeApi implements ModpackApi{
    private static final int ALGO_SHA_1 = 1;
    // Stolen from
    // https://github.com/AnzhiZhang/CurseForgeModpackDownloader/blob/6cb3f428459f0cc8f444d16e54aea4cd1186fd7b/utils/requester.py#L93
    private static final int CURSEFORGE_MINECRAFT_GAME_ID = 432;
    private static final int CURSEFORGE_MODPACK_CLASS_ID = 4471;
    // https://api.curseforge.com/v1/categories?gameId=432 and search for "Mods" (case-sensitive)
    private static final int CURSEFORGE_MOD_CLASS_ID = 6;
    private static final int CURSEFORGE_PAGINATION_SIZE = 50;
    private static final int CURSEFORGE_PAGINATION_END_REACHED = -1;

    private final ApiHandler mApiHandler;
    public CurseforgeApi(String apiKey) {
        mApiHandler = new ApiHandler(ModMirror.replaceMirrorInfoUrl("https://api.curseforge.com/v1"), apiKey);
    }

    @Override
    public String getWebUrl(ModItem item) {
        JsonObject response = searchModFromID(item.id);
        JsonObject hit = GsonJsonUtils.getJsonObjectSafe(response, "data");
        if (hit != null) {
            JsonObject links = hit.getAsJsonObject("links");
            return links.get("websiteUrl").getAsString();
        }
        return null;
    }

    @Override
    public SearchResult searchMod(ModFilters modFilters, SearchResult previousPageResult) {
        ModCategory.Category category = modFilters.getCategory();
        if (category != ModCategory.Category.ALL && category.getCurseforgeID() == null) return returnEmptyResult();
        CurseforgeSearchResult curseforgeSearchResult = (CurseforgeSearchResult) previousPageResult;

        HashMap<String, Object> params = new HashMap<>();
        params.put("gameId", CURSEFORGE_MINECRAFT_GAME_ID);
        params.put("classId", modFilters.isModpack() ? CURSEFORGE_MODPACK_CLASS_ID : CURSEFORGE_MOD_CLASS_ID);
        params.put("searchFilter", modFilters.getName());
        params.put("sortField", SearchModSort.getCurseforgeIndexById(modFilters.getSort()));
        params.put("sortOrder", "desc");
        if (category != ModCategory.Category.ALL) params.put("categoryId", category.getCurseforgeID());
        if (modFilters.getMcVersion() != null && !modFilters.getMcVersion().isEmpty())
            params.put("gameVersion", modFilters.getMcVersion());
        ModLoaderList.ModLoader modLoader = ModLoaderList.getModLoader(modFilters.getModloader());
        if (modLoader != null) {
            params.put("modLoaderTypes", String.format("[%s]", modLoader.getId()));
        }
        if(previousPageResult != null)
            params.put("index", curseforgeSearchResult.previousOffset);

        JsonObject response = mApiHandler.get("mods/search", params, JsonObject.class);
        if(response == null) return null;
        JsonArray dataArray = response.getAsJsonArray("data");
        if(dataArray == null) return null;

        JsonObject paginationInfo = response.getAsJsonObject("pagination");
        ArrayList<ModItem> modItemList = new ArrayList<>(dataArray.size());
        for(int i = 0; i < dataArray.size(); i++) {
            JsonObject dataElement = dataArray.get(i).getAsJsonObject();
            JsonElement allowModDistribution = dataElement.get("allowModDistribution");
            // Gson automatically casts null to false, which leans to issues
            // So, only check the distribution flag if it is non-null
            if(!allowModDistribution.isJsonNull() && !allowModDistribution.getAsBoolean()) {
                Logging.i("CurseforgeApi", "Skipping project "+dataElement.get("name").getAsString() + " because curseforge sucks");
                continue;
            }

            String iconUrl = fetchIconUrl(dataElement);

            String title = dataElement.get("name").getAsString();
            String subTitle = getSubTitle(title, modFilters.isModpack());

            ModItem modItem = new ModItem(Constants.SOURCE_CURSEFORGE,
                    modFilters.isModpack(),
                    dataElement.get("id").getAsString(),
                    title,
                    subTitle,
                    dataElement.get("summary").getAsString(),
                    dataElement.get("downloadCount").getAsInt(),
                    getAllCategories(dataElement),
                    getModloaders(dataElement.getAsJsonArray("latestFilesIndexes")),
                    iconUrl);
            modItemList.add(modItem);
        }
        if(curseforgeSearchResult == null) curseforgeSearchResult = new CurseforgeSearchResult();
        curseforgeSearchResult.results = modItemList.toArray(new ModItem[0]);
        curseforgeSearchResult.totalResultCount = paginationInfo.get("totalCount").getAsInt();
        curseforgeSearchResult.previousOffset += dataArray.size();
        return curseforgeSearchResult;
    }

    @NonNull
    private static ModLoaderList.ModLoader[] getModloaders(JsonArray latestFilesIndexes) {
        //获取Mod加载器信息
        Set<Integer> modloaderSet = new TreeSet<>();
        latestFilesIndexes.getAsJsonArray();
        for (JsonElement latestFilesElement : latestFilesIndexes) {
            JsonObject latestFilesObject = latestFilesElement.getAsJsonObject();
            if (latestFilesObject.get("modLoader") == null) continue;
            modloaderSet.add(latestFilesObject.get("modLoader").getAsInt());
        }
        List<ModLoaderList.ModLoader> modLoaders = new ArrayList<>();
        for (Integer index : modloaderSet) {
            String modloaderName = ModLoaderList.getModloaderNameByCurseId(index);
            ModLoaderList.addModLoaderToList(modLoaders, modloaderName);
        }
        return modLoaders.toArray(new ModLoaderList.ModLoader[]{});
    }

    @Override
    public ModDetail getModDetails(ModItem item, boolean force) {
        if (!force && ModCache.ModInfoCache.INSTANCE.containsKey(this, item.id)) return new ModDetail(item, ModCache.ModInfoCache.INSTANCE.get(this, item.id));

        List<JsonObject> allModDetails;
        try {
            allModDetails = getPaginatedDetails(item.id);
        } catch (IOException e) {
            Logging.e("CurseForgeAPI", Tools.printToString(e));
            return null;
        }

        List<ModVersionItem> modVersionItems = new ArrayList<>();

        for (JsonObject modDetail : allModDetails) {
            //获取信息
            String downloadUrl = ModMirror.replaceMirrorDownloadUrl(modDetail.get("downloadUrl").getAsString());
            String fileName = modDetail.get("fileName").getAsString();
            String displayName = modDetail.get("displayName").getAsString();
            String releaseTypeString = modDetail.get("releaseType").getAsString();
            //获取版本信息
            Set<String> mcVersions = new TreeSet<>();
            for (JsonElement gameVersionElement : modDetail.getAsJsonArray("gameVersions")) {
                String gameVersion = gameVersionElement.getAsString();
                mcVersions.add(gameVersion);
            }

            //获取全部的Mod加载器
            List<ModLoaderList.ModLoader> modloaderList = new ArrayList<>();
            mcVersions.forEach(modloaderName -> ModLoaderList.addModLoaderToList(modloaderList, modloaderName));

            //过滤非MC版本的元素
            Pattern releaseRegex = MCVersionRegex.getRELEASE_REGEX();
            Set<String> nonMCVersion = new TreeSet<>();
            mcVersions.forEach(string -> {
                if (!releaseRegex.matcher(string).find()) nonMCVersion.add(string);
            });
            if (!nonMCVersion.isEmpty()) mcVersions.removeAll(nonMCVersion);

            List<ModDependencies> modDependencies = getDependencies(item, modDetail);

            modVersionItems.add(new ModVersionItem(
                    mcVersions.toArray(new String[0]),
                    fileName,
                    displayName,
                    modloaderList.toArray(new ModLoaderList.ModLoader[0]),
                    modDependencies,
                    VersionType.getVersionType(releaseTypeString),
                    getSha1FromModData(modDetail),
                    modDetail.get("downloadCount").getAsInt(),
                    downloadUrl
            ));
        }

        ModCache.ModInfoCache.INSTANCE.put(this, item.id, modVersionItems);
        return new ModDetail(item, modVersionItems);
    }

    private List<ModDependencies> getDependencies(ModItem item, JsonObject modDetail) {
        JsonArray dependencies = modDetail.get("dependencies").getAsJsonArray();
        List<ModDependencies> modDependencies = new ArrayList<>();
        if (!item.isModpack && dependencies.size() != 0) {
            for (JsonElement dependency : dependencies) {
                JsonObject object = dependency.getAsJsonObject();
                String modId = object.get("modId").getAsString();
                String dependencyType = object.get("relationType").getAsString();

                if (!ModCache.ModItemCache.INSTANCE.containsKey(this, modId)) {
                    JsonObject response = searchModFromID(modId);
                    JsonObject hit = GsonJsonUtils.getJsonObjectSafe(response, "data");

                    if (hit != null) {
                        JsonArray itemsGameVersions = modDetail.getAsJsonArray("gameVersions");
                        Set<ModLoaderList.ModLoader> itemsModloaderNames = new TreeSet<>();
                        for(JsonElement jsonElement : itemsGameVersions) {
                            String gameVersion = jsonElement.getAsString();

                            ModLoaderList.addModLoaderToList(itemsModloaderNames, gameVersion);
                        }

                        String iconUrl = fetchIconUrl(hit);

                        String title = hit.get("name").getAsString();
                        String subTitle = getSubTitle(title, item.isModpack);

                        ModCache.ModItemCache.INSTANCE.put(this, modId, new ModItem(
                                Constants.SOURCE_CURSEFORGE,
                                hit.get("categories").getAsJsonArray().get(0).getAsJsonObject().get("classId").getAsInt() != CURSEFORGE_MOD_CLASS_ID,
                                modId,
                                title,
                                subTitle,
                                hit.get("summary").getAsString(),
                                hit.get("downloadCount").getAsInt(),
                                getAllCategories(hit),
                                itemsModloaderNames.toArray(new ModLoaderList.ModLoader[]{}),
                                iconUrl
                        ));
                    }
                }

                ModItem cacheMod = ModCache.ModItemCache.INSTANCE.get(this, modId);
                if (cacheMod != null) modDependencies.add(new ModDependencies(cacheMod, ModDependencies.getDependencyType(dependencyType)));
            }
        }
        return modDependencies;
    }

    private String fetchIconUrl(JsonObject hit) {
        try {
            return hit.getAsJsonObject("logo").get("thumbnailUrl").getAsString();
        } catch (Exception e) {
            Logging.e("CurseForgeAPI", Tools.printToString(e));
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
            String id = categories.getAsJsonObject().get("id").getAsString();
            ModCategory.Category category = ModCategory.getCategoryFromCurseForgeId(id);
            if (category != null) list.add(category);
        }
        return list;
    }

    @Override
    public ModLoader installMod(boolean isModPack, String modsPath, ModDetail modDetail, ModVersionItem modVersionItem) throws IOException{
        if (isModPack) {
            return ModpackInstaller.installModpack(modDetail, modVersionItem, (modpackFile, instanceDestination) -> installCurseforgeZip(modpackFile, instanceDestination, null));
        } else {
            return ModpackInstaller.installMod(modDetail, modsPath, modVersionItem);
        }
    }

    private JsonObject searchModFromID(String id) {
        JsonObject response = mApiHandler.get(String.format("mods/%s", id), JsonObject.class);
        System.out.println(response);

        return response;
    }

    private List<JsonObject> getPaginatedDetails(String modId) throws IOException {
        List<JsonObject> dataList = new ArrayList<>();
        int index = 0;
        boolean isMirrored = false;
        while (index != CURSEFORGE_PAGINATION_END_REACHED && !isMirrored) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("index", index);
            params.put("pageSize", CURSEFORGE_PAGINATION_SIZE);

            JsonObject response = mApiHandler.get("mods/" + modId + "/files", params, JsonObject.class);
            JsonArray data = GsonJsonUtils.getJsonArraySafe(response, "data");
            if (data == null) {
                throw new IOException("Invalid data!");
            }

            for (int i = 0; i < data.size(); i++) {
                JsonObject fileInfo = data.get(i).getAsJsonObject();
                if (fileInfo.get("isServerPack").getAsBoolean()) continue;
                dataList.add(fileInfo);
            }
            if (data.size() < CURSEFORGE_PAGINATION_SIZE) {
                index = CURSEFORGE_PAGINATION_END_REACHED; // we read the remainder! yay!
                continue;
            }
            index += CURSEFORGE_PAGINATION_SIZE;
            isMirrored = ModMirror.isInfoMirrored();
        }

        return dataList;
    }

    private SearchResult returnEmptyResult() {
        CurseforgeSearchResult searchResult = new CurseforgeSearchResult();
        searchResult.results = new ModItem[0];
        searchResult.totalResultCount = 0;
        return searchResult;
    }

    public ModLoader installCurseforgeZip(File zipFile, File instanceDestination, OnInstallStartListener onInstallStartListener) throws IOException {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)) {
            CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(modpackZipFile, "manifest.json")),
                    CurseManifest.class);
            if (!verifyManifest(curseManifest)) {
                Logging.i("CurseforgeApi", "manifest verification failed");
                return null;
            }
            if (onInstallStartListener != null) onInstallStartListener.onStart();
            ModDownloader modDownloader = getModDownloader(instanceDestination, curseManifest);
            modDownloader.awaitFinish((c, m) ->
                    ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, (int) Math.max((float) c / m * 100, 0), R.string.modpack_download_downloading_mods_fc, c, m)
            );
            String overridesDir = "overrides";
            if (curseManifest.overrides != null) overridesDir = curseManifest.overrides;
            ZipUtils.zipExtract(modpackZipFile, overridesDir, instanceDestination);
            return createInfo(curseManifest.minecraft);
        }
    }

    @NonNull
    private ModDownloader getModDownloader(File instanceDestination, CurseManifest curseManifest) {
        ModDownloader modDownloader = new ModDownloader(new File(instanceDestination, "mods"), true);
        int fileCount = curseManifest.files.length;
        for (int i = 0; i < fileCount; i++) {
            final CurseManifest.CurseFile curseFile = curseManifest.files[i];
            modDownloader.submitDownload(() -> {
                String url = getDownloadUrl(curseFile.projectID, curseFile.fileID);
                if (url == null && curseFile.required)
                    throw new IOException("Failed to obtain download URL for " + StringUtils.insertSpace(curseFile.projectID, curseFile.fileID));
                else if (url == null) return null;
                return new ModDownloader.FileInfo(url, FileUtils.getFileName(url), getDownloadSha1(curseFile.projectID, curseFile.fileID));
            });
        }
        return modDownloader;
    }

    private ModLoader createInfo(CurseManifest.CurseMinecraft minecraft) {
        CurseManifest.CurseModLoader primaryModLoader = null;
        for(CurseManifest.CurseModLoader modLoader : minecraft.modLoaders) {
            if(modLoader.primary) {
                primaryModLoader = modLoader;
                break;
            }
        }
        if(primaryModLoader == null) primaryModLoader = minecraft.modLoaders[0];
        String modLoaderId = primaryModLoader.id;
        int dashIndex = modLoaderId.indexOf('-');
        String modLoaderName = modLoaderId.substring(0, dashIndex);
        String modLoaderVersion = modLoaderId.substring(dashIndex+1);
        Logging.i("CurseforgeApi", StringUtils.insertSpace(modLoaderId, modLoaderName, modLoaderVersion));
        int modLoaderTypeInt;
        switch (modLoaderName) {
            case "forge":
                Logging.i("ModLoader", "Forge, or Quilt? ...");
                modLoaderTypeInt = ModLoader.MOD_LOADER_FORGE;
                break;
            case "neoforge":
                Logging.i("ModLoader", "NeoForge");
                modLoaderTypeInt = ModLoader.MOD_LOADER_NEOFORGE;
                break;
            case "fabric":
                Logging.i("ModLoader", "Fabric");
                modLoaderTypeInt = ModLoader.MOD_LOADER_FABRIC;
                break;
            default:
                return null;
            //TODO: Quilt is also Forge? How does that work?
        }
        return new ModLoader(modLoaderTypeInt, modLoaderVersion, minecraft.version);
    }

    private String getDownloadUrl(long projectID, long fileID) {
        // First try the official api endpoint
        JsonObject response = mApiHandler.get("mods/"+projectID+"/files/"+fileID+"/download-url", JsonObject.class);
        if (response != null && !response.get("data").isJsonNull())
            return response.get("data").getAsString();

        // Otherwise, fallback to building an edge link
        JsonObject fallbackResponse = mApiHandler.get(String.format("mods/%s/files/%s", projectID, fileID), JsonObject.class);
        if (fallbackResponse != null && !fallbackResponse.get("data").isJsonNull()){
            JsonObject modData = fallbackResponse.get("data").getAsJsonObject();
            int id = modData.get("id").getAsInt();
            return String.format("https://edge.forgecdn.net/files/%s/%s/%s", id/1000, id % 1000, modData.get("fileName").getAsString());
        }

        return null;
    }

    private @Nullable String getDownloadSha1(long projectID, long fileID) {
        // Try the api endpoint, die in the other case
        JsonObject response = mApiHandler.get("mods/"+projectID+"/files/"+fileID, JsonObject.class);
        JsonObject data = GsonJsonUtils.getJsonObjectSafe(response, "data");
        if(data == null) return null;
        return getSha1FromModData(data);
    }

    private String getSha1FromModData(@NonNull JsonObject object) {
        JsonArray hashes = GsonJsonUtils.getJsonArraySafe(object, "hashes");
        if(hashes == null) return null;
        for (JsonElement jsonElement : hashes) {
            // The sha1 = 1; md5 = 2;
            JsonObject jsonObject = GsonJsonUtils.getJsonObjectSafe(jsonElement);
            if(GsonJsonUtils.getIntSafe(
                    jsonObject,
                    "algo",
                    -1) == ALGO_SHA_1) {
                return GsonJsonUtils.getStringSafe(jsonObject, "value");
            }
        }
        return null;
    }

    private boolean verifyManifest(CurseManifest manifest) {
        return ModPackUtils.verifyManifest(manifest);
    }

    static class CurseforgeSearchResult extends SearchResult {
        int previousOffset;
    }
}
