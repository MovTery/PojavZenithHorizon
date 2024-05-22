package com.movtery.utils;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.app.Activity;
import android.widget.Toast;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Objects;

public class PasteFile {
    public static File COPY_FILE = null;
    public static PasteType PASTE_TYPE = null;

    public static void pasteFile(Activity activity, File target, String fileExtension, Runnable endRunnable) {
        if (PasteFile.COPY_FILE != null && PasteFile.COPY_FILE.exists()) {
            try {
                //获取当前记录的粘贴状态（如果为空，那么就设置为“复制”模式）
                PasteFile.PasteType pasteType = PasteFile.PASTE_TYPE == null ? PasteFile.PasteType.COPY : PasteFile.PASTE_TYPE;

                File destFileOrDir = getNewDestination(PasteFile.COPY_FILE, target, fileExtension);

                if (PasteFile.COPY_FILE.isFile()) {
                    if (pasteType == PasteFile.PasteType.COPY) {
                        FileUtils.copyFile(PasteFile.COPY_FILE, destFileOrDir);
                    } else if (!Objects.equals(PasteFile.COPY_FILE.getParent(), destFileOrDir.getParent())) {
                        //检查父路径是否一致，如果是，那么就不执行这里的移动逻辑
                        FileUtils.moveFile(PasteFile.COPY_FILE, destFileOrDir);
                    }
                } else if (PasteFile.COPY_FILE.isDirectory()) {
                    if (pasteType == PasteFile.PasteType.COPY) {
                        FileUtils.copyDirectory(PasteFile.COPY_FILE, destFileOrDir);
                    } else if (!Objects.equals(PasteFile.COPY_FILE.getParent(), destFileOrDir.getParent())) {
                        //与上面一样
                        FileUtils.moveDirectory(PasteFile.COPY_FILE, destFileOrDir);
                    }
                }

                PasteFile.COPY_FILE = null;
                PasteFile.PASTE_TYPE = null;
                PojavApplication.sExecutorService.execute(endRunnable);
            } catch (Exception e) {
                Tools.showError(activity, e);
            }
        } else {
            runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.zh_file_does_not_exist), Toast.LENGTH_SHORT).show());
        }
    }

    //获取新的目标文件或目录，确保不与现有文件或目录冲突
    private static File getNewDestination(File sourceFile, File targetDir, String fileExtension) {
        File destFile = new File(targetDir, sourceFile.getName());
        if (destFile.exists()) {
            //如果目标文件或目录已存在，则重命名
            String fileNameWithoutExt = PojavZHTools.getFileNameWithoutExtension(sourceFile.getName(), fileExtension);
            if (fileExtension == null) {
                int dotIndex = sourceFile.getName().lastIndexOf('.');
                fileExtension = dotIndex == -1 ? "" : sourceFile.getName().substring(dotIndex);
            }
            String proposedFileName;
            int counter = 1;
            while (destFile.exists()) {
                proposedFileName = fileNameWithoutExt + " (" + counter + ")" + fileExtension;
                destFile = new File(targetDir, proposedFileName);
                counter++;
            }
        }
        return destFile;
    }

    public enum PasteType {
        COPY, MOVE
    }
}
