package com.movtery.pojavzh.feature.download.item

import android.content.Context
import android.content.Intent
import com.movtery.pojavzh.feature.download.enums.ModLoader
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeDownloadTask
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils.Companion.addAutoInstallArgs
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask
import net.kdt.pojavlaunch.modloaders.ForgeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import java.io.File

class ModLoaderWrapper(
    val modLoader: ModLoader,
    val modLoaderVersion: String,
    val minecraftVersion: String
) {
    val versionId: String?
        /**
         * Get the Version ID (the name of the mod loader in the versions/ folder)
         * @return the Version ID as a string
         */
        get() = when (modLoader) {
            ModLoader.FORGE -> "$minecraftVersion-forge-$modLoaderVersion"
            ModLoader.NEOFORGE -> "neoforge-$modLoaderVersion"
            ModLoader.FABRIC -> "fabric-loader-$modLoaderVersion-$minecraftVersion"
            ModLoader.QUILT -> "quilt-loader-$modLoaderVersion-$minecraftVersion"
            else -> null
        }

    val nameById: String?
        /**
         * Obtain the name of the ModLoader based on the Id value stored internally
         * @return ModLoader Name
         */
        get() = when (modLoader) {
            ModLoader.FORGE -> "Forge"
            ModLoader.NEOFORGE -> "NeoForge"
            ModLoader.FABRIC -> "Fabric"
            ModLoader.QUILT -> "Quilt"
            else -> null
        }

    /**
     * Get the Runnable that needs to run in order to download the mod loader.
     * The task will also install the mod loader if it does not require GUI installation
     * @param listener the listener that gets notified of the installation status
     * @return the task Runnable that needs to be ran
     */
    fun getDownloadTask(listener: ModloaderDownloadListener): Runnable? {
        return when (modLoader) {
            ModLoader.FORGE -> ForgeDownloadTask(
                listener,
                minecraftVersion,
                modLoaderVersion
            )

            ModLoader.NEOFORGE -> NeoForgeDownloadTask(
                listener,
                modLoaderVersion
            )

            ModLoader.FABRIC -> FabriclikeUtils.FABRIC_UTILS.getDownloadTask(
                listener,
                minecraftVersion,
                modLoaderVersion
            )

            ModLoader.QUILT -> FabriclikeUtils.QUILT_UTILS.getDownloadTask(
                listener,
                minecraftVersion,
                modLoaderVersion
            )

            else -> null
        }
    }

    /**
     * Get the Intent to start the graphical installation of the mod loader.
     * This method should only be ran after the download task of the specified mod loader finishes.
     * This method returns null if the mod loader does not require GUI installation
     * @param context the package resolving Context (can be the base context)
     * @param modInstallerJar the JAR file of the mod installer, provided by ModloaderDownloadListener after the installation
     * finishes.
     * @return the Intent which the launcher needs to start in order to install the mod loader
     */
    fun getInstallationIntent(context: Context?, modInstallerJar: File): Intent? {
        val baseIntent = Intent(context, JavaGUILauncherActivity::class.java)
        when (modLoader) {
            ModLoader.FORGE -> {
                ForgeUtils.addAutoInstallArgs(baseIntent, modInstallerJar, versionId)
                return baseIntent
            }

            ModLoader.NEOFORGE -> {
                addAutoInstallArgs(baseIntent, modInstallerJar)
                return baseIntent
            }

            ModLoader.FABRIC -> {
                FabriclikeUtils.addAutoInstallArgs(
                    baseIntent, FabriclikeUtils.FABRIC_UTILS,
                    minecraftVersion,
                    modLoaderVersion, modInstallerJar
                )
                return baseIntent
            }

            ModLoader.QUILT -> return null
            else -> return null
        }
    }

    /**
     * Check whether the mod loader this object denotes requires GUI installation
     * @return true if mod loader requires GUI installation, false otherwise
     */
    fun requiresGuiInstallation(): Boolean {
        return when (modLoader) {
            ModLoader.FORGE, ModLoader.NEOFORGE, ModLoader.FABRIC -> true
            ModLoader.QUILT -> false
            else -> false
        }
    }
}
