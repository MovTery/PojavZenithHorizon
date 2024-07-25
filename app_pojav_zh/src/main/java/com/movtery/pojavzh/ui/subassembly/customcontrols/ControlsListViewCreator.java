package com.movtery.pojavzh.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.dialog.DeleteDialog;
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener;
import com.movtery.pojavzh.ui.subassembly.filelist.RefreshListener;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.stringutils.StringFilter;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlsListViewCreator {
    private final Context context;
    private final RecyclerView mainListView;
    private final List<ControlItemBean> mData = new ArrayList<>();
    private final AtomicInteger searchCount = new AtomicInteger(0);

    private ControlListAdapter controlListAdapter;
    private FileSelectedListener fileSelectedListener;
    private RefreshListener refreshListener;
    private File fullPath = new File(Tools.CTRLMAP_PATH);
    private String filterString = "";
    private boolean showSearchResultsOnly = false;
    private boolean caseSensitive = false;
    private TextView searchCountText;

    public ControlsListViewCreator(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.mainListView = recyclerView;
        init();
    }

    public void init() {
        controlListAdapter = new ControlListAdapter(this.mData);
        controlListAdapter.setOnItemClickListener(new ControlListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String name) {
                File file = new File(fullPath, name);
                if (ControlsListViewCreator.this.fileSelectedListener != null) fileSelectedListener.onFileSelected(file, file.getAbsolutePath());
            }

            @Override
            public void onLongClick(String name) {
                File file = new File(fullPath, name);
                if (ControlsListViewCreator.this.fileSelectedListener != null) fileSelectedListener.onItemLongClick(file, file.getAbsolutePath());
            }

            @Override
            public void onInvalidItemClick(String name) {
                File file = new File(fullPath, name);
                List<File> files = new ArrayList<>();
                files.add(file);
                new DeleteDialog(context, () -> runOnUiThread(ControlsListViewCreator.this::refresh), files).show();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        mainListView.setLayoutManager(layoutManager);
        mainListView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        mainListView.setAdapter(controlListAdapter);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setShowSearchResultsOnly(boolean showSearchResultsOnly) {
        this.showSearchResultsOnly = showSearchResultsOnly;
    }

    public int getItemCount() {
        return controlListAdapter.getItemCount();
    }

    private void loadInfoData(File path) {
        List<ControlItemBean> data = new ArrayList<>();

        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    ControlInfoData controlInfoData = null;
                    if (file.getName().endsWith(".json")) { //只有.json文件会被尝试识别
                        controlInfoData = EditControlData.loadFormFile(context, file);
                    }

                    ControlItemBean controlItemBean;
                    if (controlInfoData == null) {
                        ControlInfoData invalidInfoData = new ControlInfoData();
                        invalidInfoData.fileName = file.getName();
                        controlItemBean = new ControlItemBean(invalidInfoData);
                        controlItemBean.setInvalid(true);
                    } else {
                        controlItemBean = new ControlItemBean(controlInfoData);
                        if (shouldHighlight(controlInfoData, file)) {
                            controlItemBean.setHighlighted(true);
                            searchCount.addAndGet(1);
                        } else if (showSearchResultsOnly) {
                            continue;
                        }
                    }

                    data.add(controlItemBean);
                }
            }
        }

        updateData(data);
    }

    private boolean shouldHighlight(ControlInfoData controlInfoData, File file) {
        if (filterString == null || filterString.isEmpty()) return false;

        String name = controlInfoData.name;
        String searchString = (name != null && !name.isEmpty() && !name.equals("null")) ? name : file.getName();

        //支持搜索文件名或布局名称
        return StringFilter.containsSubstring(searchString, filterString, caseSensitive) ||
                StringFilter.containsSubstring(file.getName(), filterString, caseSensitive);
    }

    private void updateData(List<ControlItemBean> data) {
        if (!Objects.equals(data, this.mData)) {
            this.mData.clear();
            this.mData.addAll(data);
        }
        if (searchCountText != null) {
            runOnUiThread(() -> {
                //展示搜索结果
                int count = searchCount.get();
                searchCountText.setText(searchCountText.getContext().getString(R.string.zh_search_count, count));
                if (count != 0) searchCountText.setVisibility(View.VISIBLE);
            });
        }
    }

    public void listAtPath() {
        this.fullPath = controlPath();
        refresh();
    }

    public void searchControls(TextView searchCountText, String filterString, boolean caseSensitive) {
        searchCount.set(0);
        this.filterString = filterString;
        this.caseSensitive = caseSensitive;
        this.searchCountText = searchCountText;
        refresh();
    }

    private File controlPath() {
        File ctrlPath = new File(Tools.CTRLMAP_PATH);
        if (!ctrlPath.exists()) ZHTools.mkdirs(ctrlPath);
        return ctrlPath;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        PojavApplication.sExecutorService.execute(() -> {
            loadInfoData(fullPath);
            filterString = "";
            runOnUiThread(() -> {
                controlListAdapter.notifyDataSetChanged();
                mainListView.scheduleLayoutAnimation();
                if (refreshListener != null) refreshListener.onRefresh();
            });
        });
    }
}
