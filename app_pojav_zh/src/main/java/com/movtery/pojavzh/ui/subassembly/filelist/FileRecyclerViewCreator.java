package com.movtery.pojavzh.ui.subassembly.filelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.mod.ModUtils;
import com.movtery.pojavzh.utils.image.ImageUtils;
import com.movtery.pojavzh.utils.stringutils.StringFilter;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.mainRecyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        this.mainRecyclerView.setLayoutManager(layoutManager);
        this.mainRecyclerView.setAdapter(this.fileRecyclerAdapter);
    }

    public static List<FileItemBean> loadItemBeansFromPath(Context context, File path, FileIcon fileIcon, boolean showFile, boolean showFolder) {
        return loadItemBeansFromPath(context, null, false, false, new AtomicInteger(), path, fileIcon, showFile, showFolder);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static List<FileItemBean> loadItemBeansFromPath(Context context, String filterString, boolean showSearchResultsOnly, boolean caseSensitive, AtomicInteger searchCount, File path, FileIcon fileIcon, boolean showFile, boolean showFolder) {
        List<FileItemBean> itemBeans = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            Resources resources = context.getResources();
            for (File file : files) {
                if (!showFileOrFolder(file, showFile, showFolder)) continue;

                FileItemBean itemBean = new FileItemBean();
                if (filterString != null && !filterString.isEmpty()) {
                    if (StringFilter.containsSubstring(file.getName(), filterString, caseSensitive)) {
                        itemBean.isHighlighted = true;
                        searchCount.addAndGet(1);
                    } else if (showSearchResultsOnly) {
                        continue;
                    }
                }
                itemBean.file = file;
                itemBean.name = null;
                itemBean.image = getIcon(context, file, fileIcon, resources);
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
                    if (ImageUtils.isImage(file)) {
                        return Drawable.createFromPath(file.getAbsolutePath());
                    } else {
                        return getFileIcon(file, resources);
                    }
                case MOD:
                    if (file.getName().endsWith(ModUtils.JAR_FILE_SUFFIX)) {
                        return ContextCompat.getDrawable(context, R.drawable.ic_java);
                    } else if (file.getName().endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) {
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
                fileItemBean.image = drawable;
                fileItemBean.name = name;
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

    @SuppressLint("NotifyDataSetChanged")
    public void loadData(List<FileItemBean> itemBeans) {
        this.mData.clear();
        this.mData.addAll(itemBeans);
        fileRecyclerAdapter.notifyDataSetChanged();
        this.mainRecyclerView.scheduleLayoutAnimation();
    }

    public void setOnMultiSelectListener(FileRecyclerAdapter.OnMultiSelectListener listener) {
        fileRecyclerAdapter.setOnMultiSelectListener(listener);
    }

    public FileRecyclerAdapter getFileRecyclerAdapter() {
        return fileRecyclerAdapter;
    }
}
