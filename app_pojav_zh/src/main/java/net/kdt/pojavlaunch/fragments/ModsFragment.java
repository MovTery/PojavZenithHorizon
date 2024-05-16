package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.filelist.FileIcon;
import com.movtery.filelist.FileListView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.DeleteDialog;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    public static final String jarFileSuffix = ".jar";
    public static final String disableJarFileSuffix = ".jar.disabled";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddModButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
    private String mRootPath;
    private TextView mTitleView;

    public ModsFragment() {
        super(R.layout.fragment_files);
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
                                mFileListView.refreshPath();
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
        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(false);
        mFileListView.lockPathAt(new File(mRootPath));
        mFileListView.refreshPath();

        mTitleView.setText(getString(R.string.zh_profile_mods));
        mAddModButton.setText(getString(R.string.zh_profile_mods_add_mod));

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                String fileName = file.getName();
                String fileParent = file.getParent();
                String disableString = "(" + getString(R.string.zh_profile_mods_disable) + ")";

                FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                filesButton.setButtonVisibility(true, true, true, (fileName.endsWith(jarFileSuffix) || fileName.endsWith(disableJarFileSuffix)));
                filesButton.messageText = getString(R.string.zh_file_message);
                if (fileName.endsWith(jarFileSuffix))
                    filesButton.moreButtonText = getString(R.string.zh_profile_mods_disable);
                else if (fileName.endsWith(disableJarFileSuffix))
                    filesButton.moreButtonText = getString(R.string.zh_profile_mods_enable);

                FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileListView.refreshPath()), file);
                //检测后缀名，以设置正确的按钮
                if (fileName.endsWith(jarFileSuffix)) {
                    filesDialog.setMoreButtonClick(v -> {
                        File newFile = new File(fileParent, disableString + fileName + ".disabled");
                        boolean disable = file.renameTo(newFile);
                        if (disable) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_disabled) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                        filesDialog.dismiss();
                    });
                } else if (fileName.endsWith(disableJarFileSuffix)) {
                    filesDialog.setMoreButtonClick(v -> {
                        int index = fileName.indexOf(disableString);
                        if (index == -1) index = 0;
                        else index = disableString.length();
                        String newFileName = fileName.substring(index, fileName.lastIndexOf(disableJarFileSuffix));
                        if (!fileName.endsWith(jarFileSuffix))
                            newFileName += jarFileSuffix; //如果没有.jar结尾，那么默认加上.jar后缀

                        File newFile = new File(fileParent, newFileName);
                        boolean disable = file.renameTo(newFile);
                        if (disable) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_enabled) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                        filesDialog.dismiss();
                    });
                }

                filesDialog.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                if (file.isDirectory()) {
                    DeleteDialog deleteDialog = new DeleteDialog(requireContext(), () -> runOnUiThread(() -> mFileListView.refreshPath()), file);
                    deleteDialog.show();
                }
            }
        });

        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
        mAddModButton.setOnClickListener(v -> {
            String suffix = ".jar";
            Toast.makeText(requireActivity(), String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        });
        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mod_title));
            builder.setMessage(getString(R.string.zh_help_mod_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddModButton = view.findViewById(R.id.zh_files_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mTitleView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
        mFileListView.setFileIcon(FileIcon.MOD);
    }
}

