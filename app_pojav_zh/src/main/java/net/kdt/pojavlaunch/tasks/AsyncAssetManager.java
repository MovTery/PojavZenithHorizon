package net.kdt.pojavlaunch.tasks;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.content.Context;
import android.content.res.AssetManager;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.CopyDefaultFromAssets;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.file.FileTools;

import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AsyncAssetManager {

    private AsyncAssetManager(){}

    /** Unpack single files, with no regard to version tracking */
    public static void unpackSingleFiles(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_SINGLE_FILES, 0);
        sExecutorService.execute(() -> {
            try {
                CopyDefaultFromAssets.copyFromAssets(ctx);

                Tools.copyAssetFile(ctx, "launcher_profiles.json", ProfilePathHome.getGameHome(), false);
                Tools.copyAssetFile(ctx,"resolv.conf", PathAndUrlManager.DIR_DATA, false);
            } catch (IOException e) {
                Logging.e("AsyncAssetManager", "Failed to unpack critical components !");
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_SINGLE_FILES);
        });
    }

    public static void unpackComponents(Context ctx){
        ProgressLayout.setProgress(ProgressLayout.EXTRACT_COMPONENTS, 0);
        sExecutorService.execute(() -> {
            try {
                unpackComponent(ctx, "other_login", false);

                unpackComponent(ctx, "caciocavallo", false);
                unpackComponent(ctx, "caciocavallo17", false);
                // Since the Java module system doesn't allow multiple JARs to declare the same module,
                // we repack them to a single file here
                unpackComponent(ctx, "lwjgl3", false);
                unpackComponent(ctx, "security", true);
                unpackComponent(ctx, "arc_dns_injector", true);
                unpackComponent(ctx, "forge_installer", true);
            } catch (IOException e) {
                Logging.e("AsyncAssetManager", "Failed o unpack components !",e );
            }
            ProgressLayout.clearProgress(ProgressLayout.EXTRACT_COMPONENTS);
        });
    }

    private static void unpackComponent(Context ctx, String component, boolean privateDirectory) throws IOException {
        AssetManager am = ctx.getAssets();
        String rootDir = privateDirectory ? PathAndUrlManager.DIR_DATA : PathAndUrlManager.DIR_GAME_HOME;

        File versionFile = new File(rootDir + "/" + component + "/version");
        InputStream is = am.open("components/" + component + "/version");
        if(!versionFile.exists()) {
            if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                FileUtils.deleteDirectory(versionFile.getParentFile());
            }
            FileTools.mkdir(versionFile.getParentFile());

            Logging.i("UnpackPrep", component + ": Pack was installed manually, or does not exist, unpacking new...");
            String[] fileList = am.list("components/" + component);
            for(String s : fileList) {
                Tools.copyAssetFile(ctx, "components/" + component + "/" + s, rootDir + "/" + component, true);
            }
        } else {
            FileInputStream fis = new FileInputStream(versionFile);
            String release1 = Tools.read(is);
            String release2 = Tools.read(fis);
            if (!release1.equals(release2)) {
                if (versionFile.getParentFile().exists() && versionFile.getParentFile().isDirectory()) {
                    FileUtils.deleteDirectory(versionFile.getParentFile());
                }
                FileTools.mkdir(versionFile.getParentFile());

                String[] fileList = am.list("components/" + component);
                for (String fileName : fileList) {
                    Tools.copyAssetFile(ctx, "components/" + component + "/" + fileName, rootDir + "/" + component, true);
                }
            } else {
                Logging.i("UnpackPrep", component + ": Pack is up-to-date with the launcher, continuing...");
            }
        }
    }
}
