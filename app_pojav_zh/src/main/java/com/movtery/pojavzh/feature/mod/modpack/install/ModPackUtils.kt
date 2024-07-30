package com.movtery.pojavzh.feature.mod.modpack.install

import android.util.Log
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager.currentProfile
import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import java.io.File
import java.util.zip.ZipFile

object ModPackUtils {
    @JvmStatic
    fun determineModpack(modpack: File): ModPackEnum {
        val zipName = modpack.name
        val suffix = zipName.substring(zipName.lastIndexOf('.'))
        try {
            ZipFile(modpack).use { modpackZipFile ->
                if (suffix == ".zip") {
                    val mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta")
                    val curseforgeEntry = modpackZipFile.getEntry("manifest.json")
                    if (mcbbsEntry == null && curseforgeEntry != null) {
                        val curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest::class.java
                        )
                        if (verifyManifest(curseManifest)) return ModPackEnum.CURSEFORGE
                    } else if (mcbbsEntry != null) {
                        val mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta::class.java
                        )
                        if (verifyMCBBSPackMeta(mcbbsPackMeta)) return ModPackEnum.MCBBS
                    }
                } else if (suffix == ".mrpack") {
                    val entry = modpackZipFile.getEntry("modrinth.index.json")
                    if (entry != null) {
                        val modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(entry)),
                            ModrinthIndex::class.java
                        )
                        if (verifyModrinthIndex(modrinthIndex)) return ModPackEnum.MODRINTH
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("determineModpack", e.toString())
        }

        return ModPackEnum.UNKNOWN
    }

    fun createModPackProfiles(modpackName: String, profileName: String?, versionId: String?) {
        val profile = MinecraftProfile()
        profile.gameDir = "./custom_instances/$modpackName"
        profile.name = profileName
        profile.lastVersionId = versionId

        LauncherProfiles.mainProfileJson.profiles[modpackName] = profile
        LauncherProfiles.write(currentProfile)
    }

    @JvmStatic
    fun verifyManifest(manifest: CurseManifest): Boolean { //检测是否为curseforge整合包(通过manifest.json内的数据进行判断)
        if ("minecraftModpack" != manifest.manifestType) return false
        if (manifest.manifestVersion != 1) return false
        if (manifest.minecraft == null) return false
        if (manifest.minecraft.version == null) return false
        if (manifest.minecraft.modLoaders == null) return false
        return manifest.minecraft.modLoaders.isNotEmpty()
    }

    @JvmStatic
    fun verifyModrinthIndex(modrinthIndex: ModrinthIndex): Boolean { //检测是否为modrinth整合包(通过modrinth.index.json内的数据进行判断)
        if ("minecraft" != modrinthIndex.game) return false
        if (modrinthIndex.formatVersion != 1) return false
        return modrinthIndex.dependencies != null
    }

    fun verifyMCBBSPackMeta(mcbbsPackMeta: MCBBSPackMeta): Boolean { //检测是否为MCBBS整合包(通过mcbbs.packmeta内的数据进行判断)
        if ("minecraftModpack" != mcbbsPackMeta.manifestType) return false
        if (mcbbsPackMeta.manifestVersion != 2) return false
        if (mcbbsPackMeta.addons == null) return false
        if (mcbbsPackMeta.addons[0].id == null) return false
        return (mcbbsPackMeta.addons[0].version != null)
    }

    enum class ModPackEnum {
        UNKNOWN, CURSEFORGE, MCBBS, MODRINTH
    }
}
