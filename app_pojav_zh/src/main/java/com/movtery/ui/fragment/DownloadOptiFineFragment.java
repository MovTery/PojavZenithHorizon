package com.movtery.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.feature.mod.modloader.OptiFineVersionListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListItemBean;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListFragment;
import com.movtery.utils.MCVersionComparator;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;
import net.kdt.pojavlaunch.modloaders.OptiFineUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class DownloadOptiFineFragment extends TwoLevelListFragment implements ModloaderDownloadListener {
    public static final String TAG = "DownloadOptiFineFragment";
    private final ModloaderListenerProxy modloaderListenerProxy = new ModloaderListenerProxy();

    public DownloadOptiFineFragment() {
        super();
    }

    @Override
    protected void init() {
        setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_optifine));
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

        ConcurrentMap<String, List<OptiFineUtils.OptiFineVersion>> mOptiFineVersions = new ConcurrentHashMap<>();
        optiFineVersions.optifineVersions.forEach(optiFineVersionList -> //通过版本列表一层层遍历并合成为 Minecraft版本 + Optifine版本的Map集合
                optiFineVersionList.forEach(optiFineVersion ->
                        mOptiFineVersions.computeIfAbsent(optiFineVersion.minecraftVersion, k -> new ArrayList<>()).add(optiFineVersion)));

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mOptiFineVersions.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(MCVersionComparator::versionCompare))
                .forEach(entry -> mData.add(new TwoLevelListItemBean(entry.getKey(), new OptiFineVersionListAdapter(requireContext(), modloaderListenerProxy, this, entry.getValue()))));

        runOnUiThread(() -> {
            RecyclerView recyclerView = getRecyclerView();
            TwoLevelListAdapter mModAdapter = (TwoLevelListAdapter) recyclerView.getAdapter();
            if (mModAdapter == null) {
                mModAdapter = new TwoLevelListAdapter(this, mData);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(mModAdapter);
            } else {
                mModAdapter.updateData(mData);
            }

            componentProcessing(false);
            if (PREF_ANIMATION) recyclerView.scheduleLayoutAnimation();
        });
    }

    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(()->{
            Context context = requireContext();
            getParentFragmentManager().popBackStackImmediate();
            modloaderListenerProxy.detachListener();

            Intent modInstallerStartIntent = new Intent(context, JavaGUILauncherActivity.class);
            OptiFineUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile);
            context.startActivity(modInstallerStartIntent);
        });
    }

    @Override
    public void onDataNotAvailable() {
        Tools.runOnUiThread(()->{
            Context context = requireContext();
            modloaderListenerProxy.detachListener();
            Tools.dialog(context,
                    context.getString(R.string.global_error),
                    context.getString(R.string.of_dl_failed_to_scrape));
        });
    }

    @Override
    public void onDownloadError(Exception e) {
        Tools.runOnUiThread(()->{
            Context context = requireContext();
            modloaderListenerProxy.detachListener();
            Tools.showError(context, e);
        });
    }
}
