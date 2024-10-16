package com.movtery.pojavzh.ui.subassembly.customprofilepath;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.FilesFragment;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.ItemProfilePathBinding;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ProfilePathAdapter extends RecyclerView.Adapter<ProfilePathAdapter.ViewHolder> {
    private final FragmentWithAnim fragment;
    private final Map<String, RadioButton> radioButtonMap = new TreeMap<>();
    private final RecyclerView view;
    private List<ProfileItem> mData;
    private String currentId;

    public ProfilePathAdapter(FragmentWithAnim fragment, RecyclerView view, List<ProfileItem> mData) {
        this.fragment = fragment;
        this.mData = mData;
        this.view = view;
        this.currentId = AllSettings.Companion.getLauncherProfile();
    }

    @NonNull
    @Override
    public ProfilePathAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemProfilePathBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePathAdapter.ViewHolder holder, int position) {
        holder.setView(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ProfileItem> mData) {
        this.mData = mData;

        ProfilePathManager.save(this.mData);

        notifyDataSetChanged();
        this.view.scheduleLayoutAnimation();
    }

    private void setPathId(String id) {
        currentId = id;
        ProfilePathManager.setCurrentPathId(id);
        updateRadioButtonState(id);
    }

    private void updateRadioButtonState(String id) {
        //遍历全部RadioButton，取消除去此id的全部RadioButton
        for (Map.Entry<String, RadioButton> entry : radioButtonMap.entrySet()) {
            entry.getValue().setChecked(Objects.equals(id, entry.getKey()));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfilePathBinding binding;

        public ViewHolder(@NonNull ItemProfilePathBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setView(ProfileItem profileItem, int position) {
            radioButtonMap.put(profileItem.id, binding.radioButton);
            binding.title.setText(profileItem.title);
            binding.path.setText(profileItem.path);

            View.OnClickListener onClickListener = v -> setPathId(profileItem.id);
            itemView.setOnClickListener(onClickListener);
            binding.radioButton.setOnClickListener(onClickListener);

            binding.rename.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = binding.rename.getContext();

                    new EditTextDialog.Builder(context)
                            .setTitle(R.string.generic_rename)
                            .setEditText(profileItem.title)
                            .setConfirmListener(editBox -> {
                                String string = editBox.getText().toString();
                                if (string.isEmpty()) {
                                    editBox.setError(context.getString(R.string.generic_error_field_empty));
                                    return false;
                                }

                                mData.get(position).title = string;
                                updateData(mData);
                                return true;
                            }).buildDialog();
                }
            });

            binding.visit.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
                bundle.putString(FilesFragment.BUNDLE_LIST_PATH, profileItem.path);
                ZHTools.swapFragmentWithAnim(fragment, FilesFragment.class, FilesFragment.TAG, bundle);
            });

            binding.delete.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = binding.delete.getContext();
                    new TipDialog.Builder(context)
                            .setTitle(context.getString(R.string.profiles_path_delete_title))
                            .setMessage(R.string.profiles_path_delete_message)
                            .setCancelable(false)
                            .setConfirmClickListener(() -> {
                                if (Objects.equals(currentId, profileItem.id)) {
                                    //如果删除的是当前选中的路径，那么将自动选择为默认路径
                                    setPathId("default");
                                }
                                mData.remove(position);
                                updateData(mData);
                            }).buildDialog();
                }
            });

            if (profileItem.id.equals("default")) {
                binding.rename.setVisibility(View.GONE);
                binding.delete.setVisibility(View.GONE);
            }

            if (Objects.equals(currentId, profileItem.id)) {
                updateRadioButtonState(profileItem.id);
            }
        }
    }
}
