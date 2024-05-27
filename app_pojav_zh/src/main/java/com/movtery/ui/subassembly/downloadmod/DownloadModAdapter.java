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
        if (mData != null) {
            return mData.size();
        }
        return 0;
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
                flipArrow.setRotation(flipArrow.getRotation() == 180 ? 0 : 180);
                modlistView.setVisibility(modlistView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            String title = "Minecraft " + modVersionGroup.getVersionId();
            versionId.setText(title);

            refresh(modVersionGroup);
        }

        private void refresh(ModVersionGroup modVersionGroup) {
            PojavApplication.sExecutorService.execute(() -> runOnUiThread(() -> {
                progressBar.setVisibility(View.VISIBLE);

                ModVersionAdapter versionAdapter = new ModVersionAdapter(mModApi, modDetail, modVersionGroup.getModversionList(), isModpack, modsPath);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(modlistView.getContext());
                modlistView.setLayoutManager(layoutManager);
                modlistView.setAdapter(versionAdapter);

                progressBar.setVisibility(View.GONE);
            }));
        }
    }
}
