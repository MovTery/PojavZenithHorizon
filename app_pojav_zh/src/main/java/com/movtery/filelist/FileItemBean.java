package com.movtery.filelist;

import android.graphics.drawable.Drawable;

import java.io.File;

public class FileItemBean implements Comparable<FileItemBean>{
    private Drawable image;
    private File file;
    private String name;

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

    @Override
    public int compareTo(FileItemBean o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to null.");
        }

        String thisName = (this.file != null) ? this.file.getName() : this.name;
        String otherName = (o.file != null) ? o.file.getName() : o.name;

        //首先检查文件是否为目录
        if (this.file != null && this.file.isDirectory()) {
            if (o.file != null && o.file.isFile()) {
                //目录排在文件前面
                return -1;
            }
        } else if (o.file != null && o.file.isDirectory()) {
            //文件排在目录后面
            return 1;
        }

        return compareChar(thisName, otherName, 0);
    }

    private int compareChar(String first, String second, int index) {
        //如果任一字符串的长度小于index，那么较短的字符串应该排在前面
        if (index >= first.length()) return -1;
        if (index >= second.length()) return 1;

        char firstChar1 = Character.toLowerCase(first.charAt(index));
        char firstChar2 = Character.toLowerCase(second.charAt(index));

        int compare = Character.compare(firstChar1, firstChar2);
        if (compare != 0) {
            return compare;
        } else {
            return compareChar(first, second, index + 1);
        }
    }
}
