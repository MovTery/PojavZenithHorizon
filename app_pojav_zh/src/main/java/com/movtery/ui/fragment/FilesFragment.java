package com.movtery.ui.fragment;

import static com.movtery.utils.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.ui.subassembly.customprofilepath.ProfilePathManager;
import com.movtery.ui.subassembly.filelist.FileIcon;
import com.movtery.ui.subassembly.filelist.FileRecyclerAdapter;
import com.movtery.utils.file.PasteFile;
import com.movtery.ui.subassembly.filelist.FileRecyclerView;
import com.movtery.ui.subassembly.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import com.movtery.utils.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import com.movtery.ui.dialog.EditTextDialog;
import com.movtery.ui.dialog.FilesDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesFragment extends Fragment {
    public static final String TAG = "FilesFragment";
    public static final String BUNDLE_LOCK_PATH = "bundle_lock_path";
    public static final String BUNDLE_LIST_PATH = "bundle_list_path";
    public static final String BUNDLE_SHOW_FILE = "show_file";
    public static final String BUNDLE_SHOW_FOLDER = "show_folder";
    public static final String BUNDLE_QUICK_ACCESS_PATHS = "quick_access_paths";
    public static final String BUNDLE_MULTI_SELECT_MODE = "multi_select_mode";
    public static final String BUNDLE_SELECT_FOLDER_MODE = "select_folder_mode";
    public static final String BUNDLE_REMOVE_LOCK_PATH = "remove_lock_path";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private boolean mShowFiles, mShowFolders, mQuickAccessPaths, mMultiSelectMode, mSelectFolderMode, mRemoveLockPath;
    private ImageButton mReturnButton, mAddFileButton, mCreateFolderButton, mPasteButton, mSearchButton, mRefreshButton;
    private CheckBox mMultiSelectCheck, mSelectAllCheck;
    private View mExternalStorage, mSoftwarePrivate;
    private FileRecyclerView mFileRecyclerView;
    private TextView mFilePathView;
    private String mLockPath, mListPath;

    public FilesFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension(null),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, mFileRecyclerView.getFullPath().getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                mFileRecyclerView.refreshPath();
                            });
                        });
                    }
                }
        );
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        parseBundle();
        bindViews(view);

        mFileRecyclerView.setShowFiles(mShowFiles);
        mFileRecyclerView.setShowFolders(mShowFolders);
        mFileRecyclerView.setTitleListener((title) -> mFilePathView.setText(removeLockPath(title)));

        if (mListPath != null) {
            mFileRecyclerView.lockAndListAt(new File(mLockPath), new File(mListPath));
        } else {
            mFileRecyclerView.lockAndListAt(new File(mLockPath), new File(mLockPath));
        }

        mFileRecyclerView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                showDialog(file);
            }

            @Override
            public void onItemLongClick(File file, String path) {
                if (file.isDirectory()) {
                    showDialog(file);
                }
            }
        });

        mFileRecyclerView.setOnMultiSelectListener(itemBeans -> {
            if (!itemBeans.isEmpty()) {
                PojavApplication.sExecutorService.execute(() -> {
                    //取出全部文件
                    List<File> selectedFiles = new ArrayList<>();
                    itemBeans.forEach(value -> {
                        File file = value.getFile();
                        if (file != null) {
                            selectedFiles.add(file);
                        }
                    });
                    FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                    filesButton.setButtonVisibility(true, true, false, false, true, false);
                    filesButton.setDialogText(getString(R.string.zh_file_multi_select_mode_title),
                            getString(R.string.zh_file_multi_select_mode_message, itemBeans.size()), null);
                    runOnUiThread(() -> {
                        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> {
                            closeMultiSelect();
                            mFileRecyclerView.refreshPath();
                        }), selectedFiles);
                        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));
                        filesDialog.show();
                    });
                });
            }
        });
        mExternalStorage.setOnClickListener(v -> {
            closeMultiSelect();
            mFileRecyclerView.listFileAt(Environment.getExternalStorageDirectory());
        });
        mSoftwarePrivate.setOnClickListener(v -> {
            closeMultiSelect();
            mFileRecyclerView.listFileAt(requireContext().getExternalFilesDir(null));
        });
        FileRecyclerAdapter adapter = mFileRecyclerView.getAdapter();
        mMultiSelectCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSelectAllCheck.setChecked(false);
            mSelectAllCheck.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            adapter.setMultiSelectMode(isChecked);
        });
        mSelectAllCheck.setOnCheckedChangeListener((buttonView, isChecked) -> adapter.selectAllFiles(isChecked));

        mReturnButton.setOnClickListener(v -> {
            if (!mSelectFolderMode) {
                closeMultiSelect();
                Tools.removeCurrentFragment(requireActivity());
            } else {
                ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(mFileRecyclerView.getFullPath().getAbsolutePath()));
                Tools.removeCurrentFragment(requireActivity());
            }
        });
        mAddFileButton.setOnClickListener(v -> {
            closeMultiSelect();
            openDocumentLauncher.launch(null);
        }); //不限制文件类型
        mCreateFolderButton.setOnClickListener(v -> {
            closeMultiSelect();
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
        mPasteButton.setOnClickListener(v -> PasteFile.getInstance().pasteFiles(requireActivity(), mFileRecyclerView.getFullPath(), null, () -> runOnUiThread(() -> {
            closeMultiSelect();
            mPasteButton.setVisibility(View.GONE);
            mFileRecyclerView.refreshPath();
        })));
        mSearchButton.setOnClickListener(v -> {
            closeMultiSelect();
            mFileRecyclerView.showSearchDialog();
        });
        mRefreshButton.setOnClickListener(v -> {
            closeMultiSelect();
            mFileRecyclerView.refreshPath();
        });
    }

    private void closeMultiSelect() {
        //点击其它控件时关闭多选模式
        mMultiSelectCheck.setChecked(false);
        mSelectAllCheck.setVisibility(View.GONE);
    }

    private void showDialog(File file) {
        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, false);

        String message;
        if (file.isDirectory()) {
            message = getString(R.string.zh_file_folder_message);
        } else {
            message = getString(R.string.zh_file_message);
        }
        filesButton.setMessageText(message);

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileRecyclerView.refreshPath()), file);
        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));
        filesDialog.show();
    }

    private String removeLockPath(String path) {
        String string = path;
        if (mRemoveLockPath) {
            string = path.replace(mLockPath, ".");
        }
        return string;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_create_folder_button);
        mPasteButton = view.findViewById(R.id.zh_files_paste_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mSearchButton = view.findViewById(R.id.zh_files_search_button);
        mFileRecyclerView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);
        mExternalStorage = view.findViewById(R.id.zh_files_external_storage);
        mSoftwarePrivate = view.findViewById(R.id.zh_files_software_private);
        mMultiSelectCheck = view.findViewById(R.id.zh_file_multi_select_files);
        mSelectAllCheck = view.findViewById(R.id.zh_file_select_all);

        if (!mQuickAccessPaths) {
            mExternalStorage.setVisibility(View.GONE);
            mSoftwarePrivate.setVisibility(View.GONE);
        }
        if (mSelectFolderMode || !mMultiSelectMode) {
            mMultiSelectCheck.setVisibility(View.GONE);
            mSelectAllCheck.setVisibility(View.GONE);
        }

        if (mSelectFolderMode) {
            mAddFileButton.setVisibility(View.GONE);
            mReturnButton.setContentDescription(getString(R.string.folder_fragment_select));
            mReturnButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, requireActivity().getTheme()));
        }
        mFileRecyclerView.setFileIcon(FileIcon.FILE);

        mPasteButton.setVisibility(PasteFile.getInstance().getPasteType() != null ? View.VISIBLE : View.GONE);

        PojavZHTools.setTooltipText(mReturnButton, mReturnButton.getContentDescription());
        PojavZHTools.setTooltipText(mAddFileButton, mAddFileButton.getContentDescription());
        PojavZHTools.setTooltipText(mCreateFolderButton, mCreateFolderButton.getContentDescription());
        PojavZHTools.setTooltipText(mPasteButton, mPasteButton.getContentDescription());
        PojavZHTools.setTooltipText(mSearchButton, mSearchButton.getContentDescription());
        PojavZHTools.setTooltipText(mRefreshButton, mRefreshButton.getContentDescription());
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mLockPath = bundle.getString(BUNDLE_LOCK_PATH, ProfilePathManager.getCurrentPath());
        mListPath = bundle.getString(BUNDLE_LIST_PATH, null);
        mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILE, true);
        mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDER, true);
        mQuickAccessPaths = bundle.getBoolean(BUNDLE_QUICK_ACCESS_PATHS, true);
        mMultiSelectMode = bundle.getBoolean(BUNDLE_MULTI_SELECT_MODE, true);
        mSelectFolderMode = bundle.getBoolean(BUNDLE_SELECT_FOLDER_MODE, false);
        mRemoveLockPath = bundle.getBoolean(BUNDLE_REMOVE_LOCK_PATH, true);
    }
}

