package com.movtery.ui.subassembly.downloadmod;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;

import java.util.List;

public class DownloadModAdapter extends RecyclerView.Adapter<DownloadModAdapter.InnerHolder> {
    private final ModpackApi mModApi;
    private final ModDetail modDetail;
    private final List<ModVersionGroup> mData;
    private final boolean isModpack;
    private final String modsPath;

    public DownloadModAdapter(ModpackApi api, ModDetail modDetail, List<ModVersionGroup> mData, boolean isModpack, String modsPath) {
        this.mModApi = api;
        this.modDetail = modDetail;
        this.mData = mData;
        this.isModpack = isModpack;
        this.modsPath = modsPath;
    }

    @NonNull
    @Override
    public DownloadModAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod_download, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadModAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
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
        }

        public void setData(ModVersionGroup modVersionGroup) {
            mainView.setOnClickListener(v -> {
                modVersionGroup.setUnfold(!modVersionGroup.isUnfold()); // 反转展开状态
                refreshState(modVersionGroup);
            });

            String title = "Minecraft " + modVersionGroup.getVersionId();
            versionId.setText(title);

            refresh(modVersionGroup);
            refreshState(modVersionGroup);
        }

        private void refresh(ModVersionGroup modVersionGroup) {
            progressBar.setVisibility(View.VISIBLE);

            PojavApplication.sExecutorService.execute(() -> {
                List<ModVersionGroup.ModItem> modVersionList = modVersionGroup.getModversionList();

                runOnUiThread(() -> {
                    ModVersionAdapter versionAdapter = (ModVersionAdapter) modlistView.getAdapter();
                    if (versionAdapter == null) {
                        versionAdapter = new ModVersionAdapter(mModApi, modDetail, modVersionList, isModpack, modsPath);
                        modlistView.setLayoutManager(new LinearLayoutManager(modlistView.getContext()));
                        modlistView.setAdapter(versionAdapter);
                    } else {
                        versionAdapter.updateData(modVersionList);
                    }

                    progressBar.setVisibility(View.GONE);
                });
            });
        }

        private void refreshState(ModVersionGroup modVersionGroup) { // 刷新状态
            flipArrow.animate().rotation(modVersionGroup.isUnfold() ? 0 : 180).setDuration(150).start();
            modlistView.setVisibility(modVersionGroup.isUnfold() ? View.VISIBLE : View.GONE);
        }
    }
}
