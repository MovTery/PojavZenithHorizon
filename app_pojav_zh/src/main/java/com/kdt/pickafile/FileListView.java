package com.kdt.pickafile;

import androidx.appcompat.app.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.util.*;
import android.widget.*;

import com.ipaulpro.afilechooser.*;
import java.io.*;
import java.util.*;
import net.kdt.pojavlaunch.*;
import android.os.*;

import org.apache.commons.io.FileUtils;

@SuppressLint("ViewConstructor")
public class FileListView extends LinearLayout
{
    //For list view:
    private File fullPath;
    private ListView mainLv;
    private Context context;

    //For File selected listener:
    private FileSelectedListener fileSelectedListener;
    private DialogTitleListener dialogTitleListener;
    private File lockPath = new File("/");

    //For filtering by file types:
    private final String[] fileSuffixes;
    private boolean showFiles = true;
    private boolean showFolders = true;
    private FileIcon fileIcon;

    public void setFileIcon(FileIcon fileIcon) {
        this.fileIcon = fileIcon;
    }

    public FileListView(AlertDialog build, FileIcon fileIcon) {
        this(build.getContext(), fileIcon, null, new String[0]);
        dialogToTitleListener(build);
    }

    public FileListView(AlertDialog build, FileIcon fileIcon, String fileSuffix) {
        this(build.getContext(), fileIcon, null, new String[]{fileSuffix});
        dialogToTitleListener(build);
    }

    public FileListView(FileIcon fileIcon, AlertDialog build, String[] fileSuffixes){
        this(build.getContext(), fileIcon, null, fileSuffixes);
        dialogToTitleListener(build);
    }

    public FileListView(Context context, FileIcon fileIcon){
        this(context, fileIcon, null);
    }

    public FileListView(Context context, FileIcon fileIcon, AttributeSet attrs){
        this(context, fileIcon, attrs, new String[0]);
    }

    public FileListView(Context context, FileIcon fileIcon, AttributeSet attrs, String[] fileSuffixes) {
        this(context, fileIcon, attrs, 0, fileSuffixes);
    }

    public FileListView(Context context, FileIcon fileIcon, AttributeSet attrs, int defStyle, String[] fileSuffixes) {
        super(context, attrs, defStyle);
        this.fileSuffixes = fileSuffixes;
        this.fileIcon = fileIcon;
        init(context);
    }

    private void dialogToTitleListener(AlertDialog dialog) {
        if(dialog != null) dialogTitleListener = dialog::setTitle;
    }

    public void init(final Context context) {
        //Main setup:
        this.context = context;

        LayoutParams layParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        setOrientation(VERTICAL);

        mainLv = new ListView(context);

        mainLv.setOnItemClickListener((p1, p2, p3, p4) -> {
            // TODO: Implement this method
            File mainFile = new File(p1.getItemAtPosition(p3).toString());
            if (p3 == 0 && !lockPath.equals(fullPath)) {
                parentDir();
            } else {
                listFileAt(mainFile);
            }
        });

        mainLv.setOnItemLongClickListener((p1, p2, p3, p4) -> {
            // TODO: Implement this method
            File mainFile = new File(p1.getItemAtPosition(p3).toString());
            if (mainFile.isFile()) {
                fileSelectedListener.onItemLongClick(mainFile, mainFile.getAbsolutePath());
                return true;
            } else if (mainFile.isDirectory()) {
                String fileName = mainFile.getName();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getContext().getString(R.string.zh_file_delete_dir));
                builder.setMessage(getContext().getString(R.string.zh_file_delete_dir_message));

                DialogInterface.OnClickListener deleteDirListener = ((dialog, which) -> {
                    Toast.makeText(getContext(), getContext().getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                    listFileAt(mainFile.getParentFile());

                    try {
                        FileUtils.deleteDirectory(mainFile);
                        Toast.makeText(getContext(), getContext().getString(R.string.zh_file_delete_dir_success) + "\n" + fileName, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    refreshPath();
                });

                builder.setPositiveButton(getContext().getString(android.R.string.cancel), null)
                        .setNegativeButton(getContext().getString(R.string.global_delete), deleteDirListener);

                builder.show();
            }
            return false;
        });
        addView(mainLv, layParam);

        try {
            listFileAt(Environment.getExternalStorageDirectory());
        } catch (NullPointerException ignored) {} // Android 10+ disallows access to sdcard
    }
    public void setFileSelectedListener(FileSelectedListener listener)
    {
        this.fileSelectedListener = listener;
    }
    public void setDialogTitleListener(DialogTitleListener listener) {
        this.dialogTitleListener = listener;
    }

    public void listFileAt(final File path) {
        try{
            if(path.exists()){
                if(path.isDirectory()){
                    fullPath = path;

                    File[] listFile = path.listFiles();
                    FileListAdapter fileAdapter = new FileListAdapter(context, this.fileIcon);
                    if(!path.equals(lockPath)){
                        fileAdapter.add(new File(path, ".."));
                    }

                    if(listFile != null && listFile.length != 0){
                        Arrays.sort(listFile, new SortFileName());

                        for(File file : listFile){
                            if(file.isDirectory()){
                                if(showFolders && ((!file.getName().startsWith(".")) || file.getName().equals(".minecraft")))
                                    fileAdapter.add(file);
                                continue;
                            }

                            if(showFiles){
                                if(fileSuffixes.length > 0){
                                    for(String suffix : fileSuffixes){
                                        if(file.getName().endsWith("." + suffix)){
                                            fileAdapter.add(file);
                                            break;
                                        }
                                    }
                                }else {
                                    fileAdapter.add(file);
                                }
                            }
                        }
                    }
                    mainLv.setAdapter(fileAdapter);
                    if(dialogTitleListener != null) dialogTitleListener.onChangeDialogTitle(path.getAbsolutePath());
                } else {
                    fileSelectedListener.onFileSelected(path, path.getAbsolutePath());
                }
            } else {
                Toast.makeText(context, R.string.zh_file_does_not_exist, Toast.LENGTH_SHORT).show();
                refreshPath();
            }
        } catch (Exception e){
            Tools.showError(context, e);
        }
    }

    public File getFullPath(){
        return fullPath;
    }

    public void refreshPath() {
        listFileAt(getFullPath());
    }

    public void parentDir() {
        if(!fullPath.getAbsolutePath().equals("/")){
            listFileAt(fullPath.getParentFile());
        }
    }

    public void lockPathAt(File path) {
        lockPath = path;
        listFileAt(path);
    }

    public void setShowFiles(boolean showFiles){
        this.showFiles = showFiles;
    }

    public void setShowFolders(boolean showFolders){
        this.showFolders = showFolders;
    }
}
