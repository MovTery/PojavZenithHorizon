package com.movtery.pojavzh.feature.mod.modpack;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.util.Log;

import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.feature.mod.modpack.install.OnInstallStartListener;
import com.movtery.pojavzh.ui.dialog.ProgressDialog;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.utils.file.FileUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MCBBSModPack {
    private final Context context;
    private final File zipFile;
    private ProgressDialog installDialog;
    private boolean isCanceled = false;

    public MCBBSModPack(Context context, File zipFile) {
        this.context = context;
        this.zipFile = zipFile;
    }

    public ModLoader install(File instanceDestination, OnInstallStartListener onInstallStartListener) throws IOException {
        if (zipFile != null) {
            try (ZipFile modpackZipFile = new ZipFile(this.zipFile)) {
                MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                        Tools.read(ZipUtils.getEntryStream(modpackZipFile, "mcbbs.packmeta")),
                        MCBBSPackMeta.class);
                if (!ModPackUtils.verifyMCBBSPackMeta(mcbbsPackMeta)) {
                    Log.i("MCBBSModPack","manifest verification failed");
                    return null;
                }
                if (onInstallStartListener != null) onInstallStartListener.onStart();

                initDialog();

                String overridesDir = "overrides" + File.separatorChar;
                int dirNameLen = overridesDir.length();

                AtomicInteger fileCounters = new AtomicInteger(); //文件数量计数
                int length = mcbbsPackMeta.files.length;

                for (MCBBSPackMeta.MCBBSFile file : mcbbsPackMeta.files) {
                    if (isCanceled) {
                        cancel(instanceDestination);
                        return null;
                    }

                    ZipEntry entry = modpackZipFile.getEntry(overridesDir + file.path);
                    if (entry != null) {
                        try (InputStream inputStream = modpackZipFile.getInputStream(entry)) {
                            String fileHash = FileUtils.getFileHashSHA1(inputStream);
                            boolean equals = Objects.equals(file.hash, fileHash);
//                            System.out.println(file.hash + " --- " + fileHash + " " + equals);

                            if (equals) {
                                String entryName = entry.getName();
                                File zipDestination = new File(instanceDestination, entryName.substring(dirNameLen));
                                if (zipDestination.exists() && !file.force) continue;
                                //如果哈希值一致，则复制文件（文件已存在则根据“强制”设定来决定是否覆盖文件）
                                net.kdt.pojavlaunch.utils.FileUtils.ensureParentDirectory(zipDestination);

                                try (InputStream entryInputStream = modpackZipFile.getInputStream(entry);
                                     OutputStream outputStream = Files.newOutputStream(zipDestination.toPath())) {
                                    IOUtils.copy(entryInputStream, outputStream);
                                }

                                int fileCount = fileCounters.getAndIncrement();
                                runOnUiThread(() -> {
                                    installDialog.updateText(context.getString(R.string.zh_select_modpack_local_installing_files, fileCount, length));
                                    installDialog.updateProgress(fileCount, length);
                                });
                            }
                        }
                    }
                }

                closeDialog();
                return createInfo(mcbbsPackMeta.addons);
            }
        } else return null;
    }

    private void initDialog() {
        runOnUiThread(() -> {
            installDialog = new ProgressDialog(context, () -> {
                isCanceled = true;
                return true;
            });
            installDialog.show();
        });
    }

    private void closeDialog() {
        runOnUiThread(() -> installDialog.dismiss());
    }

    private void cancel(File instanceDestination) {
        org.apache.commons.io.FileUtils.deleteQuietly(instanceDestination);
    }

    private ModLoader createInfo(MCBBSPackMeta.MCBBSAddons[] addons) {
        String version = "";
        String modLoader = "";
        String modLoaderVersion = "";
        for(int i = 0; i <= addons.length; i++) {
            if(addons[i].id.equals("game")) {
                version = addons[i].version;
                continue;
            }
            if(addons[i] != null){
                modLoader = addons[i].id;
                modLoaderVersion = addons[i].version;
                break;
            }
        }
        int modLoaderTypeInt;
        switch (modLoader) {
            case "forge": modLoaderTypeInt = ModLoader.MOD_LOADER_FORGE; break;
            case "neoforge": modLoaderTypeInt = ModLoader.MOD_LOADER_NEOFORGE; break;
            case "fabric": modLoaderTypeInt = ModLoader.MOD_LOADER_FABRIC; break;
            default: return null;
        }
        return new ModLoader(modLoaderTypeInt, modLoaderVersion, version);
    }

    public static void createModPackProfiles(String modpackName, MCBBSPackMeta mcbbsPackMeta, String versionId) {
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "./custom_instances/" + modpackName;
        profile.name = mcbbsPackMeta.name;
        profile.lastVersionId = versionId;
        profile.javaArgs = StringUtils.insertSpace(null, mcbbsPackMeta.launchInfo.javaArgument);

        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.write(ProfilePathManager.getCurrentProfile());
    }
}
