package com.movtery.pojavzh.ui.subassembly.view;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class DraggableView {
    private final View mainView;
    private final AttributesFetcher fetcher;
    private long lastUpdateTime = 0;
    private float initialX;
    private float initialY;
    private float touchX;
    private float touchY;

    public DraggableView(View mainView, @NonNull AttributesFetcher fetcher) {
        this.mainView = mainView;
        this.fetcher = fetcher;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        mainView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (updateRateLimits()) return false;

                    initialX = fetcher.get()[0];
                    initialY = fetcher.get()[1];
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (updateRateLimits()) return false;

                    int x = (int) Math.max(-fetcher.getScreenPixels()[0], Math.min(fetcher.getScreenPixels()[0], initialX + (event.getRawX() - touchX)));
                    int y = (int) Math.max(-fetcher.getScreenPixels()[1], Math.min(fetcher.getScreenPixels()[1], initialY + (event.getRawY() - touchY)));
                    fetcher.set(x, y);
                    return true;
            }
            return false;
        });
    }

    //避免过于频繁的更新导致的性能开销
    private boolean updateRateLimits() {
        boolean limit = false;
        long millis = System.currentTimeMillis();
        if (millis - lastUpdateTime < 5) limit = true;
        lastUpdateTime = millis;
        return limit;
    }

    public interface AttributesFetcher {
        int[] getScreenPixels(); //获取对应的屏幕的高宽限制值
        int[] get(); //获取x, y值
        void set(int x, int y);
    }
}
