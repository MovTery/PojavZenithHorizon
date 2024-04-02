package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;
import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
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

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;

import java.io.File;

public class FilesFragment extends Fragment {
    public static final String TAG = "FilesFragment";
    public static final String BUNDLE_PATH = "bundle_path";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mCreateFolderButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
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
                        //使用AsyncTask在后台线程中执行文件复制
                        new CopyFile().execute(result);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        mFileListView.lockPathAt(new File(mPath));
        mFileListView.setDialogTitleListener((title)->mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                int caciocavallo = file.getPath().indexOf("caciocavallo");
                int lwjgl3 = file.getPath().indexOf("lwjgl3");

                builder.setTitle(getString(R.string.zh_file_tips));
                if (caciocavallo == -1 && lwjgl3 == -1) builder.setMessage(getString(R.string.zh_file_message));
                else builder.setMessage(getString(R.string.zh_file_message) + "\n" + getString(R.string.zh_file_message_main));

                //分享
                DialogInterface.OnClickListener shareListener = (dialog, which) -> shareFile(requireContext(), file.getName(), file.getAbsolutePath());

                builder.setPositiveButton(getString(R.string.global_delete), deleteFileListener(requireActivity(), mFileListView, file, false))
                        .setNegativeButton(getString(R.string.zh_file_rename), renameFileListener(requireActivity(), mFileListView, file, false))
                        .setNeutralButton(getString(R.string.zh_file_share), shareListener);

                builder.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                PojavZHTools.shareFileAlertDialog(requireContext(), file);
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(null)); //不限制文件类型
        mCreateFolderButton.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.folder_dialog_insert_name)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.folder_dialog_create, (dialog, which) -> {
                        File folder = new File(mFileListView.getFullPath(), editText.getText().toString());
                        boolean success = folder.mkdir();
                        if(success){
                            mFileListView.listFileAt(new File(mFileListView.getFullPath(),editText.getText().toString()));
                        }else{
                            mFileListView.refreshPath();
                        }
                    }).show();
        });
        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_files_tile));
            builder.setMessage(getString(R.string.zh_help_files_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            copyFileInBackground(requireContext(), uris, mFileListView.getFullPath().getAbsolutePath());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
            mFileListView.refreshPath();
        }
    }

    private String removeLockPath(String path){
        return path.replace(mPath, ".");
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_create_folder_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_icon).setVisibility(View.GONE);
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mPath = bundle.getString(BUNDLE_PATH, DIR_GAME_HOME);
    }
}

