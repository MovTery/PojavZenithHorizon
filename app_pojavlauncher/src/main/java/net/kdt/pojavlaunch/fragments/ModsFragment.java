package net.kdt.pojavlaunch.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    private Button mSaveButton;
    private FileListView mFileListView;
    private String mRootPath;

    public ModsFragment() {
        super(R.layout.fragment_mods);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(false);
        mFileListView.lockPathAt(new File(mRootPath));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                String fileName = file.getName();
                String fileParent = file.getParent();
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_profile_mods_tips));
                builder.setMessage(getString(R.string.zh_profile_mods_message));

                DialogInterface.OnClickListener deleteListener = (dialog, which) -> {
                    // 显示确认删除的对话框
                    AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(requireActivity());

                    deleteConfirmation.setTitle(getString(R.string.zh_profile_mods_tips));
                    deleteConfirmation.setMessage(getString(R.string.zh_profile_mods_delete) + "\n" + fileName);

                    deleteConfirmation.setPositiveButton(getString(R.string.global_delete), (dialog1, which1) -> {
                        boolean deleted = file.delete();
                        if (deleted) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_deleted) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                    });

                    deleteConfirmation.setNegativeButton(getString(R.string.zh_profile_mods_cancel), null);
                    deleteConfirmation.show();
                };

                DialogInterface.OnClickListener disableListener = (dialog, which) -> {
                    File newFile = new File(fileParent, fileName.substring(0, fileName.lastIndexOf('.')) + ".d");
                    boolean disable = file.renameTo(newFile);
                    if (disable) {
                        Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_disabled) + fileName, Toast.LENGTH_SHORT).show();
                    }
                    mFileListView.refreshPath();
                };

                DialogInterface.OnClickListener enableListener = (dialog, which) -> {
                    File newFile = new File(fileParent, fileName.substring(0, fileName.lastIndexOf('.')) + ".jar");
                    boolean disable = file.renameTo(newFile);
                    if (disable) {
                        Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_enabled) + fileName, Toast.LENGTH_SHORT).show();
                    }
                    mFileListView.refreshPath();
                };

                builder.setPositiveButton(getString(R.string.global_delete), deleteListener)
                        .setNegativeButton(getString(R.string.zh_profile_mods_cancel), null);
                if (file.getName().endsWith(".jar")) {
                    builder.setNeutralButton(getString(R.string.zh_profile_mods_disable), disableListener);
                } else if (file.getName().endsWith(".d")) {
                    builder.setNeutralButton(getString(R.string.zh_profile_mods_enable), enableListener);
                }

                builder.show();
            }
        });

        mSaveButton.setOnClickListener(view1 -> requireActivity().onBackPressed());
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mSaveButton = view.findViewById(R.id.zh_mods_save_button);
        mFileListView = view.findViewById(R.id.zh_mods);
    }
}

