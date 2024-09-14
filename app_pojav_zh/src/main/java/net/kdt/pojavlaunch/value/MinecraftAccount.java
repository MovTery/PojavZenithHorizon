package net.kdt.pojavlaunch.value;

import net.kdt.pojavlaunch.*;

import java.io.*;
import com.google.gson.*;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import org.apache.commons.io.FileUtils;

@Keep
public class MinecraftAccount {
    public String accessToken = "0"; // access token
    public String clientToken = "0"; // clientID: refresh and invalidate
    public String profileId = "00000000-0000-0000-0000-000000000000"; // profile UUID, for obtaining skin
    public String username = "Steve";
    public String selectedVersion = "1.7.10";
    public boolean isMicrosoft = false;
    public String msaRefreshToken = "0";
    public String xuid;
    public long expiresAt;
    public String baseUrl;
    public String account;

    void updateSkinFace(String uuid) {
        try {
            File skinFile = getSkinFaceFile(username);
            if(skinFile.exists()) FileUtils.deleteQuietly(skinFile); //清除一次图标
            Tools.downloadFile("https://crafthead.net/helm/" + uuid + "/100", skinFile.getAbsolutePath());

            Logging.i("SkinLoader", "Update skin face success");
        } catch (IOException e) {
            // Skin refresh limit, no internet connection, etc...
            // Simply ignore updating skin face
            Logging.w("SkinLoader", "Could not update skin face", e);
        }
    }

    public boolean isLocal(){
        return accessToken.equals("0");
    }
    
    public void updateSkinFace() {
        updateSkinFace(profileId);
    }
    
    public String save(String outPath) throws IOException {
        Tools.write(outPath, Tools.GLOBAL_GSON.toJson(this));
        return username;
    }
    
    public String save() throws IOException {
        return save(PathAndUrlManager.DIR_ACCOUNT_NEW + "/" + username + ".json");
    }
    
    public static MinecraftAccount parse(String content) throws JsonSyntaxException {
        return Tools.GLOBAL_GSON.fromJson(content, MinecraftAccount.class);
    }

    public static MinecraftAccount load(String name) {
        if(!accountExists(name)) return null;
        try {
            MinecraftAccount acc = parse(Tools.read(PathAndUrlManager.DIR_ACCOUNT_NEW + "/" + name + ".json"));
            if (acc.accessToken == null) {
                acc.accessToken = "0";
            }
            if (acc.clientToken == null) {
                acc.clientToken = "0";
            }
            if (acc.profileId == null) {
                acc.profileId = "00000000-0000-0000-0000-000000000000";
            }
            if (acc.username == null) {
                acc.username = "0";
            }
            if (acc.selectedVersion == null) {
                acc.selectedVersion = "1.7.10";
            }
            if (acc.msaRefreshToken == null) {
                acc.msaRefreshToken = "0";
            }
            return acc;
        } catch(IOException | JsonSyntaxException e) {
            Logging.e(MinecraftAccount.class.getName(), "Caught an exception while loading the profile",e);
            return null;
        }
    }

    private static File getSkinFaceFile(String username) {
        return new File(PathAndUrlManager.DIR_USER_ICON, username + ".png");
    }

    private static boolean accountExists(String username){
        return new File(PathAndUrlManager.DIR_ACCOUNT_NEW + "/" + username + ".json").exists();
    }

    @NonNull
    @Override
    public String toString() {
        return "MinecraftAccount{" +
                "username='" + username + '\'' +
                ", isMicrosoft=" + isMicrosoft +
                '}';
    }
}
