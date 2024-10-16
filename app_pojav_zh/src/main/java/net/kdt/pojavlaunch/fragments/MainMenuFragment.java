package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.event.single.AccountUpdateEvent;
import com.movtery.pojavzh.event.single.LaunchGameEvent;
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

import net.kdt.pojavlaunch.databinding.FragmentLauncherBinding;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainMenuFragment extends FragmentWithAnim implements TaskCountListener {
    public static final String TAG = "MainMenuFragment";
    private FragmentLauncherBinding binding;
    private AccountViewWrapper accountViewWrapper;
    private boolean mTasksRunning;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(getLayoutInflater());
        accountViewWrapper = new AccountViewWrapper(binding.viewAccount.getRoot());
        accountViewWrapper.refreshAccountInfo();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.mcVersionSpinner.setParentFragment(this);
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
        binding.pathManagerButton.setOnClickListener(v -> {
            if (!mTasksRunning) {
                checkPermissions(R.string.profiles_path_title, () -> {
                    ViewAnimUtils.setViewAnim(binding.pathManagerButton, Animations.Pulse);
                    ZHTools.swapFragmentWithAnim(this, ProfilePathManagerFragment.class, ProfilePathManagerFragment.TAG, null);
                });
            } else {
                ViewAnimUtils.setViewAnim(binding.pathManagerButton, Animations.Shake);
                runOnUiThread(() -> Toast.makeText(requireContext(), R.string.profiles_path_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        });
        binding.managerProfileButton.setOnClickListener(v -> {
            ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Pulse);
            ZHTools.swapFragmentWithAnim(this, ProfileManagerFragment.class, ProfileManagerFragment.TAG, null);
        });

        binding.playButton.setOnClickListener(v -> EventBus.getDefault().post(new LaunchGameEvent()));

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

    @Override
    public void onResume() {
        super.onResume();
        binding.mcVersionSpinner.reloadProfiles();
    }

    @Subscribe()
    public void onAccountUpdate(AccountUpdateEvent event) {
        if (accountViewWrapper != null) accountViewWrapper.refreshAccountInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.launcherMenu, Animations.BounceInDown))
                .apply(new AnimPlayer.Entry(binding.playLayout, Animations.BounceInLeft))
                .apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceEnlarge));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.launcherMenu, Animations.FadeOutUp))
                .apply(new AnimPlayer.Entry(binding.playLayout, Animations.FadeOutRight))
                .apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceShrink));
    }
}
