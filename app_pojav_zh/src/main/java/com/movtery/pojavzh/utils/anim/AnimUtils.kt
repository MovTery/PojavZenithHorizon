package com.movtery.pojavzh.utils.anim

import android.view.View
import net.kdt.pojavlaunch.PojavApplication

object AnimUtils {
    @JvmStatic
    fun setVisibilityAnim(view: View, shouldShow: Boolean) {
        setVisibilityAnim(view, shouldShow, 300, null)
    }

    @JvmStatic
    fun setVisibilityAnim(view: View, shouldShow: Boolean, listener: AnimationListener?) {
        setVisibilityAnim(view, shouldShow, 300, listener)
    }

    @JvmStatic
    fun setVisibilityAnim(view: View, shouldShow: Boolean, duration: Int) {
        setVisibilityAnim(view, shouldShow, duration, null)
    }

    @JvmStatic
    fun setVisibilityAnim(
        view: View,
        shouldShow: Boolean,
        duration: Int,
        listener: AnimationListener?
    ) {
        setVisibilityAnim(view, 0, shouldShow, duration, listener)
    }

    /**
     * 用于便捷地使用隐藏动画
     * @param view 需要操作的控件
     * @param startDelay 开始前的延迟
     * @param shouldShow true: 显示，false: 隐藏
     * @param duration 持续时间
     * @param listener 动画监听器，用于调用动画开始前和结束的回调
     */
    @JvmStatic
    fun setVisibilityAnim(
        view: View,
        startDelay: Int,
        shouldShow: Boolean,
        duration: Int,
        listener: AnimationListener?
    ) {
        listener?.onStart()

        if (shouldShow && view.visibility != View.VISIBLE) {
            fadeAnim(view, startDelay.toLong(), 0f, 1f, duration) {
                view.visibility = View.VISIBLE
                listener?.onEnd()
            }
        } else if (!shouldShow && view.visibility != View.GONE) {
            fadeAnim(view, startDelay.toLong(), view.alpha, 0f, duration) {
                view.visibility = View.GONE
                listener?.onEnd()
            }
        }
    }

    /**
     * 用于便捷地使用渐隐渐显动画
     * @param view 需要操作的控件
     * @param startDelay 开始前的延迟
     * @param begin 开始的透明度（Alpha）
     * @param end 结束的透明度（Alpha）
     * @param duration 持续时间
     * @param endAction 动画结束时执行的任务
     */
    @JvmStatic
    fun fadeAnim(
        view: View,
        startDelay: Long,
        begin: Float,
        end: Float,
        duration: Int,
        endAction: Runnable?
    ) {
        if ((view.visibility != View.VISIBLE && end == 0f) || (view.visibility == View.VISIBLE && end == 1f)) {
            endAction?.let { PojavApplication.sExecutorService.execute(endAction) }
            return
        }
        view.visibility = View.VISIBLE
        view.alpha = begin
        view.animate()
            .alpha(end)
            .setStartDelay(startDelay)
            .setDuration(duration.toLong())
            .withEndAction(endAction)
    }

    interface AnimationListener {
        fun onStart()
        fun onEnd()
    }
}
