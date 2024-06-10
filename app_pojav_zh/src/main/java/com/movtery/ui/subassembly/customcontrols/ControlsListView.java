package com.movtery.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.subassembly.filelist.FileSelectedListener;
import com.movtery.utils.stringutils.StringFilter;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ControlsListView extends LinearLayout {
    private final List<ControlItemBean> mData = new ArrayList<>();
    private ControlListAdapter controlListAdapter;
    private FileSelectedListener fileSelectedListener;
    private RecyclerView mainListView;
    private File fullPath = new File(Tools.CTRLMAP_PATH);
    private String filterString = "";
    private boolean showSearchResultsOnly = false;
    private boolean caseSensitive = false;
    private final AtomicInteger searchCount = new AtomicInteger(0);

    public ControlsListView(Context context) {
        this(context, null);
    }

    public ControlsListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        LayoutParams layParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);

        mainListView = new RecyclerView(context);
        controlListAdapter = new ControlListAdapter(this.mData);
        controlListAdapter.setOnItemClickListener((position, name) -> {
            File file = new File(fullPath, name);
            if (this.fileSelectedListener != null) fileSelectedListener.onFileSelected(file, file.getAbsolutePath());
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        mainListView.setLayoutManager(layoutManager);
        if (PREF_ANIMATION) mainListView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.fade_downwards)));
        mainListView.setAdapter(controlListAdapter);

        addView(mainListView, layParam);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    private void loadInfoData(File path) {
        List<ControlItemBean> data = new ArrayList<>();

        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    ControlInfoData controlInfoData = EditControlData.loadFormFile(getContext(), file);
                    if (controlInfoData == null) continue;

                    ControlItemBean controlItemBean = new ControlItemBean(controlInfoData);

                    if (shouldHighlight(controlInfoData, file)) {
                        controlItemBean.setHighlighted(true);
                        searchCount.addAndGet(1);
                    } else if (showSearchResultsOnly) {
                        continue;
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
    }

    public void listAtPath(File path) {
        this.fullPath = path;
        refresh();
    }

    public int searchControls(String filterString, boolean caseSensitive) {
        searchCount.set(0);
        this.filterString = filterString;
        this.caseSensitive = caseSensitive;
        refresh();
        return searchCount.get();
    }

    public void setShowSearchResultsOnly(boolean showSearchResultsOnly) {
        this.showSearchResultsOnly = showSearchResultsOnly;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        PojavApplication.sExecutorService.execute(() -> {
            loadInfoData(fullPath);
            filterString = "";
            runOnUiThread(() -> {
                controlListAdapter.notifyDataSetChanged();
                if (PREF_ANIMATION) mainListView.scheduleLayoutAnimation();
            });
        });
    }
}
