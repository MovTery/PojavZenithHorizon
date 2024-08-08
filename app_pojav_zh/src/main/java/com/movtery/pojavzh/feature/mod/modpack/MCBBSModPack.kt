package com.movtery.pojavzh.feature.mod.modpack

import android.content.Context
import android.util.Log
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager.Companion.currentProfile
import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta
import com.movtery.pojavzh.feature.mod.models.MCBBSPackMeta.MCBBSAddons
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils
import com.movtery.pojavzh.feature.mod.modpack.install.OnInstallStartListener
import com.movtery.pojavzh.ui.dialog.ProgressDialog
import com.movtery.pojavzh.utils.file.FileTools.Companion.getFileHashSHA1
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader
import net.kdt.pojavlaunch.utils.FileUtils
import net.kdt.pojavlaunch.utils.ZipUtils
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipFile

class MCBBSModPack(private val context: Context, private val zipFile: File?) {
    private var installDialog: ProgressDialog? = null
    private var isCanceled = false

    @Throws(IOException::class)
    fun install(instanceDestination: File, onInstallStartListener: OnInstallStartListener?): ModLoader? {
        zipFile?.let {
            ZipFile(this.zipFile).use { modpackZipFile ->
                val mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                    Tools.read(ZipUtils.getEntryStream(modpackZipFile, "mcbbs.packmeta")),
                    MCBBSPackMeta::class.java
                )
                if (!ModPackUtils.verifyMCBBSPackMeta(mcbbsPackMeta)) {
                    Log.i("MCBBSModPack", "manifest verification failed")
                    return null
                }
                onInstallStartListener?.onStart()

                initDialog()

                val overridesDir = "overrides" + File.separatorChar
                val dirNameLen = overridesDir.length

                val fileCounters = AtomicInteger() //文件数量计数
                val length = mcbbsPackMeta.files.size

                for (file in mcbbsPackMeta.files) {
                    if (isCanceled) {
                        cancel(instanceDestination)
                        return null
                    }

                    val entry = modpackZipFile.getEntry(overridesDir + file.path)
                    if (entry != null) {
                        val entryName = entry.name
                        val zipDestination = File(instanceDestination, entryName.substring(dirNameLen))
                        if (zipDestination.exists() && !file.force) continue

                        modpackZipFile.getInputStream(entry).use { inputStream ->
                            val fileHash = getFileHashSHA1(inputStream)
                            val equals = file.hash == fileHash

                            if (equals) {
                                //如果哈希值一致，则复制文件（文件已存在则根据“强制”设定来决定是否覆盖文件）
                                FileUtils.ensureParentDirectory(zipDestination)

                                modpackZipFile.getInputStream(entry).use { entryInputStream ->
                                    Files.newOutputStream(zipDestination.toPath())
                                        .use { outputStream ->
                                            IOUtils.copy(entryInputStream, outputStream)
                                        }
                                }
                                val fileCount = fileCounters.getAndIncrement()
                                Tools.runOnUiThread {
                                    installDialog?.updateText(
                                        context.getString(
                                            R.string.zh_select_modpack_local_installing_files,
                                            fileCount,
                                            length
                                        )
                                    )
                                    installDialog?.updateProgress(
                                        fileCount.toDouble(),
                                        length.toDouble()
                                    )
                                }
                            }
                        }
                    }
                }

                closeDialog()
                return createInfo(mcbbsPackMeta.addons)
            }
        }
        return null
    }

    private fun initDialog() {
        Tools.runOnUiThread {
            installDialog = ProgressDialog(context) {
                isCanceled = true
                true
            }
            installDialog?.show()
        }
    }

    private fun closeDialog() {
        Tools.runOnUiThread { installDialog?.dismiss() }
    }

    private fun cancel(instanceDestination: File) {
        org.apache.commons.io.FileUtils.deleteQuietly(instanceDestination)
    }

    private fun createInfo(addons: Array<MCBBSAddons?>): ModLoader? {
        var version: String? = ""
        var modLoader: String? = ""
        var modLoaderVersion: String? = ""
        for (i in 0..addons.size) {
            if (addons[i]!!.id == "game") {
                version = addons[i]!!.version
                continue
            }
            if (addons[i] != null) {
                modLoader = addons[i]!!.id
                modLoaderVersion = addons[i]!!.version
                break
            }
        }
        val modLoaderTypeInt = when (modLoader) {
            "forge" -> ModLoader.MOD_LOADER_FORGE
            "neoforge" -> ModLoader.MOD_LOADER_NEOFORGE
            "fabric" -> ModLoader.MOD_LOADER_FABRIC
            else -> return null
        }
        return ModLoader(modLoaderTypeInt, modLoaderVersion, version)
    }

    companion object {
        fun createModPackProfiles(modpackName: String, mcbbsPackMeta: MCBBSPackMeta, versionId: String?) {
            val profile = MinecraftProfile()
            profile.gameDir = "./custom_instances/$modpackName"
            profile.name = mcbbsPackMeta.name
            profile.lastVersionId = versionId
            profile.javaArgs = StringUtils.insertSpace(null, *mcbbsPackMeta.launchInfo.javaArgument)

            LauncherProfiles.mainProfileJson.profiles[modpackName] = profile
            LauncherProfiles.write(currentProfile)
        }
    }
}
