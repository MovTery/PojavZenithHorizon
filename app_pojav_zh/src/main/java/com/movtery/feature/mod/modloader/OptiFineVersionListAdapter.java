package com.movtery.feature.mod.modloader;

import android.content.Context;
import android.widget.Toast;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.modloaders.OptiFineDownloadTask;
import net.kdt.pojavlaunch.modloaders.OptiFineUtils;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class OptiFineVersionListAdapter extends ModVersionListAdapter implements TaskCountListener {
    private boolean mTasksRunning;

    public OptiFineVersionListAdapter(Context context, ModloaderListenerProxy modloaderListenerProxy, ModloaderDownloadListener listener, List<OptiFineUtils.OptiFineVersion> mData) {
        super(mData);
        ProgressKeeper.addTaskCountListener(this);
        setIconDrawable(R.drawable.ic_optifine);
        setOnItemClickListener(version -> {
            if (mTasksRunning) {
                Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                return;
            }

            modloaderListenerProxy.attachListener(listener);
            PojavApplication.sExecutorService.execute(() -> new OptiFineDownloadTask((OptiFineUtils.OptiFineVersion) version, modloaderListenerProxy).run());
        });
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = !(taskCount == 0);
    }
}
