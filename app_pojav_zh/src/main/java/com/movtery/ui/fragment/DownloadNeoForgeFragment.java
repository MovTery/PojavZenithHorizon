package com.movtery.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.feature.mod.modloader.NeoForgeUtils;
import com.movtery.feature.mod.modloader.NeoForgeVersionListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListAdapter;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListFragment;
import com.movtery.ui.subassembly.twolevellist.TwoLevelListItemBean;
import com.movtery.utils.MCVersionComparator;

import net.kdt.pojavlaunch.JavaGUILauncherActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public class DownloadNeoForgeFragment extends TwoLevelListFragment implements ModloaderDownloadListener {
    public static final String TAG = "DownloadNeoForgeFragment";
    private final ModloaderListenerProxy modloaderListenerProxy = new ModloaderListenerProxy();

    public DownloadNeoForgeFragment() {
        super();
    }

    @Override
    protected void init() {
        setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_neoforge));
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

        ConcurrentMap<String, List<String>> mNeoForgeVersions = new ConcurrentHashMap<>();
        neoForgeVersions.forEach(neoForgeVersion -> {
            if (currentTask.isCancelled()) return;

            //查找并分组Minecraft版本与NeoForge版本
            String gameVersion = null;
            int dashIndex;
            if (!neoForgeVersion.contains("1.20.1") && !neoForgeVersion.contains("47.1.82")) {
                //在字符串“20.2.3-beta”的例子中，只需要子字符串“20.2”
                dashIndex = neoForgeVersion.indexOf(".", 3);
                gameVersion = "1." + neoForgeVersion.substring(0, dashIndex); // "1." + "20.2"
            } else if (neoForgeVersion.contains("1.20.1")) {
                dashIndex = neoForgeVersion.indexOf("-");
                gameVersion = neoForgeVersion.substring(0, dashIndex); // "1.20.1"
            } else if (neoForgeVersion.equals("47.1.82")) return;
            mNeoForgeVersions.computeIfAbsent(gameVersion, k -> new ArrayList<>()).add(neoForgeVersion);
        });

        if (currentTask.isCancelled()) return;

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mNeoForgeVersions.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(MCVersionComparator::versionCompare))
                .forEach(entry -> {
                    if (currentTask.isCancelled()) return;

                    mData.add(new TwoLevelListItemBean("Minecraft " + entry.getKey(),
                            new NeoForgeVersionListAdapter(requireContext(), modloaderListenerProxy, this, entry.getValue())));
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
            } catch (Exception e) {
                return;
            }

            componentProcessing(false);
            if (PREF_ANIMATION) recyclerView.scheduleLayoutAnimation();
        });
    }

    @Override
    public void onDownloadFinished(File downloadedFile) {
        Tools.runOnUiThread(() -> {
            Context context = requireContext();
            getParentFragmentManager().popBackStackImmediate();
            modloaderListenerProxy.detachListener();

            Intent modInstallerStartIntent = new Intent(context, JavaGUILauncherActivity.class);
            NeoForgeUtils.addAutoInstallArgs(modInstallerStartIntent, downloadedFile);
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
