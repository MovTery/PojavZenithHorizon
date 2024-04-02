package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.DIR_CUSTOM_MOUSE;
import static net.kdt.pojavlaunch.PojavZHTools.copyFileInBackground;
import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.isImage;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

import java.io.File;

public class CustomMouseFragment extends Fragment {
    public static final String TAG = "CustomMouseFragment";
    private ActivityResultLauncher<String[]> openDocumentLauncher;
    private Button mReturnButton, mAddFileButton, mRefreshButton;
    private ImageButton mHelpButton;
    private ImageView mMouseView;
    private FileListView mFileListView;

    public CustomMouseFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                        new CopyFile().execute(result);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        mFileListView.lockPathAt(mousePath());
        mFileListView.listFileAt(mousePath(), true);
        mFileListView.setShowFiles(true);
        mFileListView.setShowFolders(false);

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                refreshIcon(path, requireContext());
                String fileName = file.getName();

                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_file_tips));
                builder.setMessage(getString(R.string.zh_file_message));

                DialogInterface.OnClickListener chooseListener = (dialog, which) -> {
                    DEFAULT_PREF.edit().putString("custom_mouse", fileName).apply();
                    Toast.makeText(requireContext(), getString(R.string.zh_custom_mouse_added) + fileName, Toast.LENGTH_SHORT).show();
                };

                builder.setPositiveButton(getString(R.string.global_delete), deleteFileListener(requireActivity(), mFileListView, file, true))
                        .setNegativeButton(getString(R.string.zh_file_rename), renameFileListener(requireActivity(), mFileListView, file, true));

                if (isImage(file)) builder.setNeutralButton(getString(R.string.global_select), chooseListener);

                builder.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                PojavZHTools.shareFileAlertDialog(requireContext(), file);
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mAddFileButton.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"image/*"}));

        mRefreshButton.setOnClickListener(v -> mFileListView.listFileAt(mousePath(), true));
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_mouse_title));
            builder.setMessage(getString(R.string.zh_help_mouse_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    private File mousePath() {
        File path = new File(DIR_CUSTOM_MOUSE);
        if (!path.exists()) path.mkdirs();
        return path;
    }

    private void refreshIcon(String path, Context context) {
        Bitmap mouse = BitmapFactory.decodeFile(path);
        try {
            mMouseView.setImageBitmap(mouse);
        } catch (Exception e) {
            mMouseView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_file, context.getTheme()));
        }
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        TextView mFilePathView = view.findViewById(R.id.zh_files_current_path);
        mMouseView = view.findViewById(R.id.zh_files_icon);

        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);
        mFilePathView.setText(getString(R.string.zh_custom_mouse_title));
        mAddFileButton.setText(getString(R.string.zh_custom_mouse_add));
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
            mFileListView.listFileAt(mousePath(), true);
        }
    }
}
