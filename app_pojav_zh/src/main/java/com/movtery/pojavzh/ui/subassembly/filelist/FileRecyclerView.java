package com.movtery.pojavzh.ui.subassembly.filelist;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("ViewConstructor")
public class FileRecyclerView extends LinearLayout {
    private final List<FileItemBean> mData = new ArrayList<>();
    private Context context;
    private FileRecyclerViewCreator fileRecyclerViewCreator;
    private FileIcon fileIcon = FileIcon.FILE;
    private SetTitleListener mSetTitleListener;
    private FileSelectedListener fileSelectedListener;
    private RefreshListener mRefreshListener;
    private File fullPath;
    private File lockPath = new File("/");
    private boolean showFiles = true;
    private boolean showFolders = true;
    private String filterString = "";
    private boolean showSearchResultsOnly = false;
    private boolean caseSensitive = false;
    private final AtomicInteger searchCount = new AtomicInteger(0);

    public FileRecyclerView(Context context) {
        this(context, null);
    }

    public FileRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(final Context context) {
        this.context = context;

        LayoutParams layParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);

        RecyclerView mainLv = new RecyclerView(context);

        fileRecyclerViewCreator = new FileRecyclerViewCreator(
                context,
                mainLv,
                (position, fileItemBean) -> {
                    if (position == 0 && !lockPath.equals(fullPath)) {
                        parentDir();
                    } else {
                        listFileAt(fileItemBean.getFile());
                    }
                },
                (position, fileItemBean) -> {
                    File file = fileItemBean.getFile();
                    if (file != null) {
                        if (position == 0 && !lockPath.equals(fullPath)) {
                            parentDir();
                        } else  {
                            fileSelectedListener.onItemLongClick(file, file.getAbsolutePath());
                        }
                    }
                },
                mData);

        addView(mainLv, layParam);
    }

    public void setFileSelectedListener(FileSelectedListener listener) {
        this.fileSelectedListener = listener;
    }

    public void setOnMultiSelectListener(FileRecyclerAdapter.OnMultiSelectListener listener) {
        this.fileRecyclerViewCreator.setOnMultiSelectListener(listener);
    }

    public void setTitleListener(SetTitleListener setTitleListener) {
        this.mSetTitleListener = setTitleListener;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public void setShowFiles(boolean showFiles) {
        this.showFiles = showFiles;
    }

    public void setShowFolders(boolean showFolders) {
        this.showFolders = showFolders;
    }

    public void setFileIcon(FileIcon fileIcon) {
        this.fileIcon = fileIcon;
    }

    public int searchFiles(String filterString, boolean caseSensitive) {
        searchCount.set(0);
        this.filterString = filterString;
        this.caseSensitive = caseSensitive;
        refreshPath();
        return searchCount.get();
    }

    public void setShowSearchResultsOnly(boolean showSearchResultsOnly) {
        this.showSearchResultsOnly = showSearchResultsOnly;
    }

    public FileRecyclerAdapter getAdapter() {
        return fileRecyclerViewCreator.getFileRecyclerAdapter();
    }

    public int getItemCount() {
        return fileRecyclerViewCreator.getFileRecyclerAdapter().getItemCount();
    }

    public void lockAndListAt(File lockPath, File listPath) {
        this.lockPath = lockPath;
        listFileAt(listPath);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void listFileAt(final File path) {
        if (path != null && path.exists()) {
            if (path.isDirectory()) {
                fullPath = path;

                List<FileItemBean> itemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(context, this.filterString, showSearchResultsOnly, caseSensitive, searchCount, path, this.fileIcon, this.showFiles, this.showFolders);
                Collections.sort(itemBeans);
                filterString = "";

                if (!path.equals(lockPath)) {
                    FileItemBean itemBean = new FileItemBean();
                    itemBean.setImage(context.getResources().getDrawable(R.drawable.ic_folder, context.getTheme()));
                    itemBean.setName("..");
                    itemBean.setCanCheck(false);
                    itemBeans.add(0, itemBean);
                }

                if (mSetTitleListener != null) {
                    mSetTitleListener.setTitle(path.getAbsolutePath());
                }

                runOnUiThread(() -> {
                    fileRecyclerViewCreator.loadData(itemBeans);
                    if (mRefreshListener != null) mRefreshListener.onRefresh();
                });
            } else {
                fileSelectedListener.onFileSelected(path, path.getAbsolutePath());
            }
        } else {
            listFileAt(Environment.getExternalStorageDirectory());
            Toast.makeText(context, R.string.zh_file_does_not_exist, Toast.LENGTH_SHORT).show();
        }
    }

    public File getFullPath() {
        return fullPath;
    }

    public void refreshPath() {
        listFileAt(getFullPath());
    }

    public void parentDir() {
        if (!fullPath.getAbsolutePath().equals("/")) {
            listFileAt(fullPath.getParentFile());
        }
    }
}
