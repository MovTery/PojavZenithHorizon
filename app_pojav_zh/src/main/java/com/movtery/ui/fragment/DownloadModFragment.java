package com.movtery.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.subassembly.downloadmod.DownloadModAdapter;
import com.movtery.ui.subassembly.downloadmod.ModApiViewModel;
import com.movtery.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.ui.subassembly.downloadmod.ModVersionGroup;
import com.movtery.ui.subassembly.recyclerview.SpacesItemDecoration;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ImageReceiver;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadModFragment extends Fragment {
    public static final String TAG = "DownloadModFragment";
    private ModItem mModItem;
    private ModpackApi mModApi;
    private RecyclerView mModVersionView;
    private ProgressBar mProgressBar;
    private TextView mLoadingText, mModNameText;
    private final ModIconCache mIconCache = new ModIconCache();
    private ImageReceiver mImageReceiver;
    private ImageView mModIcon;
    private Button mReturnButton, mRefreshButton;
    private CheckBox mReleaseCheckBox;
    private boolean mIsModpack;
    private String mModsPath;
    private Future<?> currentTask;

    public DownloadModFragment() {
        super(R.layout.fragment_mod_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        parseViewModel();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        if (PREF_ANIMATION) mModVersionView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        mModVersionView.setLayoutManager(layoutManager);
        mModVersionView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, (int) Tools.dpToPx(8)));

        mRefreshButton.setOnClickListener(v -> refresh());
        mReleaseCheckBox.setOnClickListener(v -> refresh());
        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));

        refresh();
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    private void cancelTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    private void refresh() {
        cancelTask();

        currentTask = PojavApplication.sExecutorService.submit(() -> {
            componentProcessing(true);

            ModDetail mModDetail = mModApi.getModDetails(mModItem);

            String regex = "^\\d+\\.\\d+\\.\\d+$|^\\d+\\.\\d+$";
            Pattern pattern = Pattern.compile(regex);

            TreeMap<String, List<ModVersionGroup.ModVersionItem>> mModVersionsByMinecraftVersion = new TreeMap<>();
            for (ModVersionGroup.ModVersionItem modVersionItem : mModDetail.modVersionItems) {
                for (String mcVersion : modVersionItem.getVersionId()) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    if (mReleaseCheckBox.isChecked()) {
                        Matcher matcher = pattern.matcher(mcVersion);
                        if (!matcher.matches()) {
                            //如果不是正式版本，将继续检测下一项
                            continue;
                        }
                    }

                    mModVersionsByMinecraftVersion.computeIfAbsent(mcVersion, k -> new ArrayList<>())
                            .add(modVersionItem);
                }
            }

            List<ModVersionGroup> mData = new ArrayList<>();
            mModVersionsByMinecraftVersion.descendingMap() //反转
                    .forEach((k, v) -> mData.add(new ModVersionGroup(k, v)));

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            runOnUiThread(() -> {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                DownloadModAdapter mModAdapter = (DownloadModAdapter) mModVersionView.getAdapter();
                if (mModAdapter == null) {
                    mModAdapter = new DownloadModAdapter(
                            new ModDependencies.SelectedMod(this,
                                    mModItem.title, mModApi, mIsModpack, mModsPath),
                            mModDetail, mData);
                    mModVersionView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    mModVersionView.setAdapter(mModAdapter);
                } else {
                    mModAdapter.updateData(mData);
                }

                componentProcessing(false);
                if (PREF_ANIMATION) mModVersionView.scheduleLayoutAnimation();
            });
        });
    }

    private void componentProcessing(boolean state) {
        runOnUiThread(() -> {
            mProgressBar.setVisibility(state ? View.VISIBLE : View.GONE);
            mLoadingText.setVisibility(state ? View.VISIBLE : View.GONE);
            mModVersionView.setVisibility(state ? View.GONE : View.VISIBLE);

            mRefreshButton.setClickable(!state);
            mReleaseCheckBox.setClickable(!state);
        });
    }

    private void bindViews(View view) {
        mModVersionView = view.findViewById(R.id.zh_mod);
        mProgressBar = view.findViewById(R.id.zh_mod_loading);
        mLoadingText = view.findViewById(R.id.zh_mod_loading_text);
        mModIcon = view.findViewById(R.id.zh_mod_icon);
        mModNameText = view.findViewById(R.id.zh_mod_name);
        mReturnButton = view.findViewById(R.id.zh_mod_return_button);
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button);
        mReleaseCheckBox = view.findViewById(R.id.zh_mod_release_version);
    }

    private void parseViewModel() {
        ModApiViewModel viewModel = new ViewModelProvider(requireActivity()).get(ModApiViewModel.class);
        mModApi = viewModel.getModApi();
        mModItem = viewModel.getModItem();
        mIsModpack = viewModel.isModpack();
        mModsPath = viewModel.getModsPath();

        mModNameText.setText(mModItem.title);

        mImageReceiver = bm -> {
            mImageReceiver = null;
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bm);
            drawable.setCornerRadius(getResources().getDimension(R.dimen._1sdp) / 250 * bm.getHeight());
            mModIcon.setImageDrawable(drawable);
        };
        mIconCache.getImage(mImageReceiver, mModItem.getIconCacheTag(), mModItem.imageUrl);
    }
}
