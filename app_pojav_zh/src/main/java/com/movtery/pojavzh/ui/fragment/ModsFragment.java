package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.ui.dialog.FilesDialog;
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon;
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerAdapter;
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerView;
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener;
import com.movtery.pojavzh.ui.subassembly.view.SearchView;
import com.movtery.pojavzh.utils.anim.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.file.OperationFile;
import com.movtery.pojavzh.utils.file.PasteFile;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.fragments.SearchModFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModsFragment extends FragmentWithAnim {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    public static final String jarFileSuffix = ".jar";
    public static final String disableJarFileSuffix = ".jar.disabled";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private View mModsLayout, mOperateLayout, mShadowView, mOperateView;
    private ImageButton mReturnButton, mAddModButton, mPasteButton, mDownloadButton, mSearchSummonButton, mRefreshButton;
    private TextView mNothingTip;
    private SearchView mSearchView;
    private CheckBox mMultiSelectCheck, mSelectAllCheck;
    private FileRecyclerView mFileRecyclerView;
    private String mRootPath;

    public ModsFragment() {
        super(R.layout.fragment_mods);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("jar"),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, mRootPath);

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_profile_mods_added_mod), Toast.LENGTH_SHORT).show();
                                mFileRecyclerView.refreshPath();
                            });
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        mFileRecyclerView.setShowFiles(true);
        mFileRecyclerView.setShowFolders(false);
        mFileRecyclerView.lockAndListAt(new File(mRootPath), new File(mRootPath));

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
                });
                //取出全部文件
                List<File> selectedFiles = new ArrayList<>();
                itemBeans.forEach(value -> {
                    File file = value.getFile();
                    if (file != null) {
                        selectedFiles.add(file);
                    }
                });
                FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                filesButton.setButtonVisibility(true, true, false, false, true, true);
                filesButton.setDialogText(getString(R.string.zh_file_multi_select_mode_title),
                        getString(R.string.zh_file_multi_select_mode_message, itemBeans.size()),
                        getString(R.string.zh_profile_mods_disable_or_enable));
                runOnUiThread(() -> {
                    FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> {
                        closeMultiSelect();
                        mFileRecyclerView.refreshPath();
                    }), selectedFiles);
                    filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));
                    filesDialog.setMoreButtonClick(() -> new OperationFile(requireContext(), () -> runOnUiThread(() -> {
                        closeMultiSelect();
                        mFileRecyclerView.refreshPath();
                    }), file -> {
                        if (file != null && file.exists()) {
                            String fileName = file.getName();
                            if (fileName.endsWith(jarFileSuffix)) {
                                disableMod(file);
                            } else if (fileName.endsWith(disableJarFileSuffix)) {
                                enableMod(file);
                            }
                        }
                    }).operationFile(selectedFiles));
                    filesDialog.show();
                });
            }
        });
        mFileRecyclerView.setRefreshListener(() -> {
            int itemCount = mFileRecyclerView.getItemCount();
            boolean show = itemCount == 0;
            AnimUtils.setVisibilityAnim(mNothingTip, show);
        });
        FileRecyclerAdapter adapter = mFileRecyclerView.getAdapter();
        mMultiSelectCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSelectAllCheck.setChecked(false);
            mSelectAllCheck.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            adapter.setMultiSelectMode(isChecked);
        });
        mSelectAllCheck.setOnCheckedChangeListener((buttonView, isChecked) -> adapter.selectAllFiles(isChecked));

        mReturnButton.setOnClickListener(v -> {
            closeMultiSelect();
            ZHTools.onBackPressed(requireActivity());
        });
        mAddModButton.setOnClickListener(v -> {
            closeMultiSelect();
            String suffix = ".jar";
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        });
        mPasteButton.setOnClickListener(v -> PasteFile.getInstance().pasteFiles(requireActivity(), mFileRecyclerView.getFullPath(), this::getFileSuffix, () -> runOnUiThread(() -> {
            closeMultiSelect();
            mPasteButton.setVisibility(View.GONE);
            mFileRecyclerView.refreshPath();
        })));
        mDownloadButton.setOnClickListener(v -> {
            closeMultiSelect();
            Bundle bundle = new Bundle();
            bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, false);
            bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, mRootPath);
            ZHTools.swapFragmentWithAnim(this, SearchModFragment.class, SearchModFragment.TAG, bundle);
        });
        mSearchSummonButton.setOnClickListener(v -> {
            closeMultiSelect();
            mSearchView.setVisibility();
        });
        mRefreshButton.setOnClickListener(v -> {
            closeMultiSelect();
            mFileRecyclerView.refreshPath();
        });

        ViewAnimUtils.slideInAnim(this);
    }

    private void closeMultiSelect() {
        //点击其它控件时关闭多选模式
        mMultiSelectCheck.setChecked(false);
        mSelectAllCheck.setVisibility(View.GONE);
    }

    private void showDialog(File file) {
        String fileName = file.getName();

        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, (fileName.endsWith(jarFileSuffix) || fileName.endsWith(disableJarFileSuffix)));
        if (file.isDirectory()) {
            filesButton.setMessageText(getString(R.string.zh_file_folder_message));
        } else {
            filesButton.setMessageText(getString(R.string.zh_file_message));
        }
        if (fileName.endsWith(jarFileSuffix))
            filesButton.setMoreButtonText(getString(R.string.zh_profile_mods_disable));
        else if (fileName.endsWith(disableJarFileSuffix))
            filesButton.setMoreButtonText(getString(R.string.zh_profile_mods_enable));

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileRecyclerView.refreshPath()), file);

        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));

        //检测后缀名，以设置正确的按钮
        if (fileName.endsWith(jarFileSuffix)) {
            filesDialog.setFileSuffix(jarFileSuffix);
            filesDialog.setMoreButtonClick(() -> {
                disableMod(file);
                mFileRecyclerView.refreshPath();
                filesDialog.dismiss();
            });
        } else if (fileName.endsWith(disableJarFileSuffix)) {
            filesDialog.setFileSuffix(disableJarFileSuffix);
            filesDialog.setMoreButtonClick(() -> {
                enableMod(file);
                mFileRecyclerView.refreshPath();
                filesDialog.dismiss();
            });
        }

        filesDialog.show();
    }

    private void disableMod(File file) {
        String fileName = file.getName();
        String fileParent = file.getParent();
        File newFile = new File(fileParent, fileName + ".disabled");
        FileTools.renameFile(file, newFile);
    }

    private void enableMod(File file) {
        String fileName = file.getName();
        String fileParent = file.getParent();
        String newFileName = fileName.substring(0, fileName.lastIndexOf(disableJarFileSuffix));
        if (!fileName.endsWith(jarFileSuffix))
            newFileName += jarFileSuffix; //如果没有.jar结尾，那么默认加上.jar后缀

        File newFile = new File(fileParent, newFileName);
        FileTools.renameFile(file, newFile);
    }

    private String getFileSuffix(File file) {
        String name = file.getName();
        if (name.endsWith(disableJarFileSuffix)) {
            return disableJarFileSuffix;
        } else if (name.endsWith(jarFileSuffix)) {
            return jarFileSuffix;
        } else {
            int dotIndex = file.getName().lastIndexOf('.');
            return dotIndex == -1 ? "" : file.getName().substring(dotIndex);
        }
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mModsLayout = view.findViewById(R.id.mods_layout);
        mOperateLayout = view.findViewById(R.id.operate_layout);
        mShadowView = view.findViewById(R.id.shadowView);

        mOperateView = view.findViewById(R.id.operate_view);

        mReturnButton = view.findViewById(R.id.zh_return_button);
        mAddModButton = view.findViewById(R.id.zh_add_file_button);
        mPasteButton = view.findViewById(R.id.zh_paste_button);
        mDownloadButton = view.findViewById(R.id.zh_create_folder_button);
        mSearchSummonButton = view.findViewById(R.id.zh_search_button);
        mRefreshButton = view.findViewById(R.id.zh_refresh_button);
        mNothingTip = view.findViewById(R.id.zh_mods_nothing);

        mAddModButton.setContentDescription(getString(R.string.zh_profile_mods_add_mod));
        mDownloadButton.setContentDescription(getString(R.string.zh_profile_mods_download_mod));
        mDownloadButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_download));

        mMultiSelectCheck = view.findViewById(R.id.zh_mods_multi_select_files);
        mSelectAllCheck = view.findViewById(R.id.zh_mods_select_all);
        mFileRecyclerView = view.findViewById(R.id.zh_mods);

        mSearchView = new SearchView(view, view.findViewById(R.id.zh_search_view));
        mSearchView.setSearchListener(mFileRecyclerView::searchFiles);
        mSearchView.setShowSearchResultsListener(mFileRecyclerView::setShowSearchResultsOnly);
        mFileRecyclerView.setFileIcon(FileIcon.MOD);

        mPasteButton.setVisibility(PasteFile.getInstance().getPasteType() != null ? View.VISIBLE : View.GONE);

        ZHTools.setTooltipText(mReturnButton, mReturnButton.getContentDescription());
        ZHTools.setTooltipText(mAddModButton, mAddModButton.getContentDescription());
        ZHTools.setTooltipText(mPasteButton, mPasteButton.getContentDescription());
        ZHTools.setTooltipText(mDownloadButton, mDownloadButton.getContentDescription());
        ZHTools.setTooltipText(mSearchSummonButton, mSearchSummonButton.getContentDescription());
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton.getContentDescription());
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mModsLayout, Techniques.BounceInDown);
        ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.BounceInLeft);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.BounceInLeft);
        ViewAnimUtils.setViewAnim(mOperateView, Techniques.FadeInLeft);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mModsLayout, Techniques.FadeOutUp);
        ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.FadeOutRight);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.FadeOutRight);
        super.slideOut(listener);
    }
}

