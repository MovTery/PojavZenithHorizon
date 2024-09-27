package com.movtery.pojavzh.feature.background

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.Companion.isImage
import net.kdt.pojavlaunch.R
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

class BackgroundManager private constructor(val context: Context) {
    companion object {
        private val FILE_BACKGROUND_PROPERTIES: File = File(PathAndUrlManager.DIR_GAME_HOME, "background.properties")
        @SuppressLint("StaticFieldLeak")
        private var sBackgroundManager: BackgroundManager? = null

        @JvmStatic
        fun setContext(context: Context) {
            sBackgroundManager = BackgroundManager(context)
        }

        @JvmStatic
        fun getInstance() = sBackgroundManager
    }

    private val defaultProperties: Properties
        get() {
            val properties = Properties()
            properties.setProperty(BackgroundType.MAIN_MENU.name, "null")
            properties.setProperty(BackgroundType.CUSTOM_CONTROLS.name, "null")
            properties.setProperty(BackgroundType.IN_GAME.name, "null")

            saveProperties(properties)
            return properties
        }

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

    fun setBackgroundImage(
        backgroundType: BackgroundType,
        backgroundView: ImageView
    ) {
        backgroundView.setImageDrawable(
            ContextCompat.getDrawable(context, R.color.background_app)
        )

        val backgroundImage = getBackgroundImage(backgroundType) ?: return

        Glide.with(context).load(backgroundImage)
            .override(backgroundView.width, backgroundView.height)
            .centerCrop()
            .into(DrawableImageViewTarget(backgroundView))
    }

    private fun getBackgroundImage(backgroundType: BackgroundType): File? {
        val pngName = properties[backgroundType.name] as String?
        if (pngName == null || pngName == "null") return null

        val backgroundImage = File(PathAndUrlManager.DIR_BACKGROUND, pngName)
        if (!backgroundImage.exists() || !isImage(backgroundImage)) return null
        return backgroundImage
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

    fun saveProperties(map: Map<BackgroundType?, String?>) {
        val properties = Properties()
        properties.setProperty(
            BackgroundType.MAIN_MENU.name,
            map[BackgroundType.MAIN_MENU] ?: "null"
        )
        properties.setProperty(
            BackgroundType.CUSTOM_CONTROLS.name,
            map[BackgroundType.CUSTOM_CONTROLS] ?: "null"
        )
        properties.setProperty(
            BackgroundType.IN_GAME.name,
            map[BackgroundType.IN_GAME] ?: "null"
        )

        saveProperties(properties)
    }
}