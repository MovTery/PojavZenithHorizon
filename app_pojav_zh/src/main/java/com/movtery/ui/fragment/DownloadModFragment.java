package com.movtery.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.movtery.ui.subassembly.downloadmod.ModVersionGroup;
import com.movtery.ui.subassembly.filelist.SpacesItemDecoration;
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
    private boolean mIsModpack;
    private String mModsPath;

    public DownloadModFragment() {
        super(R.layout.fragment_mod_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        parseViewModel();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        mModVersionView.setLayoutManager(layoutManager);
        mModVersionView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, (int) Tools.dpToPx(8)));

        mRefreshButton.setOnClickListener(v -> refresh());
        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));

        refresh();
    }

    private void refresh() {
        PojavApplication.sExecutorService.execute(() -> {
            runOnUiThread(() -> {
                mProgressBar.setVisibility(View.VISIBLE);
                mLoadingText.setVisibility(View.VISIBLE);
                mModVersionView.setVisibility(View.GONE);
                mRefreshButton.setClickable(false);
            });

            ModDetail mModDetail = mModApi.getModDetails(mModItem);

            TreeMap<String, List<ModVersionGroup.ModItem>> mModVersionsByMinecraftVersion = new TreeMap<>();
            for(ModVersionGroup.ModItem modItem : mModDetail.modItems) {
                for (String mcVersion : modItem.getVersionId()) {
                    mModVersionsByMinecraftVersion.computeIfAbsent(mcVersion, k -> new ArrayList<>())
                            .add(modItem);
                }
            }

            List<ModVersionGroup> mData = new ArrayList<>();
            mModVersionsByMinecraftVersion.descendingMap() //反转
                    .forEach((k, v) -> mData.add(new ModVersionGroup(k, v)));

            runOnUiThread(() -> {
                DownloadModAdapter mModAdapter = new DownloadModAdapter(mModApi, mModDetail, mData, mIsModpack, mModsPath);
                mModVersionView.setAdapter(mModAdapter);

                mProgressBar.setVisibility(View.GONE);
                mLoadingText.setVisibility(View.GONE);
                mModVersionView.setVisibility(View.VISIBLE);
                mRefreshButton.setClickable(true);
            });
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
