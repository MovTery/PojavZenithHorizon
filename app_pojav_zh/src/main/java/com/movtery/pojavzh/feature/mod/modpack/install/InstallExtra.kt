package com.movtery.pojavzh.feature.mod.modpack.install;

import android.app.AlertDialog;

public class InstallExtra {
    public boolean startInstall;
    public String modpackPath;
    public AlertDialog dialog;

    public InstallExtra(boolean startInstall, String modpackPath, AlertDialog dialog) {
        this.startInstall = startInstall;
        this.modpackPath = modpackPath;
        this.dialog = dialog;
    }
}
