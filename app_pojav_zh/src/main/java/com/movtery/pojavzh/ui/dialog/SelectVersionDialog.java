package com.movtery.pojavzh.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionListView;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionType;

import net.kdt.pojavlaunch.R;

public class SelectVersionDialog extends FullScreenDialog {
    private TabLayout mTabLayout;
    private TabLayout.Tab releaseTab, snapshotTab, betaTab, alphaTab, returnTab;
    private VersionType versionType;
    private VersionListView versionListView;

    public SelectVersionDialog(@NonNull Context context) {
        super(context);
        setCancelable(false);
        setContentView(R.layout.dialog_select_version);
        init(context);
    }

    private void init(Context context) {
        mTabLayout = findViewById(R.id.zh_version_tab);
        bindTab(context);
        versionListView = findViewById(R.id.zh_version);

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

        refresh(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()));
    }

    public void setOnVersionSelectedListener(VersionSelectedListener versionSelectedListener) {
        this.versionListView.setVersionSelectedListener(versionSelectedListener);
    }

    private void refresh(TabLayout.Tab tab) {
        setVersionType(tab);
        versionListView.setVersionType(versionType);
    }

    private void setVersionType(TabLayout.Tab tab) {
        if (tab == releaseTab) {
            versionType = VersionType.RELEASE;
        } else if (tab == snapshotTab) {
            versionType = VersionType.SNAPSHOT;
        } else if (tab == betaTab) {
            versionType = VersionType.BETA;
        } else if (tab == alphaTab) {
            versionType = VersionType.ALPHA;
        } else if (tab == returnTab) {
            this.dismiss();
        }
    }

    private void bindTab(Context context) {
        releaseTab = mTabLayout.newTab();
        snapshotTab = mTabLayout.newTab();
        betaTab = mTabLayout.newTab();
        alphaTab = mTabLayout.newTab();
        returnTab = mTabLayout.newTab();

        releaseTab.setText(context.getString(R.string.version_release));
        snapshotTab.setText(context.getString(R.string.version_snapshot));
        betaTab.setText(context.getString(R.string.version_beta));
        alphaTab.setText(context.getString(R.string.version_alpha));
        returnTab.setText(context.getString(R.string.generic_return));

        mTabLayout.addTab(releaseTab);
        mTabLayout.addTab(snapshotTab);
        mTabLayout.addTab(betaTab);
        mTabLayout.addTab(alphaTab);
        mTabLayout.addTab(returnTab);

        mTabLayout.selectTab(releaseTab);
    }
}
