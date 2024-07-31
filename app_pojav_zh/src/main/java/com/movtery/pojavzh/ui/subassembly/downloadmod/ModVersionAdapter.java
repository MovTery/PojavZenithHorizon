package com.movtery.pojavzh.ui.subassembly.downloadmod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.ui.dialog.ModDependenciesDialog;
import com.movtery.pojavzh.utils.NumberWithUnits;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.util.List;

public class ModVersionAdapter extends RecyclerView.Adapter<ModVersionAdapter.InnerHolder> implements TaskCountListener {
    private final ModDependencies.SelectedMod mod;
    private final ModDetail modDetail;
    private final List<ModVersionItem> mData;
    private boolean mTasksRunning;

    public ModVersionAdapter(ModDependencies.SelectedMod mod, ModDetail modDetail, List<ModVersionItem> mData) {
        this.mod = mod;
        this.modDetail = modDetail;
        this.mData = mData;
        ProgressKeeper.addTaskCountListener(this);
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
        mTasksRunning = taskCount != 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final View mainView;
        private final ImageView mImageView;
        private final TextView mTitle, mDownloadCount, mModloaders, mReleaseType;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mainView = itemView;
            mImageView = itemView.findViewById(R.id.mod_download_imageview);
            mTitle = itemView.findViewById(R.id.mod_title_textview);
            mDownloadCount = itemView.findViewById(R.id.zh_mod_download_count_textview);
            mModloaders = itemView.findViewById(R.id.zh_mod_modloader_textview);
            mReleaseType = itemView.findViewById(R.id.zh_mod_release_type_textview);
        }

        public void setData(ModVersionItem modVersionItem) {
            mImageView.setImageResource(getDownloadType(modVersionItem.versionType));

            mTitle.setText(modVersionItem.title);

            String downloadCountText = StringUtils.insertSpace(context.getString(R.string.zh_profile_mods_information_download_count),
                    NumberWithUnits.formatNumberWithUnit(modVersionItem.download, ZHTools.isEnglish(context)));
            mDownloadCount.setText(downloadCountText);

            String modloaderText;
            if (!modVersionItem.modloaders.isEmpty()) {
                modloaderText = modVersionItem.modloaders;
            } else {
                modloaderText = context.getString(R.string.zh_unknown);
            }
            mModloaders.setText(StringUtils.insertSpace(context.getString(R.string.zh_profile_mods_information_modloader), modloaderText));

            mReleaseType.setText(getDownloadTypeText(modVersionItem.versionType));

            mainView.setOnClickListener(v -> {
                if (mTasksRunning) {
                    ViewAnimUtils.setViewAnim(mainView, Techniques.Shake);
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!modVersionItem.modDependencies.isEmpty()) {
                    ModDependenciesDialog dependenciesDialog = new ModDependenciesDialog(
                            context,
                            mod,
                            modVersionItem.modDependencies,
                            () -> mod.api.handleInstallation(context, mod.isModpack, mod.modsPath, modDetail, modVersionItem));
                    dependenciesDialog.show();
                    return;
                }

                mod.api.handleInstallation(context, mod.isModpack, mod.modsPath, modDetail, modVersionItem);
            });
        }

        private int getDownloadType(VersionType.VersionTypeEnum versionType) {
            switch (versionType) {
                case BETA:
                    return R.drawable.ic_download_beta;
                case ALPHA:
                    return R.drawable.ic_download_alpha;
                default:
                case RELEASE:
                    return R.drawable.ic_download_release;
            }
        }

        private String getDownloadTypeText(VersionType.VersionTypeEnum versionType) {
            String text;
            switch (versionType) {
                case RELEASE:
                    text = mainView.getContext().getString(R.string.zh_profile_mods_information_release_type_release);
                    break;
                case BETA:
                    text = mainView.getContext().getString(R.string.zh_profile_mods_information_release_type_beta);
                    break;
                case ALPHA:
                    text = mainView.getContext().getString(R.string.zh_profile_mods_information_release_type_alpha);
                    break;
                default:
                    text = mainView.getContext().getString(R.string.zh_unknown);
                    break;
            }
            return StringUtils.insertSpace(mainView.getContext().getString(R.string.zh_profile_mods_information_release_type), text);
        }
    }
}
