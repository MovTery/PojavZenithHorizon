package com.movtery.pojavzh.ui.subassembly.twolevellist;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

import java.util.List;

public class ModListAdapter extends RecyclerView.Adapter<ModListAdapter.InnerHolder> {
    private final ModListFragment fragment;
    private final List<ModListItemBean> mData;

    public ModListAdapter(ModListFragment fragment, List<ModListItemBean> mData) {
        this.fragment = fragment;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ModListAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod_download, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModListAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ModListItemBean> newData) {
        mData.clear();
        mData.addAll(newData);
        super.notifyDataSetChanged();
    }

    public List<ModListItemBean> getData() {
        return mData;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final TextView versionId;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            versionId = itemView.findViewById(R.id.mod_version_id);
        }

        public void setData(ModListItemBean modListItemBean) {
            mainView.setOnClickListener(v -> fragment.switchToChild(modListItemBean.getAdapter(), modListItemBean.title));

            versionId.setText(modListItemBean.title);
        }
    }
}
