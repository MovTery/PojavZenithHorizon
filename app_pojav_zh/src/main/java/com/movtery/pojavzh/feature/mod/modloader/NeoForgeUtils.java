package com.movtery.pojavzh.feature.mod.modloader;

import android.content.Intent;

import net.kdt.pojavlaunch.modloaders.ForgeVersionListHandler;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class NeoForgeUtils {
    private static final String NEOFORGE_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
    private static final String NEOFORGE_INSTALLER_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%1$s/neoforge-%1$s-installer.jar";
    private static final String NEOFORGED_FORGE_METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/maven-metadata.xml";
    private static final String NEOFORGED_FORGE_INSTALLER_URL = "https://maven.neoforged.net/releases/net/neoforged/forge/%1$s/forge-%1$s-installer.jar";

    private static List<String> downloadVersions(String metaDataUrl, String name) throws Exception {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = parserFactory.newSAXParser();

        return DownloadUtils.downloadStringCached(metaDataUrl, name, input -> {
            try {
                ForgeVersionListHandler handler = new ForgeVersionListHandler();
                saxParser.parse(new InputSource(new StringReader(input)), handler);
                return handler.getVersions();
                // IOException is present here StringReader throws it only if the parser called close()
                // sooner than needed, which is a parser issue and not an I/O one
            } catch (SAXException | IOException e) {
                throw new DownloadUtils.ParseException(e);
            }
        });
    }

    public static List<String> downloadNeoForgeVersions() throws Exception {
        return downloadVersions(NEOFORGE_METADATA_URL, "neoforge_versions");
    }

    public static List<String> downloadNeoForgedForgeVersions() throws Exception {
        return downloadVersions(NEOFORGED_FORGE_METADATA_URL, "neoforged_forge_versions");
    }

    public static String getNeoForgeInstallerUrl(String version) {
        return String.format(NEOFORGE_INSTALLER_URL, version);
    }

    public static String getNeoForgedForgeInstallerUrl(String version) {
        return String.format(NEOFORGED_FORGE_INSTALLER_URL, version);
    }

    public static void addAutoInstallArgs(Intent intent, File modInstallerJar) {
        intent.putExtra("javaArgs", "-jar " + modInstallerJar.getAbsolutePath());
    }

    public static String formatGameVersion(String neoForgeVersion) {
        String originalGameVersion = neoForgeVersion.substring(0, 4); //例neoForgeVersion = 21.0.xxx : 21.0
        if (originalGameVersion.charAt(originalGameVersion.length() - 1) == '0') { //例21.0.xxx
            return "1." + originalGameVersion.substring(0, 2);
        } else { //例20.2.xxx
            return "1." + originalGameVersion;
        }
    }
}