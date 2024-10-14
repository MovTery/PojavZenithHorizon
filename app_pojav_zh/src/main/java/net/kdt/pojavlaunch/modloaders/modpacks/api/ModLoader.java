package net.kdt.pojavlaunch.modloaders.modpacks.api;

import android.content.Context;
import android.content.Intent;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask;
import net.kdt.pojavlaunch.modloaders.ForgeUtils;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeDownloadTask;
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils;

import java.io.File;

public class ModLoader {
    public static final int MOD_LOADER_FORGE = 0;
    public static final int MOD_LOADER_NEOFORGE = 1;
    public static final int MOD_LOADER_FABRIC = 2;
    public static final int MOD_LOADER_QUILT = 3;
    public final int modLoaderType;
    public final String modLoaderVersion;
    public final String minecraftVersion;

    public ModLoader(int modLoaderType, String modLoaderVersion, String minecraftVersion) {
        this.modLoaderType = modLoaderType;
        this.modLoaderVersion = modLoaderVersion;
        this.minecraftVersion = minecraftVersion;
    }

    /**
     * Get the Version ID (the name of the mod loader in the versions/ folder)
     * @return the Version ID as a string
     */
    public String getVersionId() {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                return minecraftVersion+"-forge-"+modLoaderVersion;
            case MOD_LOADER_NEOFORGE:
                return "neoforge-" + modLoaderVersion;
            case MOD_LOADER_FABRIC:
                return "fabric-loader-"+modLoaderVersion+"-"+minecraftVersion;
            case MOD_LOADER_QUILT:
                return "quilt-loader-"+modLoaderVersion+"-"+minecraftVersion;
            default:
                return null;
        }
    }

    /**
     * Obtain the name of the ModLoader based on the Id value stored internally
     * @return ModLoader Name
     */
    public String getNameById() {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                return "Forge";
            case MOD_LOADER_NEOFORGE:
                return "NeoForge";
            case MOD_LOADER_FABRIC:
                return "Fabric";
            case MOD_LOADER_QUILT:
                return "Quilt";
            default:
                return null;
        }
    }

    /**
     * Get the Runnable that needs to run in order to download the mod loader.
     * The task will also install the mod loader if it does not require GUI installation
     * @param listener the listener that gets notified of the installation status
     * @return the task Runnable that needs to be ran
     */
    public Runnable getDownloadTask(ModloaderDownloadListener listener) {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                return new ForgeDownloadTask(listener, minecraftVersion, modLoaderVersion);
            case MOD_LOADER_NEOFORGE:
                return new NeoForgeDownloadTask(listener, modLoaderVersion);
            case MOD_LOADER_FABRIC:
                return FabriclikeUtils.FABRIC_UTILS.getDownloadTask(listener, minecraftVersion, modLoaderVersion);
            case MOD_LOADER_QUILT:
                return FabriclikeUtils.QUILT_UTILS.getDownloadTask(listener, minecraftVersion, modLoaderVersion);
            default:
                return null;
        }
    }

    /**
     * Get the Intent to start the graphical installation of the mod loader.
     * This method should only be ran after the download task of the specified mod loader finishes.
     * This method returns null if the mod loader does not require GUI installation
     * @param context the package resolving Context (can be the base context)
     * @param modInstallerJar the JAR file of the mod installer, provided by ModloaderDownloadListener after the installation
     *                        finishes.
     * @return the Intent which the launcher needs to start in order to install the mod loader
     */
    public Intent getInstallationIntent(Context context, File modInstallerJar) {
        Intent baseIntent = new Intent(context, JavaGUILauncherActivity.class);
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
                ForgeUtils.addAutoInstallArgs(baseIntent, modInstallerJar, getVersionId());
                return baseIntent;
            case MOD_LOADER_NEOFORGE:
                NeoForgeUtils.addAutoInstallArgs(baseIntent, modInstallerJar);
                return baseIntent;
            case MOD_LOADER_FABRIC:
                FabriclikeUtils.addAutoInstallArgs(baseIntent, FabriclikeUtils.FABRIC_UTILS, minecraftVersion, modLoaderVersion, modInstallerJar);
                return baseIntent;
            case MOD_LOADER_QUILT:
            default:
                return null;
        }
    }

    /**
     * Check whether the mod loader this object denotes requires GUI installation
     * @return true if mod loader requires GUI installation, false otherwise
     */
    public boolean requiresGuiInstallation() {
        switch (modLoaderType) {
            case MOD_LOADER_FORGE:
            case MOD_LOADER_NEOFORGE:
            case MOD_LOADER_FABRIC:
                return true;
            case MOD_LOADER_QUILT:
            default:
                return false;
        }
    }
}
