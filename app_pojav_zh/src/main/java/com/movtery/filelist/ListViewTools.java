package com.movtery.filelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListViewTools {
    private final FileListAdapter fileListAdapter;
    private final List<FileItemBean> mData;

    public ListViewTools(Context context, RecyclerView.ItemDecoration itemDecoration, RecyclerView recyclerView, FileListAdapter.OnItemClickListener onItemClickListener, FileListAdapter.OnItemLongClickListener onItemLongClickListener, List<FileItemBean> itemBeans) {
        this.mData = itemBeans;

        this.fileListAdapter = new FileListAdapter(this.mData);
        this.fileListAdapter.setOnItemClickListener(onItemClickListener);
        this.fileListAdapter.setOnItemLongClickListener(onItemLongClickListener);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(this.fileListAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadData(List<FileItemBean> itemBeans) {
        this.mData.clear();
        this.mData.addAll(itemBeans);
        fileListAdapter.notifyDataSetChanged();
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
                        case CONTROL:
                            if (file.getName().endsWith(".json")) {
                                itemBean.setImage(resources.getDrawable(R.drawable.ic_menu_custom_controls));
                            } else {
                                itemBean.setImage(getFileIcon(file, resources));
                            }
                            break;
                        case MOD:
                            if (file.getName().endsWith(".jar")) {
                                itemBean.setImage(resources.getDrawable(R.drawable.ic_java));
                            } else {
                                itemBean.setImage(getFileIcon(file, resources));
                            }
                            break;
                        case FILE:
                        default:
                            itemBean.setImage(resources.getDrawable(R.drawable.ic_file));
                    }
                } else {
                    itemBean.setImage(resources.getDrawable(R.drawable.ic_folder));
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
            return resources.getDrawable(R.drawable.ic_folder);
        } else {
            return resources.getDrawable(R.drawable.ic_file);
        }
    }
}
