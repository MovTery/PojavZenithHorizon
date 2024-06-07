package com.movtery.ui.subassembly.filelist;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.dialog.EditTextDialog;
import com.movtery.utils.stringutils.StringFilter;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("ViewConstructor")
public class FileRecyclerView extends LinearLayout {
    private final List<FileItemBean> mData = new ArrayList<>();
    private Context context;
    private FileRecyclerViewCreator fileRecyclerViewCreator;
    private FileIcon fileIcon = FileIcon.FILE;
    private SetTitleListener mSetTitleListener;
    private FileSelectedListener fileSelectedListener;
    private File fullPath;
    private File lockPath = new File("/");
    private boolean showFiles = true;
    private boolean showFolders = true;
    private String filterString = "";

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
                (position, file, name) -> {
                    if (position == 0 && !lockPath.equals(fullPath)) {
                        parentDir();
                    } else {
                        listFileAt(file);
                    }
                },
                (position, file, name) -> {
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

    public void setTitleListener(SetTitleListener setTitleListener) {
        this.mSetTitleListener = setTitleListener;
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

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public String getFilterString() {
        return filterString;
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

                List<FileItemBean> itemBeans = filterFiles(FileRecyclerViewCreator.loadItemBeansFromPath(context, path, this.fileIcon, this.showFiles, this.showFolders));

                Collections.sort(itemBeans);

                if (!path.equals(lockPath)) {
                    FileItemBean itemBean = new FileItemBean();
                    itemBean.setImage(context.getResources().getDrawable(R.drawable.ic_folder, context.getTheme()));
                    itemBean.setName("..");
                    itemBeans.add(0, itemBean);
                }

                if (mSetTitleListener != null) {
                    mSetTitleListener.setTitle(path.getAbsolutePath());
                }

                runOnUiThread(() -> fileRecyclerViewCreator.loadData(itemBeans));
            } else {
                fileSelectedListener.onFileSelected(path, path.getAbsolutePath());
            }
        } else {
            Toast.makeText(context, R.string.zh_file_does_not_exist, Toast.LENGTH_SHORT).show();
            refreshPath();
        }
    }

    private List<FileItemBean> filterFiles(List<FileItemBean> itemBeans) {
        if (!filterString.isEmpty()) {
            List<FileItemBean> newItemBeans = new ArrayList<>();
            for (FileItemBean input : itemBeans) {
                if (StringFilter.containsAllCharacters(input.getFile().getName(), filterString)) {
                    newItemBeans.add(input);
                }
            }
            return newItemBeans;
        }
        return itemBeans;
    }

    public File getFullPath() {
        return fullPath;
    }

    public void refreshPath() {
        listFileAt(getFullPath());
    }

    public void showSearchDialog() {
        EditTextDialog editTextDialog = new EditTextDialog(context, context.getString(R.string.zh_search), null, getFilterString(), null);
        editTextDialog.setConfirm(v1 -> {
            String string = editTextDialog.getEditBox().getText().toString();
            setFilterString(string);
            refreshPath();
            editTextDialog.dismiss();
        });
        editTextDialog.show();
    }

    public void parentDir() {
        if (!fullPath.getAbsolutePath().equals("/")) {
            listFileAt(fullPath.getParentFile());
        }
    }
}
