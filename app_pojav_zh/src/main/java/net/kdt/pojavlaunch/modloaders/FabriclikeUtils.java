package net.kdt.pojavlaunch.modloaders;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.feature.log.Logging;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabriclikeUtils {

    public static final FabriclikeUtils FABRIC_UTILS = new FabriclikeUtils("https://fabricmc.net/", "https://meta.fabricmc.net/v2", "fabric", "Fabric");
    public static final FabriclikeUtils QUILT_UTILS = new FabriclikeUtils("https://quiltmc.org/", "https://meta.quiltmc.org/v3", "quilt", "Quilt");

    private static final String INSTALLER_METADATA_URL = "%s/versions/installer";
    private static final String LOADER_METADATA_URL = "%s/versions/loader";
    private static final String GAME_METADATA_URL = "%s/versions/game";

    private static final String JSON_DOWNLOAD_URL = "%s/versions/loader/%s/%s/profile/json";

    private final String mWebUrl;
    private final String mApiUrl;
    private final String mCachePrefix;
    private final String mName;

    private FabriclikeUtils(String mWebUrl, String mApiUrl, String cachePrefix, String mName) {
        this.mWebUrl = mWebUrl;
        this.mApiUrl = mApiUrl;
        this.mCachePrefix = cachePrefix;
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

    public String getInstallerDownloadUrl() throws Exception {
        String jsonString = DownloadUtils.downloadStringCached(String.format(INSTALLER_METADATA_URL, mApiUrl),
                mCachePrefix + "_installer", false, input -> input);

        JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();//始终获取最新的安装器信息
        String url = jsonObject.get("url").getAsString();
        System.out.println(url);

        return url;
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

    public String getWebUrl() {
        return mWebUrl;
    }

    public String getName() {
        return mName;
    }

    public String getIconName() {
        return mCachePrefix;
    }

    public FabriclikeDownloadTask getDownloadTask(ModloaderListenerProxy listenerProxy, String gameVersion, String loaderVersion) {
        if (Objects.equals("Fabric", mName)) {
            return new FabriclikeDownloadTask(listenerProxy, this);
        } else return new FabriclikeDownloadTask(listenerProxy, this, gameVersion, loaderVersion);
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

    public void addAutoInstallArgs(Intent intent, String gameVersion, String loaderVersion, File modInstallerJar) {
        String installDir = ProfilePathHome.getGameHome();
        String javaArgs = "-jar " + modInstallerJar.getAbsolutePath();

        if (Objects.equals("Fabric", mName)) {
            javaArgs +=
                    " client" +
                    " -mcversion " + gameVersion +
                    " -loader " + loaderVersion +
                    " -dir " + installDir;
        } else if (Objects.equals("Quilt", mName)) {
            javaArgs +=
                    " install client " +
                    gameVersion + " " +
                    loaderVersion + " " +
                    "--install-dir=" + installDir;
        }

        System.out.println(javaArgs);

        intent.putExtra("javaArgs", javaArgs);
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true);
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true);
    }
}
