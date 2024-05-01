package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.content.res.AssetManager;
import android.util.Log;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;

import java.io.IOException;

public class JRE21Util {
    public static final String JRE_21_NAME = "Internal-21";
    public static void checkInternalJre21(AssetManager assetManager) {
        String launcher_jre21_version;
        String installed_jre21_version = MultiRTUtils.__internal__readBinpackVersion("JRE_21_NAME");
        try {
            launcher_jre21_version = Tools.read(assetManager.open("components/jre-21/version"));
        } catch (IOException exc) {
            return;
        }
        if(!launcher_jre21_version.equals(installed_jre21_version)) {
            unpackJre21(assetManager, launcher_jre21_version);
        }
    }

    private static void unpackJre21(AssetManager assetManager, String rt_version) {
        PojavApplication.sExecutorService.execute(() -> {
            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        assetManager.open("components/jre-21/universal.tar.xz"),
                        assetManager.open("components/jre-21/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        JRE_21_NAME, rt_version);
                MultiRTUtils.postPrepare(JRE_21_NAME);
            }catch (IOException e) {
                Log.e("JRE21Auto", "Internal JRE unpack failed", e);
            }
        });
    }
}
