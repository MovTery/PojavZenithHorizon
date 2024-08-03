package com.movtery.pojavzh.ui.subassembly.filelist;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.movtery.pojavzh.utils.stringutils.SortStrings;

import java.io.File;

public class FileItemBean implements Comparable<FileItemBean> {
    private Drawable image;
    private File file;
    private String name;
    private boolean isHighlighted = false;
    private boolean isCanCheck = true;

    public FileItemBean() {
    }

    public FileItemBean(Drawable image, File file, String name) {
        this.image = image;
        this.file = file;
        this.name = name;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isCanCheck() {
        return isCanCheck;
    }

    public void setCanCheck(boolean canCheck) {
        isCanCheck = canCheck;
    }

    @Override
    public int compareTo(FileItemBean o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null.");
        }

        String thisName = (this.file != null) ? this.file.getName() : this.name;
        String otherName = (o.file != null) ? o.file.getName() : o.name;

        //首先检查文件是否为目录
        if (this.file != null && this.file.isDirectory()) {
            if (o.file != null && !o.file.isDirectory()) {
                //目录排在文件前面
                return -1;
            }
        } else if (o.file != null && o.file.isDirectory()) {
            //文件排在目录后面
            return 1;
        }

        return SortStrings.compareChar(thisName, otherName);
    }

    @NonNull
    @Override
    public String toString() {
        return "FileItemBean{" +
                "file=" + file +
                ", name='" + name + '\'' +
                '}';
    }
}
