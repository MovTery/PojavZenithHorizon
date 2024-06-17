package com.movtery.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.feature.mod.modloader.BaseModVersionListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListFragment;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListItemBean;
import com.movtery.utils.MCVersionComparator;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ForgeDownloadTask;
import net.kdt.pojavlaunch.modloaders.ForgeUtils;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class DownloadForgeFragment extends TwoLevelListFragment implements ModloaderDownloadListener {
    public static final String TAG = "DownloadForgeFragment";
    private final ModloaderListenerProxy modloaderListenerProxy = new ModloaderListenerProxy();

    public DownloadForgeFragment() {
        super();
    }

    @Override
    protected void init() {
        setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_anvil));
        setNameText("Forge");
        setReleaseCheckBoxGone(); //隐藏“仅展示正式版”选择框，在这里没有用处
        super.init();
    }

    @Override
    protected Future<?> refresh() {
        return PojavApplication.sExecutorService.submit(() -> {
            try {
                runOnUiThread(() -> {
                    setFailedToLoad(false);
                    componentProcessing(true);
                });
                List<String> forgeVersions = ForgeUtils.downloadForgeVersions();
                processModDetails(forgeVersions);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    componentProcessing(false);
                    setFailedToLoad(true);
                });
            }
        });
    }

    private void processModDetails(List<String> forgeVersions) {
        if (forgeVersions == null) {
            runOnUiThread(() -> {
                componentProcessing(false);
                setFailedToLoad(true);
            });
            return;
        }
        Future<?> currentTask = getCurrentTask();

        ConcurrentMap<String, List<String>> mForgeVersions = new ConcurrentHashMap<>();
        forgeVersions.forEach(forgeVersion -> {
            if (currentTask.isCancelled()) return;

            //查找并分组Minecraft版本与Forge版本
            int dashIndex = forgeVersion.indexOf("-");
            String gameVersion = forgeVersion.substring(0, dashIndex);
            mForgeVersions.computeIfAbsent(gameVersion, k -> new ArrayList<>()).add(forgeVersion);
        });

        if (currentTask.isCancelled()) return;

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mForgeVersions.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(MCVersionComparator::versionCompare))
                .forEach(entry -> {
                    if (currentTask.isCancelled()) return;

                    //为整理好的Forge版本设置Adapter
                    BaseModVersionListAdapter adapter = new BaseModVersionListAdapter(requireContext(), modloaderListenerProxy, this, R.drawable.ic_anvil, entry.getValue());
                    adapter.setOnItemClickListener(version -> new Thread(new ForgeDownloadTask(modloaderListenerProxy, (String) version)).start());

                    mData.add(new TwoLevelListItemBean("Minecraft " + entry.getKey(), adapter));
                });

        if (currentTask.isCancelled()) return;

        runOnUiThread(() -> {
            RecyclerView recyclerView = getRecyclerView();
            try {
                TwoLevelListAdapter mModAdapter = (TwoLevelListAdapter) recyclerView.getAdapter();
                if (mModAdapter == null) {
                    mModAdapter = new TwoLevelListAdapter(this, mData);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView.setAdapter(mModAdapter);
                } else {
                    mModAdapter.updateData(mData);
                }
            } catch (Exception ignored) {
            }

            componentProcessing(false);
            if (PREF_ANIMATION) recyclerView.scheduleLayoutAnimation();
        });
    }

    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(() -> {
            Context context = requireContext();
            modloaderListenerProxy.detachListener();

            Intent modInstallerStartIntent = new Intent(context, JavaGUILauncherActivity.class);
            ForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile, true);
            Tools.backToMainMenu(requireActivity());
            context.startActivity(modInstallerStartIntent);
        });
    }

    @Override
    public void onDataNotAvailable() {
        Tools.runOnUiThread(() -> {
            Context context = requireContext();
            modloaderListenerProxy.detachListener();
            Tools.dialog(context,
                    context.getString(R.string.global_error),
                    context.getString(R.string.forge_dl_no_installer));
        });
    }

    @Override
    public void onDownloadError(Exception e) {
        Tools.runOnUiThread(() -> {
            Context context = requireContext();
            modloaderListenerProxy.detachListener();
            Tools.showError(context, e);
        });
    }
}
