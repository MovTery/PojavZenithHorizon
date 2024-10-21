package com.movtery.pojavzh.feature.download

import com.movtery.pojavzh.feature.download.item.DependenciesInfoItem
import com.movtery.pojavzh.feature.download.item.ModLikeVersionItem
import com.movtery.pojavzh.feature.download.item.ModVersionItem
import com.movtery.pojavzh.feature.download.item.VersionItem
import net.kdt.pojavlaunch.modloaders.modpacks.api.ApiHandler

class InfoCache {
    abstract class CacheBase<V> {
        //需要传入api对象，以防止不同api的Mod的id重叠的问题
        private val cache: MutableMap<ApiHandler, MutableMap<String, V>> = HashMap()

        fun put(api: ApiHandler, modId: String, value: V) {
            cache.getOrPut(api) { HashMap() }[modId] = value
        }

        fun get(api: ApiHandler, modId: String): V? {
            return cache.getOrPut(api) { HashMap() }[modId]
        }

        fun containsKey(api: ApiHandler, modId: String): Boolean {
            return cache.getOrPut(api) { HashMap() }.containsKey(modId)
        }
    }

    object DependencyInfoCache : CacheBase<DependenciesInfoItem>()
    object VersionCache : CacheBase<MutableList<VersionItem>>()
    object ModVersionCache : CacheBase<MutableList<ModVersionItem>>()
    object ModPackVersionCache : CacheBase<MutableList<ModLikeVersionItem>>()
}