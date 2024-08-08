package com.movtery.pojavzh.ui.subassembly.background

import android.graphics.drawable.Drawable
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

class BackgroundManager {
    companion object {
        private val FILE_BACKGROUND_PROPERTIES: File = File(PathAndUrlManager.DIR_GAME_HOME, "background.properties")
        private val backgroundDrawable: MutableMap<String, Drawable?> = ConcurrentHashMap()

        @JvmStatic
        fun getBackgroundDrawable(name: String, imageFile: File): Drawable? {
            val hasDrawable = backgroundDrawable.containsKey(name)
            if (hasDrawable) {
                return backgroundDrawable[name]
            } else {
                try {
                    val drawable = Drawable.createFromPath(imageFile.absolutePath)
                    backgroundDrawable[name] = drawable
                    return drawable
                } catch (e: Exception) {
                    return null
                }
            }
        }

        @JvmStatic
        val properties: Properties
            get() {
                if (!FILE_BACKGROUND_PROPERTIES.exists()) {
                    return defaultProperties
                }
                val properties = Properties()
                try {
                    FileReader(FILE_BACKGROUND_PROPERTIES).use { fileReader ->
                        properties.load(fileReader)
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

                return properties
            }

        private val defaultProperties: Properties
            get() {
                val properties = Properties()
                properties.setProperty(BackgroundType.MAIN_MENU.name, "null")
                properties.setProperty(BackgroundType.SETTINGS.name, "null")
                properties.setProperty(BackgroundType.CUSTOM_CONTROLS.name, "null")
                properties.setProperty(BackgroundType.IN_GAME.name, "null")

                saveProperties(properties)
                return properties
            }

        private fun saveProperties(properties: Properties) {
            val dirBackground = PathAndUrlManager.DIR_BACKGROUND
            if (!dirBackground!!.exists()) mkdirs(dirBackground)

            try {
                properties.store(
                    FileWriter(FILE_BACKGROUND_PROPERTIES),
                    "Pojav Zenith Horizon Background Properties File"
                )
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun saveProperties(map: Map<BackgroundType?, String?>) {
            val properties = Properties()
            properties.setProperty(
                BackgroundType.MAIN_MENU.name,
                (if (map[BackgroundType.MAIN_MENU] == null) "null" else map[BackgroundType.MAIN_MENU])
            )
            properties.setProperty(
                BackgroundType.SETTINGS.name,
                (if (map[BackgroundType.SETTINGS] == null) "null" else map[BackgroundType.SETTINGS])
            )
            properties.setProperty(
                BackgroundType.CUSTOM_CONTROLS.name,
                (if (map[BackgroundType.CUSTOM_CONTROLS] == null) "null" else map[BackgroundType.CUSTOM_CONTROLS])
            )
            properties.setProperty(
                BackgroundType.IN_GAME.name,
                (if (map[BackgroundType.IN_GAME] == null) "null" else map[BackgroundType.IN_GAME])
            )

            saveProperties(properties)
        }
    }
}
