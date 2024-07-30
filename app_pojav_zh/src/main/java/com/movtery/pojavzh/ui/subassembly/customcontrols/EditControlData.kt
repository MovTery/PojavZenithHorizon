package com.movtery.pojavzh.ui.subassembly.customcontrols

import android.content.Context
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.customcontrols.CustomControls
import net.kdt.pojavlaunch.customcontrols.LayoutConverter
import org.json.JSONObject
import java.io.File
import java.io.IOException

object EditControlData {
    @JvmStatic
    fun loadFormFile(context: Context?, file: File): ControlInfoData? {
        val customControls = loadCustomControlsFromFile(context, file)
        if (customControls != null) {
            val mControlInfoDataList = customControls.mControlInfoDataList
            mControlInfoDataList.fileName = file.name
            return mControlInfoDataList
        }
        return null
    }

    @JvmStatic
    fun loadCustomControlsFromFile(context: Context?, file: File): CustomControls? {
        try {
            val jsonLayoutData = Tools.read(file)
            val layoutJsonObject = JSONObject(jsonLayoutData)
            return LayoutConverter.loadFromJsonObject(context, layoutJsonObject, jsonLayoutData, file.absolutePath, false)
        } catch (ignored: Exception) {
            return null
        }
    }

    @JvmStatic
    fun saveToFile(context: Context?, customControls: CustomControls?, file: File) {
        try {
            Tools.write(file.absolutePath, Tools.GLOBAL_GSON.toJson(customControls))
        } catch (e: IOException) {
            Tools.showError(context, e)
        }
    }

    @JvmStatic
    fun createNewControlFile(context: Context?, jsonFile: File, mControlInfoDataList: ControlInfoData?) {
        val customControls =
            CustomControls(ArrayList(), ArrayList(), ArrayList(), mControlInfoDataList)
        saveToFile(context, customControls, jsonFile)
    }
}
