package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.getFileName;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FilesFragment extends Fragment {
    public static final String TAG = "FilesFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    public static final String BUNDLE_SHOW_FILES = "show_files";
    public static final String BUNDLE_SHOW_FOLDERS = "show_folders";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mCreateFolderButton, mRefreshButton;
    private FileListView mFileListView;
    private TextView mFilePathView;
    private String mRootPath;
    private boolean mShowFiles, mShowFolders;

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

        mFileListView.setShowFiles(mShowFiles);
        mFileListView.setShowFolders(mShowFolders);
        mFileListView.lockPathAt(new File(mRootPath));
        mFileListView.setDialogTitleListener((title)->mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                String fileName = file.getName();
                String fileParent = file.getParent();
                int caciocavallo = file.getPath().indexOf("caciocavallo");
                int lwjgl3 = file.getPath().indexOf("lwjgl3");
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_file_tips));
                if (caciocavallo == -1 && lwjgl3 == -1) builder.setMessage(getString(R.string.zh_file_message));
                else builder.setMessage(getString(R.string.zh_file_message) + "\n" + getString(R.string.zh_file_message_main));

                DialogInterface.OnClickListener deleteListener = (dialog, which) -> {
                    // 显示确认删除的对话框
                    AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(requireActivity());

                    deleteConfirmation.setTitle(getString(R.string.zh_file_tips));
                    deleteConfirmation.setMessage(getString(R.string.zh_file_delete) + "\n" + fileName);

                    deleteConfirmation.setPositiveButton(getString(R.string.global_delete), (dialog1, which1) -> {
                        boolean deleted = file.delete();
                        if (deleted) {
                            Toast.makeText(requireActivity(), getString(R.string.zh_file_deleted) + fileName, Toast.LENGTH_SHORT).show();
                        }
                        mFileListView.refreshPath();
                    });

                    deleteConfirmation.setNegativeButton(getString(R.string.zh_cancel), null);
                    deleteConfirmation.show();
                };

                DialogInterface.OnClickListener renameListener = (dialog, which) -> { //重命名
                    AlertDialog.Builder renameBuilder = new AlertDialog.Builder(requireActivity());
                    String suffix = fileName.substring(fileName.lastIndexOf('.')); //防止修改后缀名，先将后缀名分离出去
                    EditText input = new EditText(requireActivity());
                    input.setText(fileName.substring(0, fileName.indexOf(suffix)));
                    renameBuilder.setTitle(getString(R.string.zh_file_rename));
                    renameBuilder.setView(input);
                    renameBuilder.setPositiveButton(getString(R.string.zh_file_rename), (dialog1, which1) -> {
                        String newName = input.getText().toString();
                        if (!newName.isEmpty()) {
                            File newFile = new File(fileParent, newName + suffix);
                            boolean renamed = file.renameTo(newFile);
                            if (renamed) {
                                Toast.makeText(requireActivity(), getString(R.string.zh_file_renamed) + file.getName() + " -> " + newName + suffix, Toast.LENGTH_SHORT).show();
                                mFileListView.refreshPath();
                            }
                        } else {
                            Toast.makeText(requireActivity(), getString(R.string.zh_file_rename_empty), Toast.LENGTH_SHORT).show();
                        }
                    });
                    renameBuilder.setNegativeButton(getString(R.string.zh_cancel), null);
                    renameBuilder.show();
                };

                //分享
                DialogInterface.OnClickListener shareListener = (dialog, which) -> Tools.shareFile(requireContext(), file.getName(), file.getAbsolutePath());

                builder.setPositiveButton(getString(R.string.global_delete), deleteListener)
                        .setNegativeButton(getString(R.string.zh_file_rename), renameListener)
                        .setNeutralButton(getString(R.string.zh_file_share), shareListener);

                builder.show();
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
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String fileName = getFileName(requireContext(), fileUri);
            File outputFile = new File(mFileListView.getFullPath().getAbsolutePath(), fileName);
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri)) {
                if (inputStream != null) {
                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        return path.replace(mRootPath, ".");
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
        mShowFiles = bundle.getBoolean(BUNDLE_SHOW_FILES, mShowFiles);
        mShowFolders = bundle.getBoolean(BUNDLE_SHOW_FOLDERS, mShowFolders);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_create_folder_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);
    }
}

