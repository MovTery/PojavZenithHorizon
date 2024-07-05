package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class DraggableDialog extends Dialog {
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
            View decorView = getWindow().getDecorView();
            decorView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = window.getAttributes().x;
                        initialY = window.getAttributes().y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.x = (int) (initialX + (event.getRawX() - touchX));
                        params.y = (int) (initialY + (event.getRawY() - touchY));
                        window.setAttributes(params);
                        return true;
                }
                return false;
            });
        }
    }
}
