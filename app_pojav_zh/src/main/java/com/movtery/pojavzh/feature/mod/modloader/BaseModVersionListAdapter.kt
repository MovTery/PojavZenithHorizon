package com.movtery.pojavzh.feature.mod.modloader

import android.content.Context
import android.widget.Toast
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener

class BaseModVersionListAdapter(
    private val context: Context,
    private val modloaderListenerProxy: ModloaderListenerProxy,
    private val listener: ModloaderDownloadListener,
    icon: Int,
    mData: List<*>?
) : ModVersionListAdapter(mData), TaskCountListener {
    private var mTasksRunning = false

    init {
        ProgressKeeper.addTaskCountListener(this)
        setIconDrawable(icon)
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        if (mTasksRunning) {
            Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT)
                .show()
            return
        }
        modloaderListenerProxy.attachListener(this.listener)

        super.setOnItemClickListener(listener)
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }
}