package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.subassembly.viewmodel.ModApiViewModel;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionAdapter;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;
import com.movtery.pojavzh.ui.subassembly.viewmodel.RecyclerViewModel;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListAdapter;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListFragment;
import com.movtery.pojavzh.ui.subassembly.twolevellist.TwoLevelListItemBean;
import com.movtery.pojavzh.utils.MCVersionComparator;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ImageReceiver;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadModFragment extends TwoLevelListFragment {
    public static final String TAG = "DownloadModFragment";
    private final ModIconCache mIconCache = new ModIconCache();
    private RecyclerView mParentUIRecyclerView;
    private ModItem mModItem;
    private ModpackApi mModApi;
    private ImageReceiver mImageReceiver;
    private boolean mIsModpack;
    private String mModsPath;

    public DownloadModFragment() {
        super();
    }

    @Override
    protected void init() {
        parseViewModel();
        super.init();
    }

    @Override
    protected Future<?> refresh() {
        return PojavApplication.sExecutorService.submit(() -> {
            try {
                runOnUiThread(() -> componentProcessing(true));
                ModDetail mModDetail = mModApi.getModDetails(mModItem);
                processModDetails(mModDetail);
            } catch (Exception e) {
                runOnUiThread(() -> componentProcessing(false));
            }
        });
    }

    @Override
    public void onDestroy() {
        mParentUIRecyclerView.setEnabled(true);
        super.onDestroy();
    }

    private void processModDetails(ModDetail mModDetail) {
        Future<?> currentTask = getCurrentTask();
        String regex = "^\\d+\\.\\d+\\.\\d+$|^\\d+\\.\\d+$";
        Pattern pattern = Pattern.compile(regex);

        boolean releaseCheckBoxChecked = getReleaseCheckBox().isChecked();
        Map<String, List<ModVersionItem>> mModVersionsByMinecraftVersion = new HashMap<>();
        mModDetail.modVersionItems.forEach(modVersionItem -> {
            if (currentTask.isCancelled()) return;

            String[] versionId = modVersionItem.getVersionId();
            for (String mcVersion : versionId) {
                if (currentTask.isCancelled()) return;

                if (releaseCheckBoxChecked) {
                    Matcher matcher = pattern.matcher(mcVersion);
                    if (!matcher.matches()) {
                        //如果不是正式版本，将继续检测下一项
                        continue;
                    }
                }

                mModVersionsByMinecraftVersion.computeIfAbsent(mcVersion, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(modVersionItem); //将Mod 版本数据加入到相应的版本号分组中
            }
        });

        if (currentTask.isCancelled()) return;

        List<TwoLevelListItemBean> mData = new ArrayList<>();
        mModVersionsByMinecraftVersion.entrySet().stream()
                .sorted((o1, o2) -> MCVersionComparator.versionCompare(o1.getKey(), o2.getKey()))
                .forEach(entry -> {
                    if (currentTask.isCancelled()) return;

                    mData.add(new TwoLevelListItemBean("Minecraft " + entry.getKey(), new ModVersionAdapter(new ModDependencies.SelectedMod(DownloadModFragment.this,
                            mModItem.title, mModApi, mIsModpack, mModsPath), mModDetail, entry.getValue())));
                });

        if (currentTask.isCancelled()) return;

        runOnUiThread(() -> {
            RecyclerView modVersionView = getRecyclerView();
            try {
                TwoLevelListAdapter mModAdapter = (TwoLevelListAdapter) modVersionView.getAdapter();
                if (mModAdapter == null) {
                    mModAdapter = new TwoLevelListAdapter(this, mData);
                    modVersionView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    modVersionView.setAdapter(mModAdapter);
                } else {
                    mModAdapter.updateData(mData);
                }
            } catch (Exception ignored) {
            }

            componentProcessing(false);
            if (PREF_ANIMATION) modVersionView.scheduleLayoutAnimation();
        });
    }

    private void parseViewModel() {
        ModApiViewModel viewModel = new ViewModelProvider(requireActivity()).get(ModApiViewModel.class);
        RecyclerViewModel recyclerViewModel = new ViewModelProvider(requireActivity()).get(RecyclerViewModel.class);
        mModApi = viewModel.getModApi();
        mModItem = viewModel.getModItem();
        mIsModpack = viewModel.isModpack();
        mModsPath = viewModel.getModsPath();
        mParentUIRecyclerView = recyclerViewModel.getView();

        setNameText(mModItem.title);

        mImageReceiver = bm -> {
            mImageReceiver = null;
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bm);
            drawable.setCornerRadius(getResources().getDimension(R.dimen._1sdp) / 250 * bm.getHeight());
            setIcon(drawable);
        };
        mIconCache.getImage(mImageReceiver, mModItem.getIconCacheTag(), mModItem.imageUrl);
    }
}
