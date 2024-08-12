package net.kdt.pojavlaunch.modloaders.modpacks.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.feature.mod.SearchModSort;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.feature.mod.modpack.install.OnInstallStartListener;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;
import com.movtery.pojavzh.ui.subassembly.downloadmod.VersionType;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;
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
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class CurseforgeApi implements ModpackApi{
    private static final Pattern sMcVersionPattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.?([0-9]+)?");
    private static final int ALGO_SHA_1 = 1;
    // Stolen from
    // https://github.com/AnzhiZhang/CurseForgeModpackDownloader/blob/6cb3f428459f0cc8f444d16e54aea4cd1186fd7b/utils/requester.py#L93
    private static final int CURSEFORGE_MINECRAFT_GAME_ID = 432;
    private static final int CURSEFORGE_MODPACK_CLASS_ID = 4471;
    // https://api.curseforge.com/v1/categories?gameId=432 and search for "Mods" (case-sensitive)
    private static final int CURSEFORGE_MOD_CLASS_ID = 6;
    private static final int CURSEFORGE_PAGINATION_SIZE = 100;
    private static final int CURSEFORGE_PAGINATION_END_REACHED = -1;
    private static final int CURSEFORGE_PAGINATION_ERROR = -2;

    private final ApiHandler mApiHandler;
    public CurseforgeApi(String apiKey) {
        mApiHandler = new ApiHandler("https://api.curseforge.com/v1", apiKey);
    }

    @Override
    public SearchResult searchMod(SearchFilters searchFilters, SearchResult previousPageResult) {
        CurseforgeSearchResult curseforgeSearchResult = (CurseforgeSearchResult) previousPageResult;

        HashMap<String, Object> params = new HashMap<>();
        params.put("gameId", CURSEFORGE_MINECRAFT_GAME_ID);
        params.put("classId", searchFilters.isModpack ? CURSEFORGE_MODPACK_CLASS_ID : CURSEFORGE_MOD_CLASS_ID);
        params.put("searchFilter", searchFilters.name);
        params.put("sortField", SearchModSort.getCurseforgeIndexById(searchFilters.sort));
        params.put("sortOrder", "desc");
        if(searchFilters.mcVersion != null && !searchFilters.mcVersion.isEmpty())
            params.put("gameVersion", searchFilters.mcVersion);
        if (searchFilters.modloaders != null && !searchFilters.modloaders.isEmpty()) {
            if (searchFilters.modloaders.size() > 1) {
                StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
                for (String modloader : searchFilters.modloaders) {
                    stringJoiner.add(ModLoaderList.getModloaderName(modloader));
                }
                params.put("modLoaderTypes", stringJoiner.toString());
            } else {
                params.put("modLoaderType", ModLoaderList.getModloaderName(searchFilters.modloaders.get(0)));
            }
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
                Logging.i("CurseforgeApi", "Skipping modpack "+dataElement.get("name").getAsString() + " because curseforge sucks");
                continue;
            }

            String iconUrl;
            try {
                iconUrl = dataElement.getAsJsonObject("logo").get("thumbnailUrl").getAsString();
            } catch (Exception e) {
                Logging.e("error", Tools.printToString(e));
                iconUrl = null;
            }

            ModItem modItem = new ModItem(Constants.SOURCE_CURSEFORGE,
                    searchFilters.isModpack,
                    dataElement.get("id").getAsString(),
                    dataElement.get("name").getAsString(),
                    dataElement.get("summary").getAsString(),
                    dataElement.get("downloadCount").getAsInt(),
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
    public ModDetail getModDetails(ModItem item) {
        ArrayList<JsonObject> allModDetails = new ArrayList<>();
        int index = 0;
        while(index != CURSEFORGE_PAGINATION_END_REACHED &&
                index != CURSEFORGE_PAGINATION_ERROR) {
            index = getPaginatedDetails(allModDetails, index, item.id);
        }
        if(index == CURSEFORGE_PAGINATION_ERROR) return null;

        List<ModVersionItem> modVersionItems = new ArrayList<>();
        Map<String, ModItem> dependenciesModMap = new HashMap<>();

        for(int i = 0; i < allModDetails.size(); i++) {
            JsonObject modDetail = allModDetails.get(i);
            //获取信息
            String downloadUrl = modDetail.get("downloadUrl").getAsString();
            String fileName = modDetail.get("fileName").getAsString();
            String displayName = modDetail.get("displayName").getAsString();
            String releaseTypeString = modDetail.get("releaseType").getAsString();
            //获取版本信息
            List<String> mcVersions = new ArrayList<>();
            JsonArray gameVersions = modDetail.getAsJsonArray("gameVersions");
            Set<String> modloaderNames = new TreeSet<>();
            for(JsonElement jsonElement : gameVersions) {
                String gameVersion = jsonElement.getAsString();
                if(!sMcVersionPattern.matcher(gameVersion).matches()) {
                    modloaderNames.add(gameVersion);
                    continue;
                }

                mcVersions.add(gameVersion);
                break;
            }

            //获取全部的Mod加载器
            List<ModLoaderList.ModLoader> modloaderList = new ArrayList<>();
            if (!modloaderNames.isEmpty()) {
                for (String modloaderName : modloaderNames) {
                    ModLoaderList.addModLoaderToList(modloaderList, modloaderName);
                }
            }

            String[] mcVersionsArray = new String[mcVersions.size()];
            mcVersions.toArray(mcVersionsArray);

            JsonArray dependencies = modDetail.get("dependencies").getAsJsonArray();
            List<ModDependencies> modDependencies = new ArrayList<>();
            if (!item.isModpack && dependencies.size() != 0) {
                for (JsonElement dependency : dependencies) {
                    JsonObject object = dependency.getAsJsonObject();
                    String modId = object.get("modId").getAsString();
                    String dependencyType = object.get("relationType").getAsString();

                    ModItem items = null;
                    if (!dependenciesModMap.containsKey(modId)) {
                        JsonObject response = searchModFromID(modId);
                        JsonObject hit = response.get("data").getAsJsonObject();

                        if (hit != null) {
                            JsonArray itemsGameVersions = modDetail.getAsJsonArray("gameVersions");
                            Set<ModLoaderList.ModLoader> itemsModloaderNames = new TreeSet<>();
                            for(JsonElement jsonElement : itemsGameVersions) {
                                String gameVersion = jsonElement.getAsString();

                                ModLoaderList.addModLoaderToList(itemsModloaderNames, gameVersion);
                            }

                            String iconUrl;
                            try {
                                iconUrl = hit.getAsJsonObject("logo").get("thumbnailUrl").getAsString();
                            } catch (Exception e) {
                                Logging.e("error", Tools.printToString(e));
                                iconUrl = null;
                            }

                            items = new ModItem(
                                    Constants.SOURCE_CURSEFORGE,
                                    hit.get("categories").getAsJsonArray().get(0).getAsJsonObject().get("classId").getAsInt() != CURSEFORGE_MOD_CLASS_ID,
                                    modId,
                                    hit.get("name").getAsString(),
                                    hit.get("summary").getAsString(),
                                    hit.get("downloadCount").getAsInt(),
                                    itemsModloaderNames.toArray(new ModLoaderList.ModLoader[]{}),
                                    iconUrl
                            );
                        }
                        dependenciesModMap.put(modId, items);
                    } else {
                        items = dependenciesModMap.get(modId);
                    }

                    if (items != null) {
                        modDependencies.add(new ModDependencies(items, ModDependencies.getDependencyType(dependencyType)));
                    }
                }
            }

            modVersionItems.add(new ModVersionItem(mcVersionsArray,
                    fileName,
                    displayName,
                    modloaderList.toArray(new ModLoaderList.ModLoader[]{}),
                    modDependencies,
                    VersionType.getVersionType(releaseTypeString),
                    getSha1FromModData(modDetail),
                    modDetail.get("downloadCount").getAsInt(),
                    downloadUrl));
        }

        return new ModDetail(item, modVersionItems);
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

    private int getPaginatedDetails(ArrayList<JsonObject> objectList, int index, String modId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("index", index);
        params.put("pageSize", CURSEFORGE_PAGINATION_SIZE);

        JsonObject response = mApiHandler.get("mods/"+modId+"/files", params, JsonObject.class);
        JsonArray data = GsonJsonUtils.getJsonArraySafe(response, "data");
        if(data == null) return CURSEFORGE_PAGINATION_ERROR;

        for(int i = 0; i < data.size(); i++) {
            JsonObject fileInfo = data.get(i).getAsJsonObject();
            if(fileInfo.get("isServerPack").getAsBoolean()) continue;
            objectList.add(fileInfo);
        }
        if(data.size() < CURSEFORGE_PAGINATION_SIZE) {
            return CURSEFORGE_PAGINATION_END_REACHED; // we read the remainder! yay!
        }
        return index + data.size();
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
            modDownloader.awaitFinish((c, m) ->
                    ProgressKeeper.submitProgress(ProgressLayout.INSTALL_MODPACK, (int) Math.max((float) c / m * 100, 0), R.string.modpack_download_downloading_mods_fc, c, m)
            );
            String overridesDir = "overrides";
            if (curseManifest.overrides != null) overridesDir = curseManifest.overrides;
            ZipUtils.zipExtract(modpackZipFile, overridesDir, instanceDestination);
            return createInfo(curseManifest.minecraft);
        }
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
                modLoaderTypeInt = ModLoader.MOD_LOADER_FORGE;
                break;
            case "neoforge":
                modLoaderTypeInt = ModLoader.MOD_LOADER_NEOFORGE;
                break;
            case "fabric":
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
