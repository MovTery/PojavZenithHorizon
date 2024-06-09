package com.movtery.ui.subassembly.filelist;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import com.movtery.ui.fragment.ModsFragment;
import com.movtery.utils.stringutils.StringFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileRecyclerViewCreator {
    private final FileRecyclerAdapter fileRecyclerAdapter;
    private final RecyclerView mainRecyclerView;
    private final List<FileItemBean> mData;

    public FileRecyclerViewCreator(Context context, RecyclerView recyclerView, FileRecyclerAdapter.OnItemClickListener onItemClickListener, FileRecyclerAdapter.OnItemLongClickListener onItemLongClickListener, List<FileItemBean> itemBeans) {
        this.mData = itemBeans;

        this.fileRecyclerAdapter = new FileRecyclerAdapter(this.mData);
        this.fileRecyclerAdapter.setOnItemClickListener(onItemClickListener);
        this.fileRecyclerAdapter.setOnItemLongClickListener(onItemLongClickListener);

        this.mainRecyclerView = recyclerView;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        if (PREF_ANIMATION) this.mainRecyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        this.mainRecyclerView.setLayoutManager(layoutManager);
        this.mainRecyclerView.setAdapter(this.fileRecyclerAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadData(List<FileItemBean> itemBeans) {
        this.mData.clear();
        this.mData.addAll(itemBeans);
        fileRecyclerAdapter.notifyDataSetChanged();
        if (PREF_ANIMATION) this.mainRecyclerView.scheduleLayoutAnimation();
    }

    public void setOnMultiSelectListener(FileRecyclerAdapter.OnMultiSelectListener listener) {
        fileRecyclerAdapter.setOnMultiSelectListener(listener);
    }

    public FileRecyclerAdapter getFileRecyclerAdapter() {
        return fileRecyclerAdapter;
    }


    public static List<FileItemBean> loadItemBeansFromPath(Context context, File path, FileIcon fileIcon, boolean showFile, boolean showFolder) {
        return loadItemBeansFromPath(context, null, false, path, fileIcon, showFile, showFolder);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static List<FileItemBean> loadItemBeansFromPath(Context context, String filterString, boolean showSearchResultsOnly, File path, FileIcon fileIcon, boolean showFile, boolean showFolder) {
        List<FileItemBean> itemBeans = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            Resources resources = context.getResources();
            for (File file : files) {
                if (!showFileOrFolder(file, showFile, showFolder)) continue;

                FileItemBean itemBean = new FileItemBean();
                if (filterString != null && !filterString.isEmpty()) {
                    if (StringFilter.containsAllCharacters(file.getName(), filterString)) {
                        itemBean.setHighlighted(true);
                    } else if (showSearchResultsOnly) {
                        continue;
                    }
                }
                itemBean.setFile(file);
                itemBean.setName(null);
                itemBean.setImage(getIcon(context, file, fileIcon, resources));
                itemBeans.add(itemBean);
            }
        }
        return itemBeans;
    }

    private static boolean showFileOrFolder(File file, boolean showFile, boolean showFolder) {
        //显示文件与显示文件夹
        if (file.isDirectory() && !showFolder) return false;
        return !file.isFile() || showFile;
    }

    private static Drawable getIcon(Context context, File file, FileIcon fileIcon, Resources resources) {
        if (file.isFile()) {
            switch (fileIcon) {
                case IMAGE:
                    if (PojavZHTools.isImage(file)) {
                        return Drawable.createFromPath(file.getAbsolutePath());
                    } else {
                        return getFileIcon(file, resources);
                    }
                case MOD:
                    if (file.getName().endsWith(ModsFragment.jarFileSuffix)) {
                        return ContextCompat.getDrawable(context, R.drawable.ic_java);
                    } else if (file.getName().endsWith(ModsFragment.disableJarFileSuffix)) {
                        return ContextCompat.getDrawable(context, R.drawable.ic_disabled);
                    } else {
                        return getFileIcon(file, resources);
                    }
                case FILE:
                default:
                    return ContextCompat.getDrawable(context, R.drawable.ic_file);
            }
        } else {
            return ContextCompat.getDrawable(context, R.drawable.ic_folder);
        }
    }

    public static List<FileItemBean> loadItemBean(Drawable drawable, String[] names) {
        List<FileItemBean> itemBeans = new ArrayList<>();
        if (names != null) {
            for (String name : names) {
                FileItemBean fileItemBean = new FileItemBean();
                fileItemBean.setImage(drawable);
                fileItemBean.setName(name);
                itemBeans.add(fileItemBean);
            }
        }
        return itemBeans;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static Drawable getFileIcon(File file, Resources resources) {
        if (file.isDirectory()) {
            return resources.getDrawable(R.drawable.ic_folder, resources.newTheme());
        } else {
            return resources.getDrawable(R.drawable.ic_file, resources.newTheme());
        }
    }
}
