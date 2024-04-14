package net.kdt.pojavlaunch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

public class ResourceManager {
    private static Resources resources;
    @SuppressLint("StaticFieldLeak")
    private static ResourceManager instance;

    private ResourceManager(Context context) {
        resources = context.getResources();
    }

    public static synchronized void setInstance(Context context) {
        if (instance == null) {
            instance = new ResourceManager(context);
        }
    }

    public static String getString(int resId) {
        return resources.getString(resId);
    }
}