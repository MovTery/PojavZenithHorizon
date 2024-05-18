package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.PasteFile;
import com.movtery.filelist.FileIcon;
import com.movtery.filelist.FileRecyclerView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    public static final String jarFileSuffix = ".jar";
    public static final String disableJarFileSuffix = ".jar.disabled";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddModButton, mPasteButton, mDownloadButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileRecyclerView mFileRecyclerView;
    private String mRootPath;

    public ModsFragment() {
        super(R.layout.fragment_mods);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runOnUiThread(() -> {
            if (mFileRecyclerView != null) {
                mFileRecyclerView.refreshPath();
            }
        });
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
        mFileRecyclerView.lockPathAt(new File(mRootPath));
        mFileRecyclerView.refreshPath();

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

        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
        mAddModButton.setOnClickListener(v -> {
            String suffix = ".jar";
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        });
        mPasteButton.setOnClickListener(v -> PasteFile.pasteFile(requireActivity(), mFileRecyclerView.getFullPath(), getFileSuffix(PasteFile.COPY_FILE), () -> runOnUiThread(() -> {
            mPasteButton.setVisibility(View.GONE);
            mFileRecyclerView.refreshPath();
        })));
        mDownloadButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, false);
            bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, mRootPath);
            Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, bundle);
        });
        mRefreshButton.setOnClickListener(v -> mFileRecyclerView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mod_title));
            builder.setMessage(getString(R.string.zh_help_mod_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private void showDialog(File file) {
        String fileName = file.getName();
        String fileParent = file.getParent();

        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, (fileName.endsWith(jarFileSuffix) || fileName.endsWith(disableJarFileSuffix)));
        if (file.isDirectory()) {
            filesButton.messageText = getString(R.string.zh_file_folder_message);
        } else {
            filesButton.messageText = getString(R.string.zh_file_message);
        }
        if (fileName.endsWith(jarFileSuffix))
            filesButton.moreButtonText = getString(R.string.zh_profile_mods_disable);
        else if (fileName.endsWith(disableJarFileSuffix))
            filesButton.moreButtonText = getString(R.string.zh_profile_mods_enable);

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileRecyclerView.refreshPath()), file);

        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));

        //检测后缀名，以设置正确的按钮
        if (fileName.endsWith(jarFileSuffix)) {
            filesDialog.setFileSuffix(jarFileSuffix);
            filesDialog.setMoreButtonClick(v -> {
                File newFile = new File(fileParent, fileName + ".disabled");
                PojavZHTools.renameFile(file, newFile);
                mFileRecyclerView.refreshPath();
                filesDialog.dismiss();
            });
        } else if (fileName.endsWith(disableJarFileSuffix)) {
            filesDialog.setFileSuffix(disableJarFileSuffix);
            filesDialog.setMoreButtonClick(v -> {
                String newFileName = fileName.substring(0, fileName.lastIndexOf(disableJarFileSuffix));
                if (!fileName.endsWith(jarFileSuffix))
                    newFileName += jarFileSuffix; //如果没有.jar结尾，那么默认加上.jar后缀

                File newFile = new File(fileParent, newFileName);
                PojavZHTools.renameFile(file, newFile);
                mFileRecyclerView.refreshPath();
                filesDialog.dismiss();
            });
        }

        filesDialog.show();
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
        mReturnButton = view.findViewById(R.id.zh_mods_return_button);
        mAddModButton = view.findViewById(R.id.zh_mods_add_mod_button);
        mPasteButton = view.findViewById(R.id.zh_mods_paste_button);
        mDownloadButton = view.findViewById(R.id.zh_mods_download_mod_button);
        mRefreshButton = view.findViewById(R.id.zh_mods_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_mods_help_button);
        mFileRecyclerView = view.findViewById(R.id.zh_mods);

        mFileRecyclerView.setFileIcon(FileIcon.MOD);

        mPasteButton.setVisibility(PasteFile.PASTE_TYPE != null ? View.VISIBLE : View.GONE);
    }
}

