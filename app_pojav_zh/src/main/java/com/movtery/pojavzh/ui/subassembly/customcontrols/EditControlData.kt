package com.movtery.pojavzh.ui.subassembly.customcontrols

import android.content.Context
import com.movtery.pojavzh.feature.log.Logging
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.customcontrols.CustomControls
import net.kdt.pojavlaunch.customcontrols.LayoutConverter
import org.json.JSONObject
import java.io.File
import java.io.IOException

class EditControlData {
    companion object {
        @JvmStatic
        fun loadFormFile(context: Context?, file: File): ControlInfoData? {
            val customControls = loadCustomControlsFromFile(context, file)
            customControls?.let {
                val mControlInfoDataList = customControls.mControlInfoDataList
                mControlInfoDataList.fileName = file.name
                return mControlInfoDataList
            }
            return null
        }

        @JvmStatic
        fun loadCustomControlsFromFile(context: Context?, file: File): CustomControls? {
            runCatching {
                val jsonLayoutData = Tools.read(file)
                val layoutJsonObject = JSONObject(jsonLayoutData)
                return LayoutConverter.loadFromJsonObject(context, layoutJsonObject, jsonLayoutData, file.absolutePath, false)
            }.getOrElse { e ->
                Logging.e("Load Controls", Tools.printToString(e))
                return null
            }
        }

        @JvmStatic
        fun saveToFile(context: Context?, customControls: CustomControls?, file: File) {
            runCatching {
                Tools.write(file.absolutePath, Tools.GLOBAL_GSON.toJson(customControls))
            }.getOrElse { e ->
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
}
