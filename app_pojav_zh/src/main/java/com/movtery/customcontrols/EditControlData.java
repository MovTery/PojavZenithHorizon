package com.movtery.customcontrols;

import android.content.Context;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.CustomControls;
import net.kdt.pojavlaunch.customcontrols.LayoutConverter;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class EditControlData {

    public static ControlInfoData loadFormFile(Context context, File file) {
        CustomControls customControls = loadCustomControlsFromFile(context, file);
        if (customControls != null) {
            ControlInfoData mControlInfoDataList = customControls.mControlInfoDataList;
            mControlInfoDataList.fileName = file.getName();
            return mControlInfoDataList;
        }
        return null;
    }

    public static CustomControls loadCustomControlsFromFile(Context context, File file) {
        try {
            String jsonLayoutData = Tools.read(file);
            JSONObject layoutJsonObject = new JSONObject(jsonLayoutData);
            return LayoutConverter.loadFromJsonObject(context, layoutJsonObject, jsonLayoutData, file.getAbsolutePath());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void saveToFile(Context context, CustomControls customControls, File file) {
        try {
            Tools.write(file.getAbsolutePath(), Tools.GLOBAL_GSON.toJson(customControls));
        } catch (IOException e) {
            Tools.showError(context, e);
        }
    }
}
