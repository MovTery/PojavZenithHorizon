package com.movtery.versionlist;

import static net.kdt.pojavlaunch.extra.ExtraCore.getValue;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.utils.FilteredSubList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class VersionListView extends LinearLayout {
    private ListView mainListView;
    private VersionType versionType;
    private Context context;
    private VersionSelectedListener versionSelectedListener;
    private String[] mInstalledVersions;
    private List<?>[] mData;

    public VersionListView(Context context) {
        this(context, null);
    }

    public VersionListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VersionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        this.context = context;

        LayoutParams layParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);

        mainListView = new ListView(context);
        mainListView.setOnItemClickListener((adapterView, view, i, l) -> {
            String version = adapterView.getItemAtPosition(i).toString();
            versionSelectedListener.onVersionSelected(version);
        });

        JMinecraftVersionList jMinecraftVersionList = (JMinecraftVersionList) getValue(ExtraConstants.RELEASE_TABLE);
        JMinecraftVersionList.Version[] versionArray;
        if (jMinecraftVersionList == null || jMinecraftVersionList.versions == null)
            versionArray = new JMinecraftVersionList.Version[0];
        else versionArray = jMinecraftVersionList.versions;

        mInstalledVersions = new File(Tools.DIR_GAME_NEW + "/versions").list();
        if (mInstalledVersions != null)
            Arrays.sort(mInstalledVersions);

        List<JMinecraftVersionList.Version> releaseList = new FilteredSubList<>(versionArray, item -> item.type.equals("release"));
        List<JMinecraftVersionList.Version> snapshotList = new FilteredSubList<>(versionArray, item -> item.type.equals("snapshot"));
        List<JMinecraftVersionList.Version> betaList = new FilteredSubList<>(versionArray, item -> item.type.equals("old_beta"));
        List<JMinecraftVersionList.Version> alphaList = new FilteredSubList<>(versionArray, item -> item.type.equals("old_alpha"));

        mData = new List[]{Arrays.asList(mInstalledVersions), releaseList, snapshotList, betaList, alphaList};

        addView(mainListView, layParam);
    }

    public void setVersionSelectedListener(VersionSelectedListener versionSelectedListener) {
        this.versionSelectedListener = versionSelectedListener;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
        showVersions(versionType);
    }

    private void showVersions(VersionType versionType) {
        switch (versionType) {
            case INSTALLED:
                getVersion(0);
                break;
            case RELEASE:
                getVersion(1);
                break;
            case SNAPSHOT:
                getVersion(2);
                break;
            case BETA:
                getVersion(3);
                break;
            case ALPHA:
                getVersion(4);
                break;
        }
    }

    private void getVersion(int type) {
        VersionListAdapter versionListAdapter = new VersionListAdapter(this.context, versionType);
        if (type != 0) {
            for (Object o : mData[type]) {
                JMinecraftVersionList.Version version = (JMinecraftVersionList.Version) o;
                versionListAdapter.add(version.id);
            }
        } else {
            for (String mInstalledVersion : mInstalledVersions) {
                versionListAdapter.add(mInstalledVersion);
            }
        }
        mainListView.setAdapter(versionListAdapter);
    }
}
