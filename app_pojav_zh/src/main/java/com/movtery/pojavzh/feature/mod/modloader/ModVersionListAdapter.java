package com.movtery.pojavzh.feature.mod.modloader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.OptiFineUtils;

import java.util.List;

public class ModVersionListAdapter extends RecyclerView.Adapter<ModVersionListAdapter.ViewHolder> {
    private final List<?> mData;
    private OnItemClickListener onItemClickListener;
    private int iconDrawable = 0;

    public ModVersionListAdapter(List<?> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public ModVersionListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModVersionListAdapter.ViewHolder holder, int position) {
        holder.setView(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setIconDrawable(int iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public interface OnItemClickListener {
        void onClick(Object version);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final TextView versionName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            ImageView icon = itemView.findViewById(R.id.zh_file_image);
            versionName = itemView.findViewById(R.id.zh_file_name);

            if (iconDrawable != 0) icon.setImageResource(iconDrawable);
            itemView.findViewById(R.id.zh_file_check).setVisibility(View.GONE);
        }

        public void setView(Object version) {
            if (version instanceof OptiFineUtils.OptiFineVersion) {
                versionName.setText(((OptiFineUtils.OptiFineVersion) version).versionName);
            } else if (version instanceof String) {
                versionName.setText((CharSequence) version);
            }
            mainView.setOnClickListener(v -> {
                if (onItemClickListener != null) onItemClickListener.onClick(version);
            });
        }
    }
}
