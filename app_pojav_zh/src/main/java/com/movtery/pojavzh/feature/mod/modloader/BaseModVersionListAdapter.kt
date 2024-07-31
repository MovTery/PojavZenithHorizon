package com.movtery.pojavzh.feature.mod.modloader

import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener

class BaseModVersionListAdapter(
    private val modloaderListenerProxy: ModloaderListenerProxy,
    private val listener: ModloaderDownloadListener,
    icon: Int,
    mData: List<*>?
) : ModVersionListAdapter(mData), TaskCountListener {

    init {
        ProgressKeeper.addTaskCountListener(this)
        setIconDrawable(icon)
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        modloaderListenerProxy.attachListener(this.listener)
        super.setOnItemClickListener(listener)
    }
}