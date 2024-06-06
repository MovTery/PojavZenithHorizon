package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.ui.subassembly.customprofilepath.ProfilePathManager;
import com.movtery.ui.subassembly.filelist.FileIcon;
import com.movtery.ui.subassembly.filelist.FileRecyclerView;
import com.movtery.ui.subassembly.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.movtery.ui.dialog.EditTextDialog;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class FileSelectorFragment extends Fragment {
    public static final String TAG = "FileSelectorFragment";
    public static final String BUNDLE_SELECT_FOLDER = "select_folder";
    public static final String BUNDLE_SELECT_FILE = "select_file";
    public static final String BUNDLE_SHOW_FILE = "show_file";
    public static final String BUNDLE_SHOW_FOLDER = "show_folder";
    public static final String BUNDLE_REMOVE_LOCK_PATH = "remove_lock_path";
    public static final String BUNDLE_ROOT_PATH = "root_path";

    private ImageButton mSelectFolderButton, mCreateFolderButton, mRefreshButton;
    private FileRecyclerView mFileRecyclerView;
    private TextView mFilePathView;

    private boolean mSelectFolder = true;
    private boolean mShowFiles = true;
    private boolean mShowFolders = true;
    private boolean mRemoveLockPath = true;
    private String mRootPath = ProfilePathManager.getCurrentPath();


    public FileSelectorFragment() {
        super(R.layout.fragment_files);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();
        if (!mSelectFolder) mSelectFolderButton.setVisibility(View.GONE);
        else mSelectFolderButton.setVisibility(View.VISIBLE);

        mFileRecyclerView.setShowFiles(mShowFiles);
        mFileRecyclerView.setShowFolders(mShowFolders);
        mFileRecyclerView.setTitleListener((title) -> mFilePathView.setText(removeLockPath(title)));
        mFileRecyclerView.lockAndListAt(new File(mRootPath), new File(mRootPath));

        mCreateFolderButton.setOnClickListener(v -> {
            EditTextDialog editTextDialog = new EditTextDialog(requireContext(), getString(R.string.folder_dialog_insert_name), null, null, null);
            editTextDialog.setConfirm(view1 -> {
                String name = editTextDialog.getEditBox().getText().toString().replace("/", "");

                if (name.isEmpty()) {
                    editTextDialog.getEditBox().setError(getString(R.string.zh_file_rename_empty));
                    return;
                }

                File folder = new File(mFileRecyclerView.getFullPath(), name);

                if (folder.exists()) {
                    editTextDialog.getEditBox().setError(getString(R.string.zh_file_rename_exitis));
                    return;
                }

                boolean success = folder.mkdir();
                if (success) {
                    mFileRecyclerView.listFileAt(new File(mFileRecyclerView.getFullPath(), name));
                } else {
                    mFileRecyclerView.refreshPath();
                }

                editTextDialog.dismiss();
            });
            editTextDialog.show();
        });

        mSelectFolderButton.setOnClickListener(v -> {
            ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(mFileRecyclerView.getFullPath().getAbsolutePath()));
            Tools.removeCurrentFragment(requireActivity());
        });

        mRefreshButton.setOnClickListener(v -> mFileRecyclerView.refreshPath());

        mFileRecyclerView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path));
                Tools.removeCurrentFragment(requireActivity());
            }

            @Override
            public void onItemLongClick(File file, String path) {
            }
        });
    }

    private String removeLockPath(String path) {
        String string = path;
        if (mRemoveLockPath) {
            string = path.replace(mRootPath, ".");
        }
        return string;
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mSelectFolder = bundle.getBoolean(BUNDLE_SELECT_FOLDER, mSelectFolder);
        mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILE, mShowFiles);
        mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDER, mShowFolders);
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
        mRemoveLockPath = bundle.getBoolean(BUNDLE_REMOVE_LOCK_PATH, true);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void bindViews(@NonNull View view) {
        mSelectFolderButton = view.findViewById(R.id.zh_files_return_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mFileRecyclerView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);

        mSelectFolderButton.setContentDescription(getString(R.string.folder_fragment_select));
        mCreateFolderButton.setContentDescription(getString(R.string.folder_fragment_create));
        mSelectFolderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, getContext().getTheme()));
        mFileRecyclerView.setFileIcon(FileIcon.FILE);

        view.findViewById(R.id.zh_files_paste_button).setVisibility(View.GONE);
    }
}
