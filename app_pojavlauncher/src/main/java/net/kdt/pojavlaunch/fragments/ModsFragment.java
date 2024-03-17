package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.R;

import java.io.File;

public class ModsFragment extends Fragment {
    public static final String TAG = "ModsFragment";
    public static final String BUNDLE_ROOT_PATH = "root_path";
    private Button mSaveButton;
    private FileListView mFileListView;
    private TextView mFilePathView;
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
        mFileListView.setDialogTitleListener((title)-> mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mSaveButton.setOnClickListener(view1 -> requireActivity().onBackPressed());
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
        mSaveButton = view.findViewById(R.id.zh_mods_save_button);
        mFileListView = view.findViewById(R.id.zh_mods);
        mFilePathView = view.findViewById(R.id.zh_mods_current_path);
    }
}

