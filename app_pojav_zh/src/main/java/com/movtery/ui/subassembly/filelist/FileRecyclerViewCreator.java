package com.movtery.ui.subassembly.filelist;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import com.movtery.ui.fragment.ModsFragment;

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

    @SuppressLint("UseCompatLoadingForDrawables")
    public static List<FileItemBean> loadItemBeansFromPath(Context context, File path, FileIcon fileIcon, boolean showFile, boolean showFolder) {
        List<FileItemBean> itemBeans = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            Resources resources = context.getResources();
            for (File file : files) {
                //显示文件与显示文件夹
                if (file.isDirectory() && !showFolder) continue;
                if (file.isFile() && !showFile) continue;

                FileItemBean itemBean = new FileItemBean();
                itemBean.setFile(file);
                itemBean.setName(null);
                if (file.isFile()) {
                    switch (fileIcon) {
                        case IMAGE:
                            if (PojavZHTools.isImage(file)) {
                                itemBean.setImage(Drawable.createFromPath(file.getAbsolutePath()));
                            } else {
                                itemBean.setImage(getFileIcon(file, resources));
                            }
                            break;
                        case MOD:
                            if (file.getName().endsWith(ModsFragment.jarFileSuffix)) {
                                itemBean.setImage(resources.getDrawable(R.drawable.ic_java, context.getTheme()));
                            } else if (file.getName().endsWith(ModsFragment.disableJarFileSuffix)) {
                                itemBean.setImage(resources.getDrawable(R.drawable.ic_disabled, context.getTheme()));
                            } else {
                                itemBean.setImage(getFileIcon(file, resources));
                            }
                            break;
                        case FILE:
                        default:
                            itemBean.setImage(resources.getDrawable(R.drawable.ic_file, context.getTheme()));
                    }
                } else {
                    itemBean.setImage(resources.getDrawable(R.drawable.ic_folder, context.getTheme()));
                }
                itemBeans.add(itemBean);
            }
        }
        return itemBeans;
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
