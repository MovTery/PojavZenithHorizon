package com.movtery.ui.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movtery.ui.dialog.EditTextDialog;
import com.movtery.ui.subassembly.customprofilepath.ProfileItem;
import com.movtery.ui.subassembly.customprofilepath.ProfilePathAdapter;
import com.movtery.ui.subassembly.customprofilepath.ProfilePathJsonObject;
import com.movtery.ui.subassembly.customprofilepath.ProfilePathManager;
import com.movtery.ui.subassembly.recyclerview.SpacesItemDecoration;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.FileSelectorFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProfilePathManagerFragment extends Fragment {
    public static final String TAG = "ProfilePathManagerFragment";
    private final List<ProfileItem> mData = new ArrayList<>();
    private ProfilePathAdapter adapter;

    public ProfilePathManagerFragment() {
        super(R.layout.fragment_profile_path_manager);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String value = (String) ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR);

        if (value != null && !value.isEmpty() && !isAddedPath(value)) {
            EditTextDialog editTextDialog = new EditTextDialog(requireContext(), getString(R.string.zh_profiles_path_create_new_title), null, null, null);
            editTextDialog.setConfirm(v -> {
                String string = editTextDialog.getEditBox().getText().toString();
                if (string.isEmpty()) {
                    editTextDialog.getEditBox().setError(getString(R.string.global_error_field_empty));
                    return;
                }

                this.mData.add(new ProfileItem(UUID.randomUUID().toString(), string, value));
                ProfilePathManager.save(this.mData);
                refresh();
                editTextDialog.dismiss();
            });
            editTextDialog.show();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        refreshData();

        RecyclerView pathList = view.findViewById(R.id.zh_profile_path);
        Button refreshButton = view.findViewById(R.id.zh_profile_path_refresh_button);
        Button createNewButton = view.findViewById(R.id.zh_profile_path_create_new_button);
        Button returnButton = view.findViewById(R.id.zh_profile_path_return_button);

        adapter = new ProfilePathAdapter(pathList, this.mData);
        adapter.setOnItemClickListener(profileItem -> {
            ProfilePathManager.setCurrentPath(profileItem.path);
            PojavZHTools.onBackPressed(requireActivity());
        });

        pathList.setLayoutManager(new LinearLayoutManager(requireContext()));
        pathList.addItemDecoration(new SpacesItemDecoration(0, 0, 0, 8));
        pathList.setAdapter(adapter);

        refreshButton.setOnClickListener(v -> refresh());
        createNewButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(FileSelectorFragment.BUNDLE_SELECT_FOLDER, true);
            bundle.putBoolean(FileSelectorFragment.BUNDLE_SHOW_FILE, false);
            bundle.putBoolean(FileSelectorFragment.BUNDLE_REMOVE_LOCK_PATH, false);
            bundle.putString(FileSelectorFragment.BUNDLE_ROOT_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());

            Tools.swapFragment(requireActivity(),
                    FileSelectorFragment.class, FileSelectorFragment.TAG, bundle);
        });
        returnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));
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
            if (PojavZHTools.FILE_PROFILE_PATH.exists()) {
                json = Tools.read(PojavZHTools.FILE_PROFILE_PATH);
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
}
