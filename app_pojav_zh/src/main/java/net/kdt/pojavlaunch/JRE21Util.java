package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.IOException;

public class JRE21Util {
    public static final String JRE_21_NAME = "Internal-21";
    public static boolean checkInternalJre21(AssetManager assetManager) {
        String launcher_jre21_version;
        String installed_jre21_version = MultiRTUtils.__internal__readBinpackVersion("JRE_21_NAME");
        try {
            launcher_jre21_version = Tools.read(assetManager.open("components/jre-21/version"));
        } catch (IOException exc) {
            return installed_jre21_version != null;
        }
        if(!launcher_jre21_version.equals(installed_jre21_version))
            return unpackJre21(assetManager, launcher_jre21_version);
        else return true;
    }

    private static boolean unpackJre21(AssetManager assetManager, String rt_version) {
        try {
            MultiRTUtils.installRuntimeNamedBinpack(
                    assetManager.open("components/jre-21/universal.tar.xz"),
                    assetManager.open("components/jre-21/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    "Internal-21", rt_version);
            MultiRTUtils.postPrepare("Internal-21");
            return true;
        }catch (IOException e) {
            Log.e("JRE21Auto", "Internal JRE unpack failed", e);
            return false;
        }
    }
    public static boolean isInternalJRE21(String s_runtime) {
        Runtime runtime = MultiRTUtils.read(s_runtime);
        if(runtime == null) return false;
        return JRE_21_NAME.equals(runtime.name);
    }

    /** @return true if everything is good, false otherwise.  */
    public static boolean installJre21IfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        if (versionInfo.javaVersion == null || versionInfo.javaVersion.component.equalsIgnoreCase("jre-legacy"))
            return true;

        LauncherProfiles.load();
        MinecraftProfile minecraftProfile = LauncherProfiles.getCurrentProfile();

        String selectedRuntime = Tools.getSelectedRuntime(minecraftProfile);

        Runtime runtime = MultiRTUtils.read(selectedRuntime);
        if (runtime.javaVersion >= versionInfo.javaVersion.majorVersion) {
            return true;
        }

        String appropriateRuntime = MultiRTUtils.getNearestJreName(versionInfo.javaVersion.majorVersion);
        if (appropriateRuntime != null) {
            if (JRE21Util.isInternalJRE21(appropriateRuntime)) {
                JRE21Util.checkInternalJre21(activity.getAssets());
            }
            minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
            LauncherProfiles.load();
        } else {
            if (versionInfo.javaVersion.majorVersion <= 17) {
                if (!JRE21Util.checkInternalJre21(activity.getAssets())){
                    showRuntimeFail(activity, versionInfo);
                    return false;
                } else {
                    minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + JRE21Util.JRE_21_NAME;
                    LauncherProfiles.load();
                }
            } else {
                showRuntimeFail(activity, versionInfo);
                return false;
            }
        }

        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompartiblert, verInfo.javaVersion.majorVersion));
    }

}
