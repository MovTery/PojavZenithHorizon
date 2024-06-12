package com.movtery.ui.subassembly.collapsibleexpandlist;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;

import java.util.List;

public class CollapsibleExpandAdapter extends RecyclerView.Adapter<CollapsibleExpandAdapter.InnerHolder> {
    private final List<CollapsibleExpandItemBean> mData;

    public CollapsibleExpandAdapter(List<CollapsibleExpandItemBean> mData) {
        this.mData = mData;
    }


    @NonNull
    @Override
    public CollapsibleExpandAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod_download, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollapsibleExpandAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<CollapsibleExpandItemBean> newData) {
        mData.clear();
        mData.addAll(newData);
        super.notifyDataSetChanged();
    }

    public List<CollapsibleExpandItemBean> getData() {
        return mData;
    }

    public static class InnerHolder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final TextView versionId;
        private final ImageView flipArrow;
        private final RecyclerView modlistView;
        private final ProgressBar progressBar;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            versionId = itemView.findViewById(R.id.mod_version_id);
            flipArrow = itemView.findViewById(R.id.mod_flip_arrow);
            modlistView = itemView.findViewById(R.id.mod_recycler_view);
            progressBar = itemView.findViewById(R.id.mod_version_loading);

            modlistView.setLayoutManager(new LinearLayoutManager(modlistView.getContext()));
            if (PREF_ANIMATION) modlistView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(modlistView.getContext(), R.anim.fade_downwards)));
        }

        public void setData(CollapsibleExpandItemBean collapsibleExpandItemBean) {
            mainView.setOnClickListener(v -> {
                collapsibleExpandItemBean.setUnfold(!collapsibleExpandItemBean.isUnfold()); // 反转展开状态
                refreshState(collapsibleExpandItemBean);
            });

            versionId.setText(collapsibleExpandItemBean.getTitle());

            refresh(collapsibleExpandItemBean);
            refreshState(collapsibleExpandItemBean);
        }

        private void refresh(CollapsibleExpandItemBean collapsibleExpandItemBean) {
            progressBar.setVisibility(View.VISIBLE);

            PojavApplication.sExecutorService.execute(() -> runOnUiThread(() -> {
                modlistView.setAdapter(collapsibleExpandItemBean.getAdapter()); //确保适配器设置正确

                if (PREF_ANIMATION) modlistView.scheduleLayoutAnimation();
                progressBar.setVisibility(View.GONE);
            }));
        }

        private void refreshState(CollapsibleExpandItemBean collapsibleExpandItemBean) { // 刷新状态
            flipArrow.animate().rotation(collapsibleExpandItemBean.isUnfold() ? 0 : 180).setDuration(150).start();
            modlistView.setVisibility(collapsibleExpandItemBean.isUnfold() ? View.VISIBLE : View.GONE);
            if (PREF_ANIMATION) modlistView.scheduleLayoutAnimation();
        }
    }
}
