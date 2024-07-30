package com.movtery.pojavzh.feature.mod.modloader;

import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class BaseModVersionListAdapter extends ModVersionListAdapter implements TaskCountListener {
    private final Context context;
    private final ModloaderListenerProxy modloaderListenerProxy;
    private final ModloaderDownloadListener listener;
    private boolean mTasksRunning;

    public BaseModVersionListAdapter(Context context, ModloaderListenerProxy modloaderListenerProxy, ModloaderDownloadListener listener,
                                     int icon, List<?> mData) {
        super(mData);
        this.context = context;
        this.modloaderListenerProxy = modloaderListenerProxy;
        this.listener = listener;

        ProgressKeeper.addTaskCountListener(this);
        setIconDrawable(icon);
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        if (mTasksRunning) {
            Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            return;
        }
        modloaderListenerProxy.attachListener(this.listener);

        super.setOnItemClickListener(listener);
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = !(taskCount == 0);
    }
}