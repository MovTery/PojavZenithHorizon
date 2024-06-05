package com.movtery.ui.subassembly.customprofilepath;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.util.List;

public class ProfilePathAdapter extends RecyclerView.Adapter<ProfilePathAdapter.ViewHolder> {
    private List<ProfileItem> mData;
    private final RecyclerView view;
    private OnItemClickListener onItemClickListener;

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

        notifyDataSetChanged();
        this.view.scheduleLayoutAnimation();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(ProfileItem profileItem);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle, mPath;
        private final ImageButton mDeleteButton;
        private final View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            mTitle = itemView.findViewById(R.id.zh_profile_path_title);
            mPath = itemView.findViewById(R.id.zh_profile_path_path);
            mDeleteButton = itemView.findViewById(R.id.zh_profile_path_delete);
        }

        public void setView(ProfileItem profileItem, int position) {
            mTitle.setText(profileItem.title);
            mPath.setText(profileItem.path);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) onItemClickListener.onClick(profileItem);
            });

            mDeleteButton.setOnClickListener(v -> {
                if (!profileItem.id.equals("default")) {
                    mData.remove(position);
                    ProfilePathManager.delete(profileItem.id);
                    updateData(mData);
                }
            });

            if (profileItem.id.equals("default")) {
                mDeleteButton.setVisibility(View.GONE);
            }
        }
    }
}
