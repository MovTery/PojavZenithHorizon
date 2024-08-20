package com.movtery.pojavzh.feature.mod

import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

class ModCache {
    abstract class CacheBase<V> {
        //需要传入api对象，以防止不同api的Mod的id重叠的问题
        private val cache: MutableMap<ModpackApi, MutableMap<String, V>> = HashMap()

        fun put(api: ModpackApi, modId: String, value: V) {
            cache.getOrPut(api) { HashMap() }[modId] = value
        }

        fun get(api: ModpackApi, modId: String): V? {
            return cache.getOrPut(api) { HashMap() }[modId]
        }

        fun containsKey(api: ModpackApi, modId: String): Boolean {
            return cache.getOrPut(api) { HashMap() }.containsKey(modId)
        }
    }

    object ModInfoCache : CacheBase<MutableList<ModVersionItem>>()
    object ModItemCache : CacheBase<ModItem>()
}