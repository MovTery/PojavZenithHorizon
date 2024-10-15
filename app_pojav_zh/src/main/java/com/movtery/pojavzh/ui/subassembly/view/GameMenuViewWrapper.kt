package com.movtery.pojavzh.ui.subassembly.view

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.setting.Settings
import com.movtery.pojavzh.utils.file.FileTools.Companion.formatFileSize
import com.movtery.pojavzh.utils.platform.MemoryUtils
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.listener.IFxViewLifecycle
import com.petterp.floatingx.util.createFx
import com.petterp.floatingx.view.FxViewHolder
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import java.util.Timer
import java.util.TimerTask

class GameMenuViewWrapper(
    private val activity: Activity,
    private val listener: View.OnClickListener
) {
    private var timer: Timer? = null
    private var showMemory: Boolean = false

    private val scopeFx by createFx {
        setLayout(R.layout.view_game_menu)
        setOnClickListener(0L, listener)
        setOnLongClickListener {
            showMemory = !showMemory
            Settings.Manager.put("gameMenuShowMemory", showMemory).save()
            setShowMemory()
            true
        }
        setEnableEdgeAdsorption(false)
        addViewLifecycle(object : IFxViewLifecycle {
            override fun initView(holder: FxViewHolder) {
                holder.view.alpha = (AllSettings.gameMenuAlpha.toFloat() / 100f).toFloat()
                showMemory = AllSettings.gameMenuShowMemory

                holder.getView<TextView>(R.id.memory_text).apply {
                    updateMemoryText(this)
                }
            }

            override fun detached(view: View) {
                cancelMemoryTimer()
            }
        })
        setGravity(getCurrentGravity())
        build().toControl(activity)
    }

    fun setVisibility(visible: Boolean) {
        if (visible) {
            setShowMemory()
            scopeFx.show()
        } else {
            scopeFx.hide()
            cancelMemoryTimer()
        }
    }

    fun setShowMemory() {
        scopeFx.getView()?.findViewById<TextView>(R.id.memory_text)?.apply {
            updateMemoryText(this)
        }
    }

    private fun updateMemoryText(memoryText: TextView) {
        cancelMemoryTimer()

        memoryText.apply {
            visibility = if (showMemory) {
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        val memoryText =
                            "${AllSettings.gameMenuMemoryText} ${formatFileSize(MemoryUtils.getUsedDeviceMemory(activity))}/${
                                formatFileSize(MemoryUtils.getTotalDeviceMemory(activity))
                            }".trim()
                        Tools.runOnUiThread {
                            text = memoryText
                        }
                    }
                }, 0, 2000)
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun cancelMemoryTimer() {
        timer?.cancel()
        timer = null
    }

    private fun getCurrentGravity(): FxGravity {
        return when(AllSettings.gameMenuLocation) {
            "left_or_top" -> FxGravity.LEFT_OR_TOP
            "left_or_bottom" -> FxGravity.LEFT_OR_BOTTOM
            "right_or_top" -> FxGravity.RIGHT_OR_TOP
            "right_or_bottom" -> FxGravity.RIGHT_OR_BOTTOM
            "top_or_center" -> FxGravity.TOP_OR_CENTER
            "bottom_or_center" -> FxGravity.BOTTOM_OR_CENTER
            "center" -> FxGravity.CENTER
            else -> FxGravity.CENTER
        }
    }
}