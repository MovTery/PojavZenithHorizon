package com.movtery.ui.subassembly.downloadmod;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.feature.ResourceManager;
import com.movtery.utils.NumberWithUnits;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class ModVersionAdapter extends RecyclerView.Adapter<ModVersionAdapter.InnerHolder> implements TaskCountListener {
    private final ModpackApi mModApi;
    private final ModDetail modDetail;
    private final List<ModVersionGroup.ModItem> mData;
    private final boolean isModpack;
    private final String modsPath;
    private boolean mTasksRunning;

    public ModVersionAdapter(ModpackApi api, ModDetail modDetail, List<ModVersionGroup.ModItem> mData, boolean isModpack, String modsPath) {
        this.mModApi = api;
        this.modDetail = modDetail;
        this.mData = mData;
        this.isModpack = isModpack;
        this.modsPath = modsPath;
    }

    @NonNull
    @Override
    public ModVersionAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod_version, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModVersionAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        Tools.runOnUiThread(() -> mTasksRunning = taskCount != 0);
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final TextView mTitle, mDownloadCount, mModloaders;
        public InnerHolder(@NonNull View itemView) {
            super(itemView);

            mainView = itemView;
            mTitle = itemView.findViewById(R.id.mod_title_textview);
            mDownloadCount = itemView.findViewById(R.id.zh_mod_download_count_textview);
            mModloaders = itemView.findViewById(R.id.zh_mod_modloader_textview);
        }

        public void setData(ModVersionGroup.ModItem modItem) {
            mTitle.setText(modItem.getTitle());
            //下载量
            String downloaderCount = ResourceManager.getString(R.string.zh_profile_mods_information_download_count) + " " + NumberWithUnits.formatNumberWithUnit(modItem.getDownload(),
                    //判断当前系统语言是否为英文
                    PojavZHTools.isEnglish());
            mDownloadCount.setText(downloaderCount);
            //Mod加载器
            String modloaderText = ResourceManager.getString(R.string.zh_profile_mods_information_modloader) + " ";
            if (modItem.getModloaders() != null && !modItem.getModloaders().isEmpty()) {
                modloaderText += modItem.getModloaders();
            } else {
                modloaderText += ResourceManager.getString(R.string.zh_unknown);
            }
            mModloaders.setText(modloaderText);

            mainView.setOnClickListener(v -> {
                if (!mTasksRunning) {
                    mModApi.handleInstallation(mainView.getContext(), isModpack, modsPath, modDetail, modItem);
                } else {
                    Toast.makeText(mainView.getContext(), mainView.getContext().getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
