package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class DraggableDialog extends Dialog {
    private long lastUpdateTime = 0;
    private float initialX;
    private float initialY;
    private float touchX;
    private float touchY;

    public DraggableDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public DraggableDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    public DraggableDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        Window window = getWindow();
        if (window != null) {
            View contentView = window.findViewById(android.R.id.content);
            if (contentView != null) {
                contentView.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (updateRateLimits()) return false;

                            initialX = window.getAttributes().x;
                            initialY = window.getAttributes().y;
                            touchX = event.getRawX();
                            touchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if (updateRateLimits()) return false;

                            WindowManager.LayoutParams params = window.getAttributes();
                            int screenWidth = (currentDisplayMetrics.widthPixels - contentView.getWidth()) / 2;
                            int screenHeight = (currentDisplayMetrics.heightPixels - contentView.getHeight()) / 2;
                            params.x = (int) Math.max(-screenWidth, Math.min(screenWidth, initialX + (event.getRawX() - touchX)));
                            params.y = (int) Math.max(-screenHeight, Math.min(screenHeight, initialY + (event.getRawY() - touchY)));
                            window.setAttributes(params);
                            return true;
                    }
                    return false;
                });
            } else {
                Log.w("DraggableDialog", "The content view does not exist!");
            }
        }
    }

    //避免过于频繁的更新导致的性能开销
    private boolean updateRateLimits() {
        boolean limit = false;
        long millis = System.currentTimeMillis();
        if (millis - lastUpdateTime < 5) limit = true;
        lastUpdateTime = millis;
        return limit;
    }
}
