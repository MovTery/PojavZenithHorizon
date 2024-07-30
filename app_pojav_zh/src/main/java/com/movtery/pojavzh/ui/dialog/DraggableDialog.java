package com.movtery.pojavzh.ui.dialog;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.movtery.pojavzh.ui.subassembly.view.DraggableView;

public abstract class DraggableDialog {

    public static void initDialog(DialogInitializationListener listener) {
        Window window = listener.onInit();
        if (window != null) {
            View contentView = window.findViewById(android.R.id.content);
            if (contentView != null) {
                DraggableView draggableView = new DraggableView(contentView, new DraggableView.AttributesFetcher() {
                    @Override
                    public DraggableView.ScreenPixels getScreenPixels() {
                        int width = (currentDisplayMetrics.widthPixels - contentView.getWidth()) / 2;
                        int height = (currentDisplayMetrics.heightPixels - contentView.getHeight()) / 2;
                        return new DraggableView.ScreenPixels(-width, -height, width, height);
                    }

                    @Override
                    public int[] get() {
                        WindowManager.LayoutParams attributes = window.getAttributes();
                        return new int[]{attributes.x, attributes.y};
                    }

                    @Override
                    public void set(int x, int y) {
                        WindowManager.LayoutParams attributes = window.getAttributes();
                        attributes.x = x;
                        attributes.y = y;
                        window.setAttributes(attributes);
                    }
                });

                draggableView.init();
            } else {
                Log.w("DraggableDialog", "The content view does not exist!");
            }
        }
    }

    public interface DialogInitializationListener {
        Window onInit();
    }
}
