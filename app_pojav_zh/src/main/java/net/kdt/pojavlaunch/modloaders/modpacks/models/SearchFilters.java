package net.kdt.pojavlaunch.modloaders.modpacks.models;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Search filters, passed to APIs
 */
public class SearchFilters {
    public boolean isModpack;
    public String name;
    @Nullable public String mcVersion;
    public List<String> modloaders = new ArrayList<>();
    public int sort = 0;
    public ApiPlatform platform = ApiPlatform.BOTH;

    public enum ApiPlatform {
        CURSEFORGE,
        MODRINTH,
        BOTH
    }
}
