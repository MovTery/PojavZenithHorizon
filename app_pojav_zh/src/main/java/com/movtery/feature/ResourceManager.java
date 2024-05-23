package com.movtery.feature;

import android.content.Context;
import android.content.res.Resources;

public class ResourceManager {
    private static Resources resources;

    public static synchronized void setResources(Context context) {
        if (resources == null) {
            resources = context.getResources();
        }
    }

    public static Resources getResources() {
        return resources;
    }

    public static String getString(int resId) {
        return resources.getString(resId);
    }
}