package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.shareLog;
import static net.kdt.pojavlaunch.fragments.ControlButtonFragment.BUNDLE_ROOT_PATH;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenDirButton = view.findViewById(R.id.zh_open_dir_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mAboutButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), AboutFragment.class, AboutFragment.TAG ,true, new Bundle()));
        mCustomControlButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_ROOT_PATH, Tools.CTRLMAP_PATH);

            Tools.swapFragment(requireActivity(),
                    ControlButtonFragment.class, ControlButtonFragment.TAG, true, bundle);
        });
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v->{
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener((v) -> shareLog(requireContext()));

        mOpenDirButton.setOnClickListener(v -> {
            boolean storagePermAllowed = (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 29 ||
                    ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && Tools.checkStorageRoot(requireContext());
            File dir = new File(storagePermAllowed ? Tools.DIR_GAME_HOME : Tools.DIR_DATA);
            if (dir.exists()) {
                Bundle bundle = new Bundle();
                bundle.putString(FilesFragment.BUNDLE_ROOT_PATH, dir.toString());
                bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILES, true);
                bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FOLDERS, true);

                Tools.swapFragment(requireActivity(),
                        FilesFragment.class, FilesFragment.TAG, true, bundle);
            } else {
                Toast.makeText(requireContext(), getString(R.string.zh_file_does_not_exist), Toast.LENGTH_SHORT).show();
            }
        });

        mAboutButton.setOnLongClickListener((v)->{
            Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, true, null);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }
}
