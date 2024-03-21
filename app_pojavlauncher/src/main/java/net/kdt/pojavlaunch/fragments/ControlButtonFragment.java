package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.CustomControlsActivity.BUNDLE_CONTROL_PATH;
import static net.kdt.pojavlaunch.Tools.deleteFileListener;
import static net.kdt.pojavlaunch.Tools.getFileName;
import static net.kdt.pojavlaunch.Tools.renameFileListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ControlButtonFragment extends Fragment {
    public static final String TAG = "ControlButtonFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private Button mReturnButton, mAddControlButton, mImportControlButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
    private TextView mFilePathView;
    private String mRootPath;

    public ControlButtonFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension("json"),
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

        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(true);
        mFileListView.lockPathAt(new File(mRootPath));
        mFileListView.setDialogTitleListener((title)->mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.preference_category_buttons));
                builder.setMessage(getString(R.string.zh_file_message));

                //加载
                DialogInterface.OnClickListener loadListener = (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), CustomControlsActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_CONTROL_PATH, file.getAbsolutePath());
                    intent.putExtras(bundle);

                    startActivity(intent);
                };

                builder.setPositiveButton(getString(R.string.global_delete), deleteFileListener(requireActivity(), mFileListView, file))
                        .setNegativeButton(getString(R.string.zh_file_rename), renameFileListener(requireActivity(), mFileListView, file))
                        .setNeutralButton(getString(R.string.zh_controls_load), loadListener);

                builder.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                Tools.shareFileAlertDialog(requireContext(), file);
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mImportControlButton.setOnClickListener(v -> {
            String suffix = ".json";
            Toast.makeText(requireActivity(),  String.format(getString(R.string.zh_file_add_file_tip), suffix), Toast.LENGTH_SHORT).show();
            openDocumentLauncher.launch(suffix);
        }); //限制.json文件
        mAddControlButton.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.zh_controls_create_new_title)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.zh_file_create_file, (dialog, which) -> {
                        File file = new File(mFileListView.getFullPath().getAbsolutePath(), editText.getText().toString() + ".json");
                        if (!file.exists()) {
                            boolean success;
                            try {
                                success = file.createNewFile();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if(success){
                                try (BufferedWriter optionFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                                    optionFileWriter.write("{\"version\":6}");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            mFileListView.refreshPath();
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.zh_file_create_file_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        });
        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_control_button_tile));
            builder.setMessage(getString(R.string.zh_help_control_button_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);
        });
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
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mImportControlButton = view.findViewById(R.id.zh_files_add_file_button);
        mAddControlButton = view.findViewById(R.id.zh_files_create_folder_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        mImportControlButton.setText(getString(R.string.zh_controls_import_control));
        mAddControlButton.setText(getString(R.string.zh_controls_create_new));
    }
}

