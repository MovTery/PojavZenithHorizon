package com.movtery.pojavzh.ui.subassembly.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.movtery.pojavzh.utils.ZHTools
import kotlin.math.max
import kotlin.math.min

class DraggableViewWrapper(private val mainView: View, private val fetcher: AttributesFetcher) {
    private var lastUpdateTime: Long = 0
    private var initialX = 0f
    private var initialY = 0f
    private var touchX = 0f
    private var touchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        mainView.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (updateRateLimits()) return@setOnTouchListener false

                    initialX = fetcher.get()[0].toFloat()
                    initialY = fetcher.get()[1].toFloat()
                    touchX = event.rawX
                    touchY = event.rawY
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (updateRateLimits()) return@setOnTouchListener false

                    val x = max(fetcher.screenPixels.minX.toDouble(), min(fetcher.screenPixels.maxX.toDouble(),
                            (initialX + (event.rawX - touchX)).toDouble())
                    ).toInt()
                    val y = max(fetcher.screenPixels.minY.toDouble(), min(fetcher.screenPixels.maxY.toDouble(),
                            (initialY + (event.rawY - touchY)).toDouble())
                    ).toInt()
                    fetcher.set(x, y)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    //避免过于频繁的更新导致的性能开销
    private fun updateRateLimits(): Boolean {
        var limit = false
        val millis = ZHTools.getCurrentTimeMillis()
        if (millis - lastUpdateTime < 5) limit = true
        lastUpdateTime = millis
        return limit
    }

    interface AttributesFetcher {
        //获取对应的屏幕的高宽限制值
        val screenPixels: ScreenPixels
        fun get(): IntArray //获取x, y值
        fun set(x: Int, y: Int)
    }

    class ScreenPixels(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)
}
