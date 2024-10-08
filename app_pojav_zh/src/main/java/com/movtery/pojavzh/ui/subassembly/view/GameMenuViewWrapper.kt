package com.movtery.pojavzh.ui.subassembly.view

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import com.movtery.pojavzh.utils.platform.MemoryUtils
import com.petterp.floatingx.util.createFx
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.Tools.currentDisplayMetrics
import java.util.Timer
import java.util.TimerTask

class GameMenuViewWrapper(private val activity: Activity, private val listener: View.OnClickListener) {
    private var timer: Timer? = null
    private var showMemory: Boolean = false

    private val scopeFx by createFx {
        setLayout(R.layout.view_game_menu)
        setOnClickListener(0L, listener)
        setOnLongClickListener {
            showMemory = !showMemory
            setShowMemory(showMemory)
            true
        }
        setEnableEdgeAdsorption(false)
        setX((currentDisplayMetrics.widthPixels / 2).toFloat())
        setY((currentDisplayMetrics.heightPixels / 2).toFloat())
        build().toControl(activity)
    }

    fun setVisibility(visible: Boolean) {
        if (visible) {
            setShowMemory(showMemory)
            scopeFx.show()
        } else {
            scopeFx.hide()
            cancelMemoryTimer()
        }
    }

    fun setShowMemory(show: Boolean) {
        scopeFx.getView()?.findViewById<TextView>(R.id.memory_text)?.apply {
            visibility = if (show) {
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        val memoryText = "M: ${formatFileSize(MemoryUtils.getUsedDeviceMemory(activity))}/${formatFileSize(MemoryUtils.getTotalDeviceMemory(activity))}"
                        Tools.runOnUiThread {
                            text = memoryText
                        }
                    }
                }, 0, 2000)
                View.VISIBLE
            } else {
                cancelMemoryTimer()
                View.GONE
            }
        }
    }

    private fun cancelMemoryTimer() {
        timer?.cancel()
        timer = null
    }
}