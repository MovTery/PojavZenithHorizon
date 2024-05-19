package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.annotation.SuppressLint;
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

import com.movtery.utils.PasteFile;
import com.movtery.filelist.FileIcon;
import com.movtery.filelist.FileRecyclerView;
import com.movtery.filelist.FileSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.dialog.EditTextDialog;
import net.kdt.pojavlaunch.dialog.FilesDialog;

import java.io.File;

public class FilesFragment extends Fragment {
    public static final String TAG = "FilesFragment";
    public static final String BUNDLE_PATH = "bundle_path";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mCreateFolderButton, mPasteButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileRecyclerView mFileRecyclerView;
    private TextView mFilePathView;
    private String mPath;

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
        bindViews(view);
        parseBundle();

        mFileRecyclerView.lockPathAt(new File(mPath));
        mFileRecyclerView.setTitleListener((title) -> mFilePathView.setText(removeLockPath(title)));
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
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(null)); //不限制文件类型
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
        mPasteButton.setOnClickListener(v -> PasteFile.pasteFile(requireActivity(), mFileRecyclerView.getFullPath(), null, () -> runOnUiThread(() -> {
            mPasteButton.setVisibility(View.GONE);
            mFileRecyclerView.refreshPath();
        })));
        mRefreshButton.setOnClickListener(v -> mFileRecyclerView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_files_title));
            builder.setMessage(getString(R.string.zh_help_files_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private void showDialog(File file) {
        FilesDialog.FilesButton filesButton = new FilesDialog.FilesButton();
        filesButton.setButtonVisibility(true, true, !file.isDirectory(), true, true, false);

        int caciocavallo = file.getPath().indexOf("caciocavallo");
        int lwjgl3 = file.getPath().indexOf("lwjgl3");

        String message;
        if (file.isDirectory()) {
            message = getString(R.string.zh_file_folder_message);
        } else {
            message = getString(R.string.zh_file_message);
        }
        if (!(caciocavallo == -1 && lwjgl3 == -1))
            message += "\n" + getString(R.string.zh_file_message_main);

        filesButton.messageText = message;
        filesButton.moreButtonText = null;

        FilesDialog filesDialog = new FilesDialog(requireContext(), filesButton, () -> runOnUiThread(() -> mFileRecyclerView.refreshPath()), file);

        filesDialog.setCopyButtonClick(() -> mPasteButton.setVisibility(View.VISIBLE));

        filesDialog.show();
    }

    private String removeLockPath(String path) {
        return path.replace(mPath, ".");
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_create_folder_button);
        mPasteButton = view.findViewById(R.id.zh_files_paste_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileRecyclerView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
        mFileRecyclerView.setFileIcon(FileIcon.FILE);

        mPasteButton.setVisibility(PasteFile.PASTE_TYPE != null ? View.VISIBLE : View.GONE);
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mPath = bundle.getString(BUNDLE_PATH, DIR_GAME_HOME);
    }
}

