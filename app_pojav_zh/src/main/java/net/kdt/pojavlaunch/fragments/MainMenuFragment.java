package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kdt.mcgui.mcVersionSpinner;
import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.feature.accounts.AccountUpdateListener;
import com.movtery.pojavzh.ui.fragment.AboutFragment;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.ControlButtonFragment;
import com.movtery.pojavzh.ui.fragment.FilesFragment;

import com.movtery.pojavzh.ui.fragment.ProfileManagerFragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.movtery.pojavzh.ui.dialog.ShareLogDialog;
import com.movtery.pojavzh.ui.fragment.ProfilePathManagerFragment;
import com.movtery.pojavzh.ui.subassembly.account.AccountViewWrapper;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

public class MainMenuFragment extends FragmentWithAnim implements TaskCountListener, AccountUpdateListener {
    public static final String TAG = "MainMenuFragment";
    private AccountViewWrapper accountViewWrapper;
    private ImageButton mPathManagerButton, mManagerProfileButton;
    private Button mPlayButton;
    private mcVersionSpinner mVersionSpinner;
    private View mMenuLayout, mPlayLayout, mPlayButtonsLayout;
    private boolean mTasksRunning;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindValues(view);
        ProgressKeeper.addTaskCountListener(this);

        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);

        mAboutButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, AboutFragment.class, AboutFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        mPathManagerButton.setOnClickListener(v -> {
            if (!mTasksRunning) {
                checkPermissions(R.string.zh_profiles_path_title, () -> {
                    ViewAnimUtils.setViewAnim(mPathManagerButton, Animations.Pulse);
                    ZHTools.swapFragmentWithAnim(this, ProfilePathManagerFragment.class, ProfilePathManagerFragment.TAG, null);
                });
            } else {
                ViewAnimUtils.setViewAnim(mPathManagerButton, Animations.Shake);
                runOnUiThread(() -> Toast.makeText(requireContext(), R.string.zh_profiles_path_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        });
        mManagerProfileButton.setOnClickListener(v -> {
            ViewAnimUtils.setViewAnim(mManagerProfileButton, Animations.Pulse);
            ZHTools.swapFragmentWithAnim(this, ProfileManagerFragment.class, ProfileManagerFragment.TAG, null);
        });

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext());
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathAndUrlManager.DIR_GAME_HOME);
            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        });
    }

    private void bindValues(View view) {
        mMenuLayout = view.findViewById(R.id.launcher_menu);
        mPlayLayout = view.findViewById(R.id.play_layout);
        mPlayButtonsLayout = view.findViewById(R.id.play_buttons_layout);
        mPathManagerButton = view.findViewById(R.id.path_manager_button);
        mManagerProfileButton = view.findViewById(R.id.manager_profile_button);
        mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);
        accountViewWrapper = new AccountViewWrapper(view.findViewById(R.id.view_account));
        accountViewWrapper.refreshAccountInfo();
        mVersionSpinner.setParentFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVersionSpinner != null) mVersionSpinner.reloadProfiles();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = taskCount != 0;
    }

    @Override
    public void onUpdate() {
        if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(mMenuLayout, Animations.BounceInDown))
                .apply(new AnimPlayer.Entry(mPlayLayout, Animations.BounceInLeft))
                .apply(new AnimPlayer.Entry(mPlayButtonsLayout, Animations.BounceEnlarge));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(mMenuLayout, Animations.FadeOutUp))
                .apply(new AnimPlayer.Entry(mPlayLayout, Animations.FadeOutRight))
                .apply(new AnimPlayer.Entry(mPlayButtonsLayout, Animations.BounceShrink));
    }
}
