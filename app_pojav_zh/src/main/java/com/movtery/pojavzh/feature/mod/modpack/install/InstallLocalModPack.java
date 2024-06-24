package com.movtery.pojavzh.feature.mod.modpack.install;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.widget.Toast;

import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta;
import com.movtery.pojavzh.feature.mod.modpack.MCBBSModPack;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathManager;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
import net.kdt.pojavlaunch.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InstallLocalModPack {
    public static ModLoader installModPack(Context context, int type, File zipFile, OnInstallStartListener onInstallStartListener) throws Exception {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)) {
            String zipName = zipFile.getName();
            String packName = zipName.substring(0, zipName.lastIndexOf('.'));
            ModLoader modLoader;
            switch (type) {
                case 1: //curseforge
                    ZipEntry curseforgeEntry = modpackZipFile.getEntry("manifest.json");
                    CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest.class);

                    modLoader = curseforgeModPack(context, zipFile, packName, onInstallStartListener);
                    ModPackUtils.createModPackProfiles(packName, curseManifest.name, modLoader.getVersionId());

                    return modLoader;
                case 2: //mcbbs
                    ZipEntry mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta");

                    MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta.class);

                    modLoader = mcbbsModPack(context, zipFile, packName, onInstallStartListener);
                    if (modLoader != null)
                        MCBBSModPack.createModPackProfiles(packName, mcbbsPackMeta, modLoader.getVersionId());

                    return modLoader;
                case 3: // modrinth
                    ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(ZipUtils.getEntryStream(modpackZipFile, "modrinth.index.json")),
                            ModrinthIndex.class); // 用于获取创建实例所需的数据

                    modLoader = modrinthModPack(zipFile, packName, onInstallStartListener);
                    ModPackUtils.createModPackProfiles(packName, modrinthIndex.name, modLoader.getVersionId());

                    return modLoader;
                default:
                    runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_select_modpack_local_not_supported), Toast.LENGTH_SHORT).show());
                    return null;
            }
        } finally {
            FileUtils.deleteQuietly(zipFile); // 删除文件（虽然文件通常来说并不会很大）
        }
    }

    private static ModLoader curseforgeModPack(Context context, File zipFile, String packName, OnInstallStartListener onInstallStartListener) throws Exception {
        CurseforgeApi curseforgeApi = new CurseforgeApi(context.getString(R.string.curseforge_api_key));
        return curseforgeApi.installCurseforgeZip(zipFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName), onInstallStartListener);
    }

    private static ModLoader modrinthModPack(File zipFile, String packName, OnInstallStartListener onInstallStartListener) throws Exception {
        ModrinthApi modrinthApi = new ModrinthApi();
        return modrinthApi.installMrpack(zipFile, new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName), onInstallStartListener);
    }

    private static ModLoader mcbbsModPack(Context context, File zipFile, String packName, OnInstallStartListener onInstallStartListener) throws Exception {
        MCBBSModPack mcbbsModPack = new MCBBSModPack(context, zipFile);
        return mcbbsModPack.install(new File(ProfilePathManager.getCurrentPath(), "custom_instances/" + packName), onInstallStartListener);
    }
}
