package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    private static final int REQUEST_CODE_GET_FILE = 114;
    private Button mSaveButton, mSelectModButton;
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

                DialogInterface.OnClickListener renameListener = (dialog, which) -> {
                    AlertDialog.Builder renameBuilder = new AlertDialog.Builder(requireActivity());
                    EditText input = new EditText(requireActivity());
                    input.setText(fileName);
                    renameBuilder.setTitle(getString(R.string.zh_profile_mods_rename));
                    renameBuilder.setView(input);
                    renameBuilder.setPositiveButton(getString(R.string.zh_profile_mods_rename), (dialog1, which1) -> {
                        String newName = input.getText().toString();
                        if (!newName.isEmpty()) {
                            File newFile = new File(fileParent, newName);
                            boolean renamed = file.renameTo(newFile);
                            if (renamed) {
                                Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_renamed) + file.getName() + " -> " + newName, Toast.LENGTH_SHORT).show();
                                mFileListView.refreshPath();
                            }
                        } else {
                            Toast.makeText(requireActivity(), getString(R.string.zh_profile_mods_rename_empty), Toast.LENGTH_SHORT).show();
                        }
                    });
                    renameBuilder.setNegativeButton(getString(R.string.zh_profile_mods_cancel), null);
                    renameBuilder.show();
                };

                builder.setPositiveButton(getString(R.string.global_delete), deleteListener)
                        .setNegativeButton(getString(R.string.zh_profile_mods_rename), renameListener);
                if (file.getName().endsWith(".jar")) {
                    builder.setNeutralButton(getString(R.string.zh_profile_mods_disable), disableListener);
                } else if (file.getName().endsWith(".d")) {
                    builder.setNeutralButton(getString(R.string.zh_profile_mods_enable), enableListener);
                }

                builder.show();
            }
        });

        mSaveButton.setOnClickListener(view1 -> requireActivity().onBackPressed());
        mSelectModButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/jar"); // 设置MIME类型为jar
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_GET_FILE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                //使用AsyncTask在后台线程中执行文件复制
                new CopyFile().execute(fileUri);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            File file = new File(fileUri.getPath());
            File outputFile = new File(mRootPath, file.getName());
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
            Toast.makeText(requireContext(), getString(R.string.zh_profile_mods_added_mod), Toast.LENGTH_SHORT).show();
        }
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath);
    }

    private void bindViews(@NonNull View view) {
        mSaveButton = view.findViewById(R.id.zh_mods_save_button);
        mSelectModButton = view.findViewById(R.id.zh_select_mod_button);
        mFileListView = view.findViewById(R.id.zh_mods);
    }
}

