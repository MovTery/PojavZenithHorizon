package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.file.FileTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.profiles.ProfileIconCache;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProfileManagerFragment extends FragmentWithAnim {
    public static final String TAG = "ProfileManagerFragment";
    public static final String DELETED_PROFILE = "deleted_profile";
    private View mShortcutsLayout, mModdedLayout;
    private String mProfileKey;

    public ProfileManagerFragment() {
        super(R.layout.fragment_profile_manager);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        ViewAnimUtils.slideInAnim(this);
    }

    private void init(View view) {
        File gameDirPath = ZHTools.getGameDirPath(getCurrentProfile().gameDir);
        mProfileKey = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "");

        mShortcutsLayout = view.findViewById(R.id.shortcuts_layout);
        mModdedLayout = view.findViewById(R.id.modded_layout);

        Button modsButton = view.findViewById(R.id.zh_shortcuts_mods);
        Button instanceButton = view.findViewById(R.id.zh_instance_path);
        Button resourceButton = view.findViewById(R.id.zh_resource_path);
        Button worldButton = view.findViewById(R.id.zh_world_path);
        Button logsButton = view.findViewById(R.id.zh_logs_path);
        Button crashReportButton = view.findViewById(R.id.zh_crash_report_path);

        Button editButton = view.findViewById(R.id.zh_profile_edit);
        Button deleteButton = view.findViewById(R.id.zh_profile_delete);

        modsButton.setOnClickListener(v -> {
            File modsPath = new File(gameDirPath, "/mods");
            if (!modsPath.exists()) {
                FileTools.mkdirs(modsPath);
            }

            Bundle bundle = new Bundle();
            bundle.putString(ModsFragment.BUNDLE_ROOT_PATH, modsPath.getAbsolutePath());

            ZHTools.swapFragmentWithAnim(this, ModsFragment.class, ModsFragment.TAG, bundle);
        });

        instanceButton.setOnClickListener(v -> swapFilesFragment(gameDirPath, gameDirPath));
        resourceButton.setOnClickListener(v -> swapFilesFragment(gameDirPath, new File(gameDirPath, "/resourcepacks")));
        worldButton.setOnClickListener(v -> swapFilesFragment(gameDirPath, new File(gameDirPath, "/saves")));
        logsButton.setOnClickListener(v -> swapFilesFragment(gameDirPath, new File(gameDirPath, "/logs")));
        crashReportButton.setOnClickListener(v -> swapFilesFragment(gameDirPath, new File(gameDirPath, "/crash-reports")));

        editButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ProfileEditorFragment.class, ProfileEditorFragment.TAG, null));
        deleteButton.setOnClickListener(v -> new TipDialog.Builder(requireContext())
                .setTitle(R.string.zh_warning)
                .setMessage(R.string.zh_profile_manager_delete_message)
                .setConfirmClickListener(() -> {
                    if (LauncherProfiles.mainProfileJson.profiles.size() > 1) {
                        ProfileIconCache.dropIcon(mProfileKey);
                        LauncherProfiles.mainProfileJson.profiles.remove(mProfileKey);
                        LauncherProfiles.write(ProfilePathManager.getCurrentProfile());
                        ExtraCore.setValue(ExtraConstants.REFRESH_VERSION_SPINNER, DELETED_PROFILE);
                    }

                    Tools.removeCurrentFragment(requireActivity());
                })
                .buildDialog());
    }

    private void swapFilesFragment(File lockPath, File listPath) {
        if (!lockPath.exists()) {
            FileTools.mkdirs(lockPath);
        }
        if (!listPath.exists()) {
            FileTools.mkdirs(listPath);
        }

        Bundle bundle = new Bundle();
        bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, lockPath.getAbsolutePath());
        bundle.putString(FilesFragment.BUNDLE_LIST_PATH, listPath.getAbsolutePath());
        bundle.putBoolean(FilesFragment.BUNDLE_QUICK_ACCESS_PATHS, false);

        ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mShortcutsLayout, Techniques.BounceInRight));
        yoYos.add(ViewAnimUtils.setViewAnim(mModdedLayout, Techniques.BounceInLeft));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mShortcutsLayout, Techniques.FadeOutLeft));
        yoYos.add(ViewAnimUtils.setViewAnim(mModdedLayout, Techniques.FadeOutRight));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }
}
