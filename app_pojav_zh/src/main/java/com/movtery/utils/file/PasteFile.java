package com.movtery.utils.file;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.app.Activity;
import android.widget.Toast;

import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PasteFile {
    private List<File> copyFiles = new ArrayList<>();
    private PasteType pasteType = null;

    // 单例模式
    private static final PasteFile instance = new PasteFile();

    public static PasteFile getInstance() {
        return instance;
    }

    private PasteFile() {}

    public void setCopyFiles(List<File> files) {
        this.copyFiles = new ArrayList<>(files);
    }

    public void setCopyFile(File file) {
        this.copyFiles.clear();
        this.copyFiles.add(file);
    }

    public void setPaste(List<File> files, PasteType type) {
        setCopyFiles(files);
        this.pasteType = type;
    }

    public void setPaste(File file, PasteType type) {
        setCopyFile(file);
        this.pasteType = type;
    }

    public PasteType getPasteType() {
        return pasteType;
    }

    public interface FileExtensionGetter {
        String onGet(File file);
    }

    public void pasteFiles(Activity activity, File target, FileExtensionGetter fileExtensionGetter, Runnable endRunnable) {
        if (copyFiles == null || copyFiles.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.zh_file_does_not_exist), Toast.LENGTH_SHORT).show());
            return;
        }
        new OperationFile(activity, () -> {
            resetState();
            if (endRunnable != null) {
                endRunnable.run();
            }
        }, file -> {
            try {
                if (copyFiles.contains(file)) {
                    handleFileOperation(file, target, fileExtensionGetter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).operationFile(copyFiles);
    }

    private void handleFileOperation(File file, File target, FileExtensionGetter fileExtensionGetter) throws Exception {
        String fileExtension = (fileExtensionGetter != null) ? fileExtensionGetter.onGet(file) : null;
        File destFileOrDir = getNewDestination(file, target, fileExtension);

        if (file.isFile()) {
            if (pasteType == PasteType.COPY) {
                FileUtils.copyFile(file, destFileOrDir);
            } else if (!Objects.equals(file.getParent(), destFileOrDir.getParent())) {
                FileUtils.moveFile(file, destFileOrDir);
            }
        } else if (file.isDirectory()) {
            if (pasteType == PasteType.COPY) {
                FileUtils.copyDirectory(file, destFileOrDir);
            } else if (!Objects.equals(file.getParent(), destFileOrDir.getParent())) {
                FileUtils.moveDirectory(file, destFileOrDir);
            }
        }
    }

    private File getNewDestination(File sourceFile, File targetDir, String fileExtension) {
        File destFile = new File(targetDir, sourceFile.getName());
        if (destFile.exists()) {
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

    private void resetState() {
        pasteType = null;
        copyFiles.clear();
    }

    public enum PasteType {
        COPY, MOVE
    }
}
