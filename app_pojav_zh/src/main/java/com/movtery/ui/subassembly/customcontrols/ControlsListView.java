package com.movtery.ui.subassembly.customcontrols;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.subassembly.filelist.FileSelectedListener;
import com.movtery.ui.subassembly.recyclerview.SpacesItemDecoration;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ControlsListView extends LinearLayout {
    private final List<ControlInfoData> mData = new ArrayList<>();
    private ControlListAdapter controlListAdapter;
    private FileSelectedListener fileSelectedListener;
    private File fullPath = new File(Tools.CTRLMAP_PATH);

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

        RecyclerView mainListView = new RecyclerView(context);
        controlListAdapter = new ControlListAdapter(this.mData);
        controlListAdapter.setOnItemClickListener((position, name) -> {
            File file = new File(fullPath, name);
            if (this.fileSelectedListener != null) fileSelectedListener.onFileSelected(file, file.getAbsolutePath());
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        mainListView.setLayoutManager(layoutManager);
        mainListView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, (int) Tools.dpToPx(8)));
        mainListView.setAdapter(controlListAdapter);

        addView(mainListView, layParam);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    private void loadInfoData(File path) {
        List<ControlInfoData> data = new ArrayList<>();

        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    ControlInfoData controlInfoData = EditControlData.loadFormFile(getContext(), file);
                    if (controlInfoData == null) continue;
                    data.add(controlInfoData);
                }
            }
        }

        if (!Objects.equals(data, this.mData)) {
            this.mData.clear();
            this.mData.addAll(data);
        }
    }

    public void listAtPath(File path) {
        this.fullPath = path;
        refresh();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        PojavApplication.sExecutorService.execute(() -> {
            loadInfoData(fullPath);
            runOnUiThread(() -> controlListAdapter.notifyDataSetChanged());
        });
    }
}
