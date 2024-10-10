package com.movtery.pojavzh.setting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.movtery.pojavzh.event.single.SettingsChangeEvent
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.utils.PathAndUrlManager
import net.kdt.pojavlaunch.Tools
import org.apache.commons.io.FileUtils
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Type
import java.util.Objects

class Settings {
    companion object {
        private val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

        private var settings: List<SettingAttribute> = refresh()

        private fun refresh(): List<SettingAttribute> {
            return PathAndUrlManager.FILE_SETTINGS.takeIf { it.exists() }?.let { file ->
                try {
                    val jsonString = Tools.read(file)
                    val listType: Type = object : TypeToken<List<SettingAttribute>>() {}.type
                    GSON.fromJson(jsonString, listType)
                } catch (e: Throwable) {
                    Logging.e("Settings", Tools.printToString(e))
                    emptyList()
                }
            } ?: emptyList()
        }

        fun refreshSettings() {
            settings = refresh()
        }
    }

    class Manager {
        companion object {
            private fun <T> getValue(key: String, defaultValue: T, parser: (String) -> T?): T {
                settings.forEach {
                    if (Objects.equals(it.key, key)) {
                        return it.value?.let { value -> parser(value) } ?: defaultValue
                    }
                }
                return defaultValue
            }

            fun getInt(key: String, defaultValue: Int): Int {
                return getValue(key, defaultValue) { it.toIntOrNull() }
            }

            fun getFloat(key: String, defaultValue: Float): Float {
                return getValue(key, defaultValue) { it.toFloatOrNull() }
            }

            fun getDouble(key: String, defaultValue: Double): Double {
                return getValue(key, defaultValue) { it.toDoubleOrNull() }
            }

            fun getLong(key: String, defaultValue: Long): Long {
                return getValue(key, defaultValue) { it.toLongOrNull() }
            }

            fun getBoolean(key: String, defaultValue: Boolean): Boolean {
                return getValue(key, defaultValue) { it.toBoolean() }
            }

            fun getString(key: String, defaultValue: String?): String? {
                return getValue(key, defaultValue) { it }
            }

            fun contains(key: String): Boolean {
                return settings.any { it.key == key }
            }

            fun put(key: String, value: Any?): SettingBuilder =
                SettingBuilder().put(key, value)
        }

        class SettingBuilder {
            private val valueMap: MutableMap<String, Any?> = HashMap()

            fun put(key: String, value: Any?): SettingBuilder {
                valueMap[key] = value
                return this
            }

            fun save() {
                val settingsFile = PathAndUrlManager.FILE_SETTINGS
                if (!settingsFile.exists()) settingsFile.createNewFile()

                val currentSettings = settings.toMutableList()
                val nullValueList: MutableSet<SettingAttribute> = HashSet()

                valueMap.forEach { (key, value) ->
                    val attribute = currentSettings.find { it.key == key }

                    if (value == null) {
                        attribute?.apply { nullValueList.add(this) }
                    } else {
                        if (attribute != null) {
                            attribute.value = value.toString()
                        } else {
                            val newAttribute = SettingAttribute().apply {
                                this.key = key
                                this.value = value.toString()
                            }
                            currentSettings.add(newAttribute)
                        }
                    }
                }

                currentSettings.removeAll(nullValueList)

                val json = GSON.toJson(currentSettings)

                runCatching {
                    FileUtils.write(settingsFile, json)
                    refreshSettings()
                }.getOrElse { e ->
                    Logging.e("SettingBuilder", Tools.printToString(e))
                }

                EventBus.getDefault().post(SettingsChangeEvent())
            }
        }
    }
}
