package com.movtery.pojavzh.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathJsonObject;
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfileItem;
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathAdapter;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProfilePathManagerFragment extends FragmentWithAnim {
    public static final String TAG = "ProfilePathManagerFragment";
    private final List<ProfileItem> mData = new ArrayList<>();
    private View mPathLayout, mOperateLayout, mShadowView, mOperateView;
    private ProfilePathAdapter adapter;

    public ProfilePathManagerFragment() {
        super(R.layout.fragment_profile_path_manager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String value = (String) ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR);

        if (value != null && !value.isEmpty() && !isAddedPath(value)) {
            new EditTextDialog.Builder(requireContext())
                    .setTitle(R.string.zh_profiles_path_create_new_title)
                    .setConfirmListener(editBox -> {
                        String string = editBox.getText().toString();
                        if (string.isEmpty()) {
                            editBox.setError(getString(R.string.global_error_field_empty));
                            return false;
                        }

                        this.mData.add(new ProfileItem(UUID.randomUUID().toString(), string, value));
                        ProfilePathManager.save(this.mData);
                        refresh();

                        return true;
                    }).buildDialog();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        refreshData();

        mPathLayout = view.findViewById(R.id.path_layout);
        mOperateLayout = view.findViewById(R.id.operate_layout);
        mShadowView = view.findViewById(R.id.shadowView);

        mOperateView = view.findViewById(R.id.operate_view);

        RecyclerView pathList = view.findViewById(R.id.zh_profile_path);
        ImageButton refreshButton = view.findViewById(R.id.zh_profile_path_refresh_button);
        ImageButton createNewButton = view.findViewById(R.id.zh_profile_path_create_new_button);
        ImageButton returnButton = view.findViewById(R.id.zh_profile_path_return_button);

        ZHTools.setTooltipText(refreshButton, refreshButton.getContentDescription());
        ZHTools.setTooltipText(createNewButton, createNewButton.getContentDescription());
        ZHTools.setTooltipText(returnButton, returnButton.getContentDescription());

        adapter = new ProfilePathAdapter(this, pathList, this.mData);
        pathList.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_downwards)));
        pathList.setLayoutManager(new LinearLayoutManager(requireContext()));
        pathList.setAdapter(adapter);

        refreshButton.setOnClickListener(v -> refresh());
        createNewButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(FilesFragment.BUNDLE_SELECT_FOLDER_MODE, true);
            bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILE, false);
            bundle.putBoolean(FilesFragment.BUNDLE_REMOVE_LOCK_PATH, false);
            bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());

            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        });
        returnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        ViewAnimUtils.slideInAnim(this);
    }

    private void refresh() {
        refreshData();
        adapter.updateData(this.mData);
    }

    private void refreshData() {
        this.mData.clear();
        this.mData.add(new ProfileItem("default", getString(R.string.zh_profiles_path_default), Tools.DIR_GAME_HOME));

        try {
            String json;
            if (ZHTools.FILE_PROFILE_PATH.exists()) {
                json = Tools.read(ZHTools.FILE_PROFILE_PATH);
                if (json.isEmpty()) {
                    return;
                }
            } else {
                return;
            }

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            for (String key : jsonObject.keySet()) {
                ProfilePathJsonObject profilePathId = new Gson().fromJson(jsonObject.get(key), ProfilePathJsonObject.class);
                ProfileItem item = new ProfileItem(key, profilePathId.title, profilePathId.path);
                this.mData.add(item);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isAddedPath(String path) {
        for (ProfileItem mDatum : this.mData) {
            if (Objects.equals(mDatum.path, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mPathLayout, Techniques.BounceInDown);
        ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.BounceInLeft);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.BounceInLeft);
        ViewAnimUtils.setViewAnim(mOperateView, Techniques.FadeInLeft);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mPathLayout, Techniques.FadeOutUp);
        ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.FadeOutRight);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.FadeOutRight);
        super.slideOut(listener);
    }
}
