package com.movtery.pojavzh.ui.dialog;

import static com.movtery.pojavzh.utils.ZHTools.DIR_CUSTOM_MOUSE;
import static com.movtery.pojavzh.utils.image.ImageUtils.isImage;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon;
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean;
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator;
import com.movtery.pojavzh.utils.file.FileTools;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectMouseDialog extends FullScreenDialog {
    private final List<FileItemBean> mData = new ArrayList<>();
    private MouseSelectedListener mouseSelectedListener;

    public SelectMouseDialog(@NonNull Context context) {
        super(context);

        this.setCancelable(false);
        this.setContentView(R.layout.dialog_select_item);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        RecyclerView mMouseListView = findViewById(R.id.zh_select_view);
        TextView mTitleText = findViewById(R.id.zh_select_item_title);
        ImageButton mCloseButton = findViewById(R.id.zh_select_item_close_button);
        mTitleText.setText(R.string.zh_custom_mouse_title);
        mCloseButton.setOnClickListener(v -> this.dismiss());

        initView(mMouseListView);
    }

    private void initView(RecyclerView mMouseListView) {
        FileRecyclerViewCreator fileRecyclerViewCreator = new FileRecyclerViewCreator(getContext(), mMouseListView, (position, fileItemBean) -> {
            File file = fileItemBean.getFile();
            if (file != null && file.exists() && isImage(file)) {
                DEFAULT_PREF.edit().putString("custom_mouse", file.getName()).apply();
                mouseSelectedListener.onSelectedListener();
                this.dismiss();
            }
            if (position == 0) {
                DEFAULT_PREF.edit().putString("custom_mouse", null).apply();
                mouseSelectedListener.onSelectedListener();
                this.dismiss();
            }
        }, null, mData);

        loadData(fileRecyclerViewCreator);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadData(FileRecyclerViewCreator fileRecyclerViewCreator) {
        List<FileItemBean> fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(getContext(), mousePath(), FileIcon.IMAGE, true, false);
        fileItemBeans.add(0, new FileItemBean(getContext().getDrawable(R.drawable.ic_mouse_pointer), null, getContext().getString(R.string.zh_custom_mouse_default)));
        runOnUiThread(() -> fileRecyclerViewCreator.loadData(fileItemBeans));
    }

    private File mousePath() {
        File path = new File(DIR_CUSTOM_MOUSE);
        if (!path.exists()) FileTools.mkdirs(path);
        return path;
    }

    public void setOnSelectedListener(MouseSelectedListener mouseSelectedListener) {
        this.mouseSelectedListener = mouseSelectedListener;
    }

    public interface MouseSelectedListener {
        void onSelectedListener();
    }
}
