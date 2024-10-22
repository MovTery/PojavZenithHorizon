package com.movtery.zalithlauncher.feature.background

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.utils.PathAndUrlManager
import com.movtery.zalithlauncher.utils.file.FileTools.Companion.mkdirs
import com.movtery.zalithlauncher.utils.image.ImageUtils.Companion.isImage
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

object BackgroundManager {
    private val FILE_BACKGROUND_PROPERTIES: File = File(PathAndUrlManager.DIR_DATA, "background.properties")

    private val defaultProperties: Properties
        get() {
            val properties = Properties()
            properties.setProperty(BackgroundType.MAIN_MENU.name, "null")
            properties.setProperty(BackgroundType.CUSTOM_CONTROLS.name, "null")
            properties.setProperty(BackgroundType.IN_GAME.name, "null")
            return properties
        }

    val properties: Properties
        get() {
            FILE_BACKGROUND_PROPERTIES.apply {
                if (!exists()) {
                    return@apply
                }

                val properties = Properties()
                runCatching {
                    FileReader(this).use { fileReader ->
                        properties.load(fileReader)
                    }
                }.getOrElse { e ->
                    Logging.e("BackgroundManager", Tools.printToString(e))
                    return@apply
                }

                return properties
            }

            return defaultProperties
        }

    @JvmStatic
    fun setBackgroundImage(
        context: Context,
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

    fun getBackgroundImage(backgroundType: BackgroundType): File? {
        val pngName = properties[backgroundType.name] as String?
        if (pngName == null || pngName == "null") return null

        val backgroundImage = File(PathAndUrlManager.DIR_BACKGROUND, pngName)
        if (!backgroundImage.exists() || !isImage(backgroundImage)) return null
        return backgroundImage
    }

    private fun saveProperties(properties: Properties) {
        PathAndUrlManager.DIR_BACKGROUND.apply {
            if (!exists()) mkdirs(this)
        }

        runCatching {
            properties.store(
                FileWriter(FILE_BACKGROUND_PROPERTIES),
                "Zalith Launcher Background Properties File"
            )
        }.getOrElse { e -> Logging.e("saveProperties", Tools.printToString(e)) }
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