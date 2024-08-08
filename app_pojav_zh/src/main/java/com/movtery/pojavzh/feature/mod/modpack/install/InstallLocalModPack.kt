package com.movtery.pojavzh.feature.mod.modpack.install

import android.content.Context
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager
import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta
import com.movtery.pojavzh.feature.mod.modpack.MCBBSModPack
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils.ModPackEnum
import com.movtery.pojavzh.ui.dialog.TipDialog
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi
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
            onInstallStartListener: OnInstallStartListener
        ): ModLoader? {
            try {
                ZipFile(zipFile).use { modpackZipFile ->
                    val zipName = zipFile.name
                    val packName = zipName.substring(0, zipName.lastIndexOf('.'))
                    val modLoader: ModLoader?
                    when (type) {
                        ModPackEnum.CURSEFORGE -> {
                            val curseforgeEntry = modpackZipFile.getEntry("manifest.json")
                            val curseManifest = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(
                                    modpackZipFile.getInputStream(curseforgeEntry)
                                ), CurseManifest::class.java
                            )

                            modLoader = curseforgeModPack(context, zipFile, packName, onInstallStartListener)
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

                            modLoader = mcbbsModPack(context, zipFile, packName, onInstallStartListener)
                            MCBBSModPack.createModPackProfiles(
                                packName,
                                mcbbsPackMeta,
                                modLoader?.versionId
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

                            modLoader = modrinthModPack(zipFile, packName, onInstallStartListener)
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
                                    .setMessage(R.string.zh_select_modpack_local_not_supported) //弹窗提醒
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
            context: Context,
            zipFile: File,
            packName: String,
            onInstallStartListener: OnInstallStartListener
        ): ModLoader {
            val curseforgeApi = CurseforgeApi(context.getString(R.string.curseforge_api_key))
            return curseforgeApi.installCurseforgeZip(
                zipFile,
                File(ProfilePathManager.currentPath, "custom_instances/$packName"),
                onInstallStartListener
            )
        }

        @Throws(Exception::class)
        private fun modrinthModPack(
            zipFile: File,
            packName: String,
            onInstallStartListener: OnInstallStartListener
        ): ModLoader {
            val modrinthApi = ModrinthApi()
            return modrinthApi.installMrpack(
                zipFile,
                File(ProfilePathManager.currentPath, "custom_instances/$packName"),
                onInstallStartListener
            )
        }

        @Throws(Exception::class)
        private fun mcbbsModPack(context: Context, zipFile: File, packName: String, onInstallStartListener: OnInstallStartListener): ModLoader? {
            val mcbbsModPack = MCBBSModPack(context, zipFile)
            return mcbbsModPack.install(
                File(
                    ProfilePathManager.currentPath,
                    "custom_instances/$packName"
                ), onInstallStartListener
            )
        }
    }
}
