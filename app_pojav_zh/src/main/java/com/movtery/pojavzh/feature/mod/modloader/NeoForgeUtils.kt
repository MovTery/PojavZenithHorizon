package com.movtery.pojavzh.feature.mod.modloader

import android.content.Intent
import net.kdt.pojavlaunch.modloaders.ForgeVersionListHandler
import net.kdt.pojavlaunch.utils.DownloadUtils
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

class NeoForgeUtils {
    companion object {
        private const val NEOFORGE_METADATA_URL =
            "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"
        private const val NEOFORGE_INSTALLER_URL =
            "https://maven.neoforged.net/releases/net/neoforged/neoforge/%1\$s/neoforge-%1\$s-installer.jar"
        private const val NEOFORGED_FORGE_METADATA_URL =
            "https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml"
        private const val NEOFORGED_FORGE_INSTALLER_URL =
            "https://maven.neoforged.net/releases/net/neoforged/forge/%1\$s/forge-%1\$s-installer.jar"

        @Throws(Exception::class)
        private fun downloadVersions(metaDataUrl: String, name: String, force: Boolean): List<String> {
            val parserFactory = SAXParserFactory.newInstance()
            val saxParser = parserFactory.newSAXParser()

            return DownloadUtils.downloadStringCached<List<String>>(
                metaDataUrl,
                name,
                force,
            ) { input: String? ->
                try {
                    val handler = ForgeVersionListHandler()
                    saxParser.parse(InputSource(StringReader(input)), handler)
                    return@downloadStringCached handler.versions
                    // IOException is present here StringReader throws it only if the parser called close()
                    // sooner than needed, which is a parser issue and not an I/O one
                } catch (e: SAXException) {
                    throw DownloadUtils.ParseException(e)
                } catch (e: IOException) {
                    throw DownloadUtils.ParseException(e)
                }
            }
        }

        @JvmStatic
        @Throws(Exception::class)
        fun downloadNeoForgeVersions(force: Boolean): List<String> {
            return downloadVersions(NEOFORGE_METADATA_URL, "neoforge_versions", force)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun downloadNeoForgedForgeVersions(force: Boolean): List<String> {
            return downloadVersions(NEOFORGED_FORGE_METADATA_URL, "neoforged_forge_versions", force)
        }

        @JvmStatic
        fun getNeoForgeInstallerUrl(version: String?): String {
            return String.format(NEOFORGE_INSTALLER_URL, version)
        }

        @JvmStatic
        fun getNeoForgedForgeInstallerUrl(version: String?): String {
            return String.format(NEOFORGED_FORGE_INSTALLER_URL, version)
        }

        @JvmStatic
        fun addAutoInstallArgs(intent: Intent, modInstallerJar: File) {
            intent.putExtra("javaArgs", "-jar " + modInstallerJar.absolutePath)
        }

        @JvmStatic
        fun formatGameVersion(neoForgeVersion: String): String {
            val originalGameVersion =
                neoForgeVersion.substring(0, 4) //例neoForgeVersion = 21.0.xxx : 21.0
            return if (originalGameVersion[originalGameVersion.length - 1] == '0') { //例21.0.xxx
                "1." + originalGameVersion.substring(0, 2)
            } else { //例20.2.xxx
                "1.$originalGameVersion"
            }
        }
    }
}