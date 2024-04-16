package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.FilesDialog;
import net.kdt.pojavlaunch.prefs.SLPreferences;

import java.io.File;

public class PrefsFragment extends Fragment {
    public static final String TAG = "PrefsFragment";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mCreateNewButton, mImportPrefsButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
    private TextView mTitleView;

    public PrefsFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("prefs"),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();

                        PojavApplication.sExecutorService.execute(() -> {
                            copyFileInBackground(requireContext(), result, mFileListView.getFullPath().getAbsolutePath());

                            runOnUiThread(() -> {
                                Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
                                mFileListView.refreshPath();
                                refreshFileCount();
                            });
                        });
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);

        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(false);
        mFileListView.lockPathAt(prefsPath());
        mFileListView.refreshPath();

        refreshFileCount();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
                filesButton.setButtonVisibility(true, true, true, true);

                filesButton.messageText = getString(R.string.zh_prefs_message);
                filesButton.moreButtonText = getString(R.string.global_load);

                FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, mFileListView, file);
                filesDialog.setMoreButtonClick(v -> PojavApplication.sExecutorService.execute(() -> {
                    try {
                        SLPreferences.load(file);
                        runOnUiThread(() -> Toast.makeText(requireContext(), getString(R.string.zh_prefs_loaded), Toast.LENGTH_LONG).show());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    filesDialog.dismiss();
                })); //在新的线程中加载
                filesDialog.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {}
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mImportPrefsButton.setOnClickListener(v -> {
            String suffix = ".prefs";
            Toast.makeText(requireActivity(),  String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        });

        mCreateNewButton.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.zh_prefs_create_new_title)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.zh_create, (dialog, which) -> PojavApplication.sExecutorService.execute(() -> {
                        File prefsFile = new File(PojavZHTools.DIR_PREFS, "/" + editText.getText().toString() + ".prefs");
                        if (!prefsFile.exists()) {
                            //在新的线程中创建
                            try {
                                SLPreferences.save(prefsFile);

                                runOnUiThread(() -> {
                                    mFileListView.refreshPath();
                                    refreshFileCount();
                                });
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(requireContext(), getString(R.string.zh_file_create_file_invalid), Toast.LENGTH_SHORT).show());
                        }
                    })).show();
        });
        mRefreshButton.setOnClickListener(v -> {
            mFileListView.refreshPath();
            refreshFileCount();
        });
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_prefs_title));
            builder.setMessage(getString(R.string.zh_help_prefs_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private File prefsPath() {
        File ctrlPath = new File(PojavZHTools.DIR_PREFS);
        if (!ctrlPath.exists()) ctrlPath.mkdirs();
        return ctrlPath;
    }

    private void refreshFileCount() {
        String text = getString(R.string.zh_main_prefs) + " ( " + getString(R.string.zh_file_total) + getFileCount() + " )";
        mTitleView.setText(text);
    }

    private int getFileCount() {
        return mFileListView.getMainLv().getAdapter().getCount();
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mImportPrefsButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateNewButton = view.findViewById(R.id.zh_files_create_folder_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mTitleView = view.findViewById(R.id.zh_files_current_path);

        mImportPrefsButton.setText(getString(R.string.zh_prefs_import_prefs));
        mCreateNewButton.setText(getString(R.string.zh_prefs_create_new));

        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
    }
}

