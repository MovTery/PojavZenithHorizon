package com.movtery.pojavzh.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionListView;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionType;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class VersionSelectorFragment extends Fragment {
    public static final String TAG = "FileSelectorFragment";
    private Button mRefreshButton, mReturnButton;
    private VersionListView mVersionListView;
    private TabLayout mTabLayout;
    private TabLayout.Tab installed, release, snapshot, beta, alpha;
    private VersionType versionType;

    public VersionSelectorFragment() {
        super(R.layout.fragment_version);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        bindTab();

        refresh(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                refresh(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mRefreshButton.setOnClickListener(v -> refresh(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition())));
        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        mVersionListView.setVersionSelectedListener(new VersionSelectedListener() {
            @Override
            public void onVersionSelected(String version) {
                ExtraCore.setValue(ZHExtraConstants.VERSION_SELECTOR, version);
                ZHTools.onBackPressed(requireActivity());
            }
        });
    }

    private void refresh(TabLayout.Tab tab) {
        setVersionType(tab);

        String[] installedVersionsList = new File(ProfilePathHome.getGameHome() + "/versions").list();
        //如果安装的版本列表为空，那么隐藏 已安装 按钮
        boolean hasInstalled = !(installedVersionsList == null || installedVersionsList.length == 0);
        if (hasInstalled) {
            if (mTabLayout.getTabAt(0) != installed) mTabLayout.addTab(installed, 0);
        } else {
            if (mTabLayout.getTabAt(0) == installed) mTabLayout.removeTab(installed);
        }

        mVersionListView.setVersionType(versionType);
    }

    private void setVersionType(TabLayout.Tab tab) {
        if (tab == installed) {
            versionType = VersionType.INSTALLED;
        } else if (tab == release) {
            versionType = VersionType.RELEASE;
        } else if (tab == snapshot) {
            versionType = VersionType.SNAPSHOT;
        } else if (tab == beta) {
            versionType = VersionType.BETA;
        } else if (tab == alpha) {
            versionType = VersionType.ALPHA;
        }
    }

    private void bindViews(@NonNull View view) {
        mRefreshButton = view.findViewById(R.id.zh_version_refresh_button);
        mReturnButton = view.findViewById(R.id.zh_version_return_button);

        mTabLayout = view.findViewById(R.id.zh_version_tab);

        mVersionListView = view.findViewById(R.id.zh_version);
    }

    private void bindTab() {
        installed = mTabLayout.newTab();
        release = mTabLayout.newTab();
        snapshot = mTabLayout.newTab();
        beta = mTabLayout.newTab();
        alpha = mTabLayout.newTab();

        installed.setText(getString(R.string.mcl_setting_veroption_installed));
        release.setText(getString(R.string.mcl_setting_veroption_release));
        snapshot.setText(getString(R.string.mcl_setting_veroption_snapshot));
        beta.setText(getString(R.string.mcl_setting_veroption_oldbeta));
        alpha.setText(getString(R.string.mcl_setting_veroption_oldalpha));

        mTabLayout.addTab(installed);
        mTabLayout.addTab(release);
        mTabLayout.addTab(snapshot);
        mTabLayout.addTab(beta);
        mTabLayout.addTab(alpha);

        mTabLayout.selectTab(release);
    }
}
