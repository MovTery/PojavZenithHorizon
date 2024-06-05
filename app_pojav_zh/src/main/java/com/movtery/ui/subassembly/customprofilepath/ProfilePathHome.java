package com.movtery.ui.subassembly.customprofilepath;

public class ProfilePathHome {
    public static String getGameHome() {
        return ProfilePathManager.getCurrentPath() + "/.minecraft";
    }

    public static String getVersionsHome() {
        return getGameHome() + "/versions";
    }

    public static String getLibrariesHome() {
        return getGameHome() + "/libraries";
    }

    public static String getAssetsHome() {
        return getGameHome() + "/assets";
    }

    public static String getResourcesHome() {
        return getGameHome() + "/resources";
    }
}
