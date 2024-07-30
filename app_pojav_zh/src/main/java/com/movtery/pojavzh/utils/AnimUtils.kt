package com.movtery.pojavzh.utils;

import android.view.View;

import net.kdt.pojavlaunch.PojavApplication;

public class AnimUtils {
    public static void setVisibilityAnim(View view, boolean shouldShow) {
        setVisibilityAnim(view, shouldShow, 300, null);
    }

    public static void setVisibilityAnim(View view, boolean shouldShow, AnimationListener listener) {
        setVisibilityAnim(view, shouldShow, 300, listener);
    }

    public static void setVisibilityAnim(View view, boolean shouldShow, int duration) {
        setVisibilityAnim(view, shouldShow, duration, null);
    }

    public static void setVisibilityAnim(View view, boolean shouldShow, int duration, AnimationListener listener) {
        setVisibilityAnim(view, 0, shouldShow, duration, listener);
    }

    /**
     * 用于便捷地使用隐藏动画
     * @param view 需要操作的控件
     * @param startDelay 开始前的延迟
     * @param shouldShow true: 显示，false: 隐藏
     * @param duration 持续时间
     * @param listener 动画监听器，用于调用动画开始前和结束的回调
     */
    public static void setVisibilityAnim(View view, int startDelay, boolean shouldShow, int duration, AnimationListener listener) {
        if (listener != null) listener.onStart();

        if (shouldShow && view.getVisibility() != View.VISIBLE) {
            fadeAnim(view, startDelay, 0f, 1f, duration, () -> {
                view.setVisibility(View.VISIBLE);
                if (listener != null) listener.onEnd();
            });
        } else if (!shouldShow && view.getVisibility() != View.GONE) {
            fadeAnim(view, startDelay, view.getAlpha(), 0f, duration, () -> {
                view.setVisibility(View.GONE);
                if (listener != null) listener.onEnd();
            });
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
    public static void fadeAnim(View view, long startDelay, float begin, float end, int duration, Runnable endAction) {
        if ((view.getVisibility() != View.VISIBLE && end == 0) || (view.getVisibility() == View.VISIBLE && end == 1)) {
            if (endAction != null) PojavApplication.sExecutorService.execute(endAction);
            return;
        }
        view.setVisibility(View.VISIBLE);
        view.setAlpha(begin);
        view.animate()
                .alpha(end)
                .setStartDelay(startDelay)
                .setDuration(duration)
                .withEndAction(endAction);
    }

    public interface AnimationListener {
        void onStart();
        void onEnd();
    }
}
