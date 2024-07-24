package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.mod.modloader.BaseModVersionListAdapter;
import com.movtery.pojavzh.ui.dialog.SelectRuntimeDialog;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListAdapter;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListItemBean;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListFragment;
import com.movtery.pojavzh.utils.MCVersionComparator;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.modloaders.OptiFineDownloadTask;
import net.kdt.pojavlaunch.modloaders.OptiFineUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class DownloadOptiFineFragment extends TwoLevelListFragment implements ModloaderDownloadListener {
    public static final String TAG = "DownloadOptiFineFragment";
    private final ModloaderListenerProxy modloaderListenerProxy = new ModloaderListenerProxy();

    public DownloadOptiFineFragment() {
        super();
    }

    @Override
    protected void init() {
        setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_optifine));
        setNameText("OptiFine");
        setReleaseCheckBoxGone();
        super.init();
    }

    @Override
    protected Future<?> refresh() {
        return PojavApplication.sExecutorService.submit(() -> {
            try {
                runOnUiThread(() -> componentProcessing(true));
                OptiFineUtils.OptiFineVersions optiFineVersions = OptiFineUtils.downloadOptiFineVersions();
                processModDetails(optiFineVersions);
            } catch (Exception e) {
                runOnUiThread(() -> componentProcessing(false));
            }
        });
    }

    private void processModDetails(OptiFineUtils.OptiFineVersions optiFineVersions) {
        if (optiFineVersions == null) {
            runOnUiThread(() -> componentProcessing(false));
            return;
        }
        Future<?> currentTask = getCurrentTask();

        Map<String, List<OptiFineUtils.OptiFineVersion>> mOptiFineVersions = new HashMap<>();
        optiFineVersions.optifineVersions.forEach(optiFineVersionList -> { //通过版本列表一层层遍历并合成为 Minecraft版本 + Optifine版本的Map集合
            if (currentTask.isCancelled()) return;

            optiFineVersionList.forEach(optiFineVersion -> {
                if (currentTask.isCancelled()) return;

                mOptiFineVersions.computeIfAbsent(optiFineVersion.minecraftVersion, k -> new ArrayList<>()).add(optiFineVersion);
            });
        });

        if (currentTask.isCancelled()) return;

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mOptiFineVersions.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(MCVersionComparator::versionCompare))
                .forEach(entry -> {
                    if (currentTask.isCancelled()) return;

                    BaseModVersionListAdapter adapter = new BaseModVersionListAdapter(activity, modloaderListenerProxy, this, R.drawable.ic_optifine, entry.getValue());
                    adapter.setOnItemClickListener(version -> new Thread(new OptiFineDownloadTask((OptiFineUtils.OptiFineVersion) version, modloaderListenerProxy)).start());

                    mData.add(new TwoLevelListItemBean(entry.getKey(), adapter));
                });

        if (currentTask.isCancelled()) return;

        runOnUiThread(() -> {
            RecyclerView recyclerView = getRecyclerView();
            try {
                TwoLevelListAdapter mModAdapter = (TwoLevelListAdapter) recyclerView.getAdapter();
                if (mModAdapter == null) {
                    mModAdapter = new TwoLevelListAdapter(this, mData);
                    recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                    recyclerView.setAdapter(mModAdapter);
                } else {
                    mModAdapter.updateData(mData);
                }
            } catch (Exception ignored) {
            }

            componentProcessing(false);
            recyclerView.scheduleLayoutAnimation();
        });
    }

    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(() -> {
            Intent modInstallerStartIntent = new Intent(activity, JavaGUILauncherActivity.class);
            OptiFineUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile);
            SelectRuntimeDialog selectRuntimeDialog = new SelectRuntimeDialog(activity);
            selectRuntimeDialog.setListener(jreName -> {
                modloaderListenerProxy.detachListener();

                modInstallerStartIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName);
                selectRuntimeDialog.dismiss();
                Tools.backToMainMenu(activity);
                activity.startActivity(modInstallerStartIntent);
            });
            selectRuntimeDialog.show();
        });
    }

    @Override
    public void onDataNotAvailable() {
        Tools.runOnUiThread(() -> {
            modloaderListenerProxy.detachListener();
            Tools.dialog(activity,
                    activity.getString(R.string.global_error),
                    activity.getString(R.string.of_dl_failed_to_scrape));
        });
    }

    @Override
    public void onDownloadError(Exception e) {
        Tools.runOnUiThread(() -> {
            modloaderListenerProxy.detachListener();
            Tools.showError(activity, e);
        });
    }
}
