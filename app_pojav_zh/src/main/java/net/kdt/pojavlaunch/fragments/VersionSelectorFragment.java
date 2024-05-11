package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.versionlist.VersionListView;
import com.movtery.versionlist.VersionSelectedListener;
import com.movtery.versionlist.VersionType;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class VersionSelectorFragment extends Fragment {
    public static final String TAG = "FileSelectorFragment";
    private Button mRefreshButton, mReturnButton;
    private Button mInstalledButton, mReleaseButton, mSnapshotButton, mBetaButton, mAlphaButton;
    private VersionListView mVersionListView;
    private VersionType versionType;

    public VersionSelectorFragment() {
        super(R.layout.fragment_version);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);

        versionType = VersionType.RELEASE;
        refresh();

        mRefreshButton.setOnClickListener(v -> refresh());
        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());

        mInstalledButton.setOnClickListener(v -> {
            versionType = VersionType.INSTALLED;
            refresh();
        });
        mReleaseButton.setOnClickListener(v -> {
            versionType = VersionType.RELEASE;
            refresh();
        });
        mSnapshotButton.setOnClickListener(v -> {
            versionType = VersionType.SNAPSHOT;
            refresh();
        });
        mBetaButton.setOnClickListener(v -> {
            versionType = VersionType.BETA;
            refresh();
        });
        mAlphaButton.setOnClickListener(v -> {
            versionType = VersionType.ALPHA;
            refresh();
        });

        mVersionListView.setVersionSelectedListener(new VersionSelectedListener() {
            @Override
            public void onVersionSelected(String version) {
                ExtraCore.setValue(ExtraConstants.VERSION_SELECTOR, version);
                requireActivity().onBackPressed();
            }
        });
    }

    private void refresh() {
        String[] installedVersionsList = new File(Tools.DIR_GAME_NEW + "/versions").list();
        //如果安装的版本列表为空，那么隐藏 已安装 按钮
        mInstalledButton.setVisibility((installedVersionsList == null || installedVersionsList.length == 0) ? View.GONE : View.VISIBLE);

        mVersionListView.setVersionType(versionType);
    }

    private void bindViews(@NonNull View view) {
        mRefreshButton = view.findViewById(R.id.zh_version_refresh_button);
        mReturnButton = view.findViewById(R.id.zh_version_return_button);

        mInstalledButton = view.findViewById(R.id.zh_version_installed);
        mReleaseButton = view.findViewById(R.id.zh_version_release);
        mSnapshotButton = view.findViewById(R.id.zh_version_snapshot);
        mBetaButton = view.findViewById(R.id.zh_version_beta);
        mAlphaButton = view.findViewById(R.id.zh_version_alpha);

        mVersionListView = view.findViewById(R.id.zh_version);
    }
}
