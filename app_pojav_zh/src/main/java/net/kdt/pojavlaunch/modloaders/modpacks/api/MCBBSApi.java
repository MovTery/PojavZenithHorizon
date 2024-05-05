package net.kdt.pojavlaunch.modloaders.modpacks.api;

import static net.kdt.pojavlaunch.PojavZHTools.verifyMCBBSPackMeta;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Context;
import android.widget.TextView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.DownloadDialog;
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
    private DownloadDialog downloadDialog;
    private TextView downloadTipTextView;
    private boolean isStopped = false;

    public MCBBSApi() {
    }

    public ModLoader installMCBBSZip(Context context, File zipFile, File instanceDestination) throws IOException {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)){
            MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(modpackZipFile, "mcbbs.packmeta")),
                    MCBBSPackMeta.class);
            if(!verifyMCBBSPackMeta(mcbbsPackMeta)) {
                return null;
            }

            runOnUiThread(() -> {
                MCBBSApi.this.downloadDialog = new DownloadDialog(context);
                MCBBSApi.this.downloadTipTextView = MCBBSApi.this.downloadDialog.getTextView();

                MCBBSApi.this.downloadDialog.getCancelButton().setOnClickListener(view -> {
                    MCBBSApi.this.isStopped = true;
                    MCBBSApi.this.downloadDialog.dismiss();
                });
                MCBBSApi.this.downloadDialog.show();
            });

            String overridesDir = "overrides";
            Enumeration<? extends ZipEntry> zipEntries = modpackZipFile.entries();

            AtomicInteger fileCounters = new AtomicInteger(); //文件数量计数

            int dirNameLen = overridesDir.length();
            while(zipEntries.hasMoreElements() && !MCBBSApi.this.isStopped) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if(!entryName.startsWith(overridesDir) || zipEntry.isDirectory()) continue;

                File zipDestination = new File(instanceDestination, entryName.substring(dirNameLen));
                FileUtils.ensureParentDirectory(zipDestination);
                try (InputStream inputStream = modpackZipFile.getInputStream(zipEntry);
                     OutputStream outputStream = new FileOutputStream(zipDestination)) {
                    IOUtils.copy(inputStream, outputStream);
                }

                int fileCount = fileCounters.getAndIncrement();
                runOnUiThread(() -> {
                    TextView textView = MCBBSApi.this.downloadTipTextView.findViewById(R.id.zh_download_upload_textView);
                    textView.setText(context.getString(R.string.zh_select_modpack_local_installing_files, fileCount));
                });
            }

            runOnUiThread(() -> MCBBSApi.this.downloadDialog.dismiss());

            if (MCBBSApi.this.isStopped) {
                // 如果玩家取消了安装，那么就删除已经安装的文件
                org.apache.commons.io.FileUtils.deleteDirectory(instanceDestination);
                return null;
            } else {
                return createInfo(mcbbsPackMeta.addons);
            }
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
