package com.movtery.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.dialog.EditTextDialog;
import com.movtery.ui.dialog.TipDialog;

import net.kdt.pojavlaunch.R;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ProfilePathAdapter extends RecyclerView.Adapter<ProfilePathAdapter.ViewHolder> {
    private List<ProfileItem> mData;
    private final Map<String, RadioButton> radioButtonMap = new TreeMap<>();
    private final RecyclerView view;
    private String currentId;

    public ProfilePathAdapter(RecyclerView view, List<ProfileItem> mData) {
        this.mData = mData;
        this.view = view;
    }

    @NonNull
    @Override
    public ProfilePathAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_path, parent, false);
        return new ViewHolder(view);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton mRadioButton;
        private final TextView mTitle, mPath;
        private final ImageButton mRenameButton, mDeleteButton;
        private final View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            mRadioButton = itemView.findViewById(R.id.zh_profile_path_radio_button);
            mTitle = itemView.findViewById(R.id.zh_profile_path_title);
            mPath = itemView.findViewById(R.id.zh_profile_path_path);
            mRenameButton = itemView.findViewById(R.id.zh_profile_path_rename);
            mDeleteButton = itemView.findViewById(R.id.zh_profile_path_delete);
        }

        public void setView(ProfileItem profileItem, int position) {
            currentId = DEFAULT_PREF.getString("launcherProfile", "default");
            radioButtonMap.put(profileItem.id, mRadioButton);

            if (Objects.equals(currentId, profileItem.id)) {
                setRadioButton(profileItem.id);
            }

            mTitle.setText(profileItem.title);
            mPath.setText(profileItem.path);

            View.OnClickListener onClickListener = v -> {
                currentId = profileItem.id;
                setRadioButton(profileItem.id);
                ProfilePathManager.setCurrentPathId(profileItem.id);
            };

            itemView.setOnClickListener(onClickListener);
            mRadioButton.setOnClickListener(onClickListener);

            mRenameButton.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = mRenameButton.getContext();

                    EditTextDialog editTextDialog = new EditTextDialog(context, context.getString(R.string.zh_rename), null, profileItem.title, null);
                    editTextDialog.setConfirm(v1 -> {
                        String string = editTextDialog.getEditBox().getText().toString();
                        if (string.isEmpty()) {
                            editTextDialog.getEditBox().setError(context.getString(R.string.global_error_field_empty));
                            return;
                        }

                        mData.remove(position);
                        mData.add(position, new ProfileItem(profileItem.id, string, profileItem.path));
                        updateData(mData);
                        editTextDialog.dismiss();
                    });
                    editTextDialog.show();
                }
            });

            mDeleteButton.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    Context context = mDeleteButton.getContext();
                    TipDialog.Builder builder = new TipDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.zh_profiles_path_delete_title));
                    builder.setMessage(context.getString(R.string.zh_profiles_path_delete_message));
                    builder.setCancelable(false);
                    builder.setConfirmClickListener(() -> {
                        if (Objects.equals(currentId, profileItem.id)) {
                            //如果删除的是当前选中的路径，那么将自动选择为默认路径
                            ProfilePathManager.setCurrentPathId("default");
                            setRadioButton("default");
                        }
                        mData.remove(position);
                        updateData(mData);
                    });

                    builder.buildDialog();
                }
            });

            if (profileItem.id.equals("default")) {
                mRenameButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
            }
        }

        private void setRadioButton(String id) {
            radioButtonMap.forEach((k, v) -> {
                if (Objects.equals(id, k)) {
                    v.toggle(); //遍历全部RadioButton，取消除去此id的全部RadioButton
                } else {
                    v.setChecked(false);
                }
            });
        }
    }
}
