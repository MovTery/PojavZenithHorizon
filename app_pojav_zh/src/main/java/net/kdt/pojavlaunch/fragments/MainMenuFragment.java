package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ADVANCED_FEATURES;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.ShareLogDialog;
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
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);
        Button mOpenInstanceDirButton = view.findViewById(R.id.zh_open_instance_dir_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mAboutButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), AboutFragment.class, AboutFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v->{
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext(), new File(Tools.DIR_GAME_HOME + "/latestlog.txt"));
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, Tools.DIR_GAME_HOME);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenInstanceDirButton.setOnClickListener(v -> {
            String path = PojavZHTools.getGameDirPath(getCurrentProfile().gameDir).getAbsolutePath();
            File file = new File(path);
            if (!file.exists()) file.mkdirs(); //必须保证此路径存在

            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, path);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenMainDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
        mOpenInstanceDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
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
