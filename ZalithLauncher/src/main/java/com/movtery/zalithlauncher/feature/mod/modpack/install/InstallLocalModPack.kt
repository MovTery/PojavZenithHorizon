package com.movtery.zalithlauncher.feature.mod.modpack.install

import android.content.Context
import com.movtery.zalithlauncher.feature.customprofilepath.ProfilePathManager
import com.movtery.zalithlauncher.feature.download.install.OnInstallStartListener
import com.movtery.zalithlauncher.feature.download.item.ModLoaderWrapper
import com.movtery.zalithlauncher.feature.download.platform.curseforge.CurseForgeModPackInstallHelper
import com.movtery.zalithlauncher.feature.download.platform.modrinth.ModrinthModPackInstallHelper
import com.movtery.zalithlauncher.feature.download.utils.PlatformUtils
import com.movtery.zalithlauncher.feature.mod.models.MCBBSPackMeta
import com.movtery.zalithlauncher.feature.mod.modpack.MCBBSModPack
import com.movtery.zalithlauncher.feature.mod.modpack.install.ModPackUtils.ModPackEnum
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex
import net.kdt.pojavlaunch.utils.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

class InstallLocalModPack {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun installModPack(
            context: Context,
            type: ModPackEnum?,
            zipFile: File,
            listener: OnInstallStartListener
        ): ModLoaderWrapper? {
            try {
                ZipFile(zipFile).use { modpackZipFile ->
                    val zipName = zipFile.name
                    val packName = zipName.substring(0, zipName.lastIndexOf('.'))
                    val modLoader: ModLoaderWrapper?
                    when (type) {
                        ModPackEnum.CURSEFORGE -> {
                            val curseforgeEntry = modpackZipFile.getEntry("manifest.json")
                            val curseManifest = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(
                                    modpackZipFile.getInputStream(curseforgeEntry)
                                ), CurseManifest::class.java
                            )

                            modLoader = curseforgeModPack(zipFile, packName, listener) ?: return null
                            ModPackUtils.createModPackProfiles(
                                packName,
                                curseManifest.name,
                                modLoader.versionId
                            )

                            return modLoader
                        }

                        ModPackEnum.MCBBS -> {
                            val mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta")

                            val mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(
                                    modpackZipFile.getInputStream(mcbbsEntry)
                                ), MCBBSPackMeta::class.java
                            )

                            modLoader = mcbbsModPack(context, zipFile, packName, listener) ?: return null
                            MCBBSModPack.createModPackProfiles(
                                packName,
                                mcbbsPackMeta,
                                modLoader.versionId
                            )

                            return modLoader
                        }

                        ModPackEnum.MODRINTH -> {
                            val modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(
                                    ZipUtils.getEntryStream(
                                        modpackZipFile,
                                        "modrinth.index.json"
                                    )
                                ),
                                ModrinthIndex::class.java
                            ) // 用于获取创建实例所需的数据

                            modLoader = modrinthModPack(zipFile, packName, listener) ?: return null
                            ModPackUtils.createModPackProfiles(
                                packName,
                                modrinthIndex.name,
                                modLoader.versionId
                            )

                            return modLoader
                        }

                        else -> {
                            Tools.runOnUiThread {
                                TipDialog.Builder(context)
                                    .setMessage(R.string.select_modpack_local_not_supported) //弹窗提醒
                                    .setShowCancel(true)
                                    .setShowConfirm(false)
                                    .buildDialog()
                            }
                            return null
                        }
                    }
                }
            } finally {
                FileUtils.deleteQuietly(zipFile) // 删除文件（虽然文件通常来说并不会很大）
            }
        }

        @Throws(Exception::class)
        private fun curseforgeModPack(
            zipFile: File,
            packName: String,
            listener: OnInstallStartListener
        ): ModLoaderWrapper? {
            return CurseForgeModPackInstallHelper.installZip(
                PlatformUtils.createCurseForgeApi(),
                zipFile,
                File(ProfilePathManager.currentPath, "modpack_instances/$packName"),
                listener = listener
            )
        }

        @Throws(Exception::class)
        private fun modrinthModPack(
            zipFile: File,
            packName: String,
            listener: OnInstallStartListener
        ): ModLoaderWrapper? {
            return ModrinthModPackInstallHelper.installZip(
                zipFile,
                File(ProfilePathManager.currentPath, "modpack_instances/$packName"),
                listener = listener
            )
        }

        @Throws(Exception::class)
        private fun mcbbsModPack(context: Context, zipFile: File, packName: String, listener: OnInstallStartListener): ModLoaderWrapper? {
            val mcbbsModPack = MCBBSModPack(context, zipFile)
            return mcbbsModPack.install(
                File(ProfilePathManager.currentPath, "modpack_instances/$packName"), listener
            )
        }
    }
}
