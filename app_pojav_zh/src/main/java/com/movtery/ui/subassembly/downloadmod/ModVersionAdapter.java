package com.movtery.ui.subassembly.downloadmod;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.ui.dialog.ModDependenciesDialog;
import com.movtery.utils.NumberWithUnits;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class ModVersionAdapter extends RecyclerView.Adapter<ModVersionAdapter.InnerHolder> implements TaskCountListener {
    private final ModDependencies.SelectedMod mod;
    private final ModDetail modDetail;
    private List<ModVersionGroup.ModVersionItem> mData;
    private boolean mTasksRunning;

    public ModVersionAdapter(ModDependencies.SelectedMod mod, ModDetail modDetail, List<ModVersionGroup.ModVersionItem> mData) {
        this.mod = mod;
        this.modDetail = modDetail;
        this.mData = mData;
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
        return mData != null ? mData.size() : 0;
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        Tools.runOnUiThread(() -> mTasksRunning = taskCount != 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ModVersionGroup.ModVersionItem> newData) {
        this.mData = newData;
        notifyDataSetChanged();
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

        public void setData(ModVersionGroup.ModVersionItem modVersionItem) {
            mTitle.setText(modVersionItem.getTitle());

            String downloadCountText = mainView.getContext().getString(R.string.zh_profile_mods_information_download_count) + " " +
                    NumberWithUnits.formatNumberWithUnit(modVersionItem.getDownload(), PojavZHTools.isEnglish());
            mDownloadCount.setText(downloadCountText);

            String modloaderText = mainView.getContext().getString(R.string.zh_profile_mods_information_modloader) + " ";
            if (modVersionItem.getModloaders() != null && !modVersionItem.getModloaders().isEmpty()) {
                modloaderText += modVersionItem.getModloaders();
            } else {
                modloaderText += mainView.getContext().getString(R.string.zh_unknown);
            }
            mModloaders.setText(modloaderText);

            mainView.setOnClickListener(v -> {
                if (mTasksRunning) {
                    Toast.makeText(mainView.getContext(), mainView.getContext().getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!modVersionItem.getModDependencies().isEmpty()) {
                    ModDependenciesDialog dependenciesDialog = new ModDependenciesDialog(
                            mainView.getContext(),
                            mod,
                            modVersionItem.getModDependencies(),
                            () -> mod.api.handleInstallation(mainView.getContext(), mod.isModpack, mod.modsPath, modDetail, modVersionItem));
                    dependenciesDialog.show();
                    return;
                }

                mod.api.handleInstallation(mainView.getContext(), mod.isModpack, mod.modsPath, modDetail, modVersionItem);
            });
        }
    }
}
