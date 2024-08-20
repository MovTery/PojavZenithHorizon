package net.kdt.pojavlaunch.modloaders;

import com.google.gson.JsonSyntaxException;
import com.movtery.pojavzh.feature.log.Logging;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FabriclikeUtils {

    public static final FabriclikeUtils FABRIC_UTILS = new FabriclikeUtils("https://meta.fabricmc.net/v2", "fabric", "Fabric", "fabric");
    public static final FabriclikeUtils QUILT_UTILS = new FabriclikeUtils("https://meta.quiltmc.org/v3", "quilt", "Quilt", "quilt");

    private static final String LOADER_METADATA_URL = "%s/versions/loader";
    private static final String GAME_METADATA_URL = "%s/versions/game";

    private static final String JSON_DOWNLOAD_URL = "%s/versions/loader/%s/%s/profile/json";

    private final String mApiUrl;
    private final String mCachePrefix;
    private final String mName;
    private final String mIconName;

    private FabriclikeUtils(String mApiUrl, String cachePrefix, String mName, String iconName) {
        this.mApiUrl = mApiUrl;
        this.mCachePrefix = cachePrefix;
        this.mIconName = iconName;
        this.mName = mName;
    }

    public FabricVersion[] downloadGameVersions(boolean force) throws IOException{
        try {
            return DownloadUtils.downloadStringCached(String.format(GAME_METADATA_URL, mApiUrl), mCachePrefix+"_game_versions", force,
                    FabriclikeUtils::deserializeRawVersions
            );
        }catch (DownloadUtils.ParseException ignored) {}
        return null;
    }

    public FabricVersion[] downloadLoaderVersions(boolean force) throws IOException {
        try {
            return DownloadUtils.downloadStringCached(String.format(LOADER_METADATA_URL, mApiUrl),
                    mCachePrefix + "_loader_versions", force,
                    (input) -> {
                        try {
                            return deserializeLoaderVersions(input);
                        } catch (JSONException e) {
                            throw new DownloadUtils.ParseException(e);
                        }
                    });
        } catch (DownloadUtils.ParseException e) {
            Logging.e("Download Fabric Meta", e.toString());
        }
        return null;
    }

    public String createJsonDownloadUrl(String gameVersion, String loaderVersion) {
        try {
            gameVersion = URLEncoder.encode(gameVersion, "UTF-8");
            loaderVersion = URLEncoder.encode(loaderVersion, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return String.format(JSON_DOWNLOAD_URL, mApiUrl, gameVersion, loaderVersion);
    }

    public String getName() {
        return mName;
    }
    public String getIconName() {
        return mIconName;
    }

    private static FabricVersion[] deserializeLoaderVersions(String input) throws JSONException {
        JSONArray jsonArray = new JSONArray(input);
        List<FabricVersion> fabricVersions = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            FabricVersion fabricVersion = new FabricVersion();
            if (jsonObject.has("stable")) fabricVersion.stable = jsonObject.getBoolean("stable");
            fabricVersion.version = jsonObject.getString("version");

            fabricVersions.add(fabricVersion);
        }
        return fabricVersions.toArray(new FabricVersion[]{});
    }

    private static FabricVersion[] deserializeRawVersions(String jsonArrayIn) throws DownloadUtils.ParseException {
        try {
            return Tools.GLOBAL_GSON.fromJson(jsonArrayIn, FabricVersion[].class);
        }catch (JsonSyntaxException e) {
            Logging.e(FabriclikeUtils.class.getName(), Tools.printToString(e));
            throw new DownloadUtils.ParseException(null);
        }
    }
}
