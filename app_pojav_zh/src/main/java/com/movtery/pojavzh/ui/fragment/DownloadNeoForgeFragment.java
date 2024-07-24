package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.mod.modloader.BaseModVersionListAdapter;
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeDownloadTask;
import com.movtery.pojavzh.feature.mod.modloader.NeoForgeUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

public class DownloadNeoForgeFragment extends TwoLevelListFragment implements ModloaderDownloadListener {
    public static final String TAG = "DownloadNeoForgeFragment";
    private final ModloaderListenerProxy modloaderListenerProxy = new ModloaderListenerProxy();

    public DownloadNeoForgeFragment() {
        super();
    }

    @Override
    protected void init() {
        setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_neoforge));
        setNameText("NeoForge");
        setReleaseCheckBoxGone(); //隐藏“仅展示正式版”选择框，在这里没有用处
        super.init();
    }

    @Override
    protected Future<?> refresh() {
        return PojavApplication.sExecutorService.submit(() -> {
            try {
                runOnUiThread(() -> componentProcessing(true));
                processModDetails(loadVersionList());
            } catch (Exception e) {
                runOnUiThread(() -> componentProcessing(false));
            }
        });
    }

    public List<String> loadVersionList() {
        try {
            List<String> versions = new ArrayList<>();
            versions.addAll(NeoForgeUtils.downloadNeoForgedForgeVersions());
            versions.addAll(NeoForgeUtils.downloadNeoForgeVersions());

            Collections.reverse(versions);

            return versions;
        } catch (IOException e) {
            return null;
        }
    }

    private void processModDetails(List<String> neoForgeVersions) {
        if (neoForgeVersions == null) {
            runOnUiThread(() -> componentProcessing(false));
            return;
        }
        Future<?> currentTask = getCurrentTask();

        Map<String, List<String>> mNeoForgeVersions = new HashMap<>();
        neoForgeVersions.forEach(neoForgeVersion -> {
            if (currentTask.isCancelled()) return;

            //查找并分组Minecraft版本与NeoForge版本
            String gameVersion;

            boolean is1_20_1 = neoForgeVersion.contains("1.20.1");
            if (is1_20_1) {
                gameVersion = "1.20.1";
            } else if (neoForgeVersion.equals("47.1.82")) {
                return;
            } else { //1.20.2+
                gameVersion = NeoForgeUtils.formatGameVersion(neoForgeVersion);
            }
            mNeoForgeVersions.computeIfAbsent(gameVersion, k -> new ArrayList<>()).add(neoForgeVersion);
        });

        if (currentTask.isCancelled()) return;

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mNeoForgeVersions.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(MCVersionComparator::versionCompare))
                .forEach(entry -> {
                    if (currentTask.isCancelled()) return;

                    BaseModVersionListAdapter adapter = new BaseModVersionListAdapter(activity, modloaderListenerProxy, this, R.drawable.ic_neoforge, entry.getValue());
                    adapter.setOnItemClickListener(version -> new Thread(new NeoForgeDownloadTask(modloaderListenerProxy, (String) version)).start());

                    mData.add(new TwoLevelListItemBean("Minecraft " + entry.getKey(), adapter));
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
            NeoForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile);
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
                    activity.getString(R.string.forge_dl_no_installer));
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
