package com.movtery.pojavzh.ui.subassembly.versionlist;

import static net.kdt.pojavlaunch.extra.ExtraCore.getValue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome;
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean;
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.utils.FilteredSubList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class VersionListView extends LinearLayout {
    private Context context;
    private List<JMinecraftVersionList.Version> releaseList, snapshotList, betaList, alphaList;
    private String[] mInstalledVersions;
    private FileRecyclerViewCreator fileRecyclerViewCreator;
    private VersionSelectedListener versionSelectedListener;

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


    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context) {
        this.context = context;

        LayoutParams layParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);

        RecyclerView mainListView = new RecyclerView(context);

        JMinecraftVersionList jMinecraftVersionList = (JMinecraftVersionList) getValue(ExtraConstants.RELEASE_TABLE);
        JMinecraftVersionList.Version[] versionArray;
        if (jMinecraftVersionList == null || jMinecraftVersionList.versions == null)
            versionArray = new JMinecraftVersionList.Version[0];
        else versionArray = jMinecraftVersionList.versions;

        mInstalledVersions = new File(ProfilePathHome.getGameHome() + "/versions").list();
        if (mInstalledVersions != null)
            Arrays.sort(mInstalledVersions);

        releaseList = new FilteredSubList<>(versionArray, item -> item.type.equals("release"));
        snapshotList = new FilteredSubList<>(versionArray, item -> item.type.equals("snapshot"));
        betaList = new FilteredSubList<>(versionArray, item -> item.type.equals("old_beta"));
        alphaList = new FilteredSubList<>(versionArray, item -> item.type.equals("old_alpha"));

        fileRecyclerViewCreator = new FileRecyclerViewCreator(
                context,
                mainListView,
                (position, fileItemBean) -> versionSelectedListener.onVersionSelected(fileItemBean.name),
                null,
                showVersions(VersionType.RELEASE)
        );

        addView(mainListView, layParam);
    }

    private String[] getVersionIds(List<JMinecraftVersionList.Version> versions) {
        String[] strings = new String[versions.size()];
        for (int i = 0; i < versions.size(); i++) {
            strings[i] = versions.get(i).id;
        }
        return strings;
    }

    public void setVersionSelectedListener(VersionSelectedListener versionSelectedListener) {
        this.versionSelectedListener = versionSelectedListener;
    }

    public void setVersionType(VersionType versionType) {
        showVersions(versionType);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private List<FileItemBean> showVersions(VersionType versionType) {
        switch (versionType) {
            case INSTALLED:
                return getVersion(context.getDrawable(R.drawable.ic_pojav_full), mInstalledVersions);
            case RELEASE:
                return getVersion(context.getDrawable(R.drawable.ic_minecraft), getVersionIds(releaseList));
            case SNAPSHOT:
                return getVersion(context.getDrawable(R.drawable.ic_command_block), getVersionIds(snapshotList));
            case BETA:
                return getVersion(context.getDrawable(R.drawable.ic_old_cobblestone), getVersionIds(betaList));
            case ALPHA:
                return getVersion(context.getDrawable(R.drawable.ic_old_grass_block), getVersionIds(alphaList));
        }
        return null;
    }

    private List<FileItemBean> getVersion(Drawable icon, String[] names) {
        List<FileItemBean> itemBeans = FileRecyclerViewCreator.loadItemBean(icon, names);
        Tools.runOnUiThread(() -> fileRecyclerViewCreator.loadData(itemBeans));
        return itemBeans;
    }
}
