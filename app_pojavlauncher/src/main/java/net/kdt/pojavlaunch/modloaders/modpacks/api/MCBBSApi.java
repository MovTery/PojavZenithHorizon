package net.kdt.pojavlaunch.modloaders.modpacks.api;

import static net.kdt.pojavlaunch.PojavZHTools.verifyMCBBSPackMeta;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.MCBBSPackMeta;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.utils.ZipUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MCBBSApi {
    public MCBBSApi() {
    }

    public ModLoader installMCBBSZip(File zipFile, File instanceDestination) throws IOException {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)){
            MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(modpackZipFile, "mcbbs.packmeta")),
                    MCBBSPackMeta.class);
            if(!verifyMCBBSPackMeta(mcbbsPackMeta)) {
                return null;
            }
            ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0, R.string.newdl_starting);
            runOnUiThread(() -> ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0, R.string.zh_select_modpack_local_installing_files, 0));

            String overridesDir = "overrides";
            Enumeration<? extends ZipEntry> zipEntries = modpackZipFile.entries();

            double entrySize = 0.0d; //文件大小计数
            AtomicInteger fileCounters = new AtomicInteger(); //文件数量计数
            final double totalFileSize = zipFile.length(); //文件总大小

            int dirNameLen = overridesDir.length();
            while(zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if(!entryName.startsWith(overridesDir) || zipEntry.isDirectory()) continue;
                entrySize += zipEntry.getSize();

                File zipDestination = new File(instanceDestination, entryName.substring(dirNameLen));
                FileUtils.ensureParentDirectory(zipDestination);
                try (InputStream inputStream = modpackZipFile.getInputStream(zipEntry);
                     OutputStream outputStream = new FileOutputStream(zipDestination)) {
                    IOUtils.copy(inputStream, outputStream);
                }

                int fileCount = fileCounters.getAndIncrement();
                int progress = (int) ((entrySize * 100L) / totalFileSize); // 计算进度
                runOnUiThread(() -> ProgressLayout.setProgress(ProgressLayout.DOWNLOAD_MINECRAFT, progress,
                        R.string.zh_select_modpack_local_installing_files, fileCount));
            }

            runOnUiThread(() -> ProgressLayout.clearProgress(ProgressLayout.DOWNLOAD_MINECRAFT));
            return createInfo(mcbbsPackMeta.addons);
        }
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
}
