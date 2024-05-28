package com.movtery.ui.subassembly.downloadmod;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.feature.ResourceManager;
import com.movtery.ui.fragment.DownloadModFragment;
import com.movtery.utils.NumberWithUnits;
import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ImageReceiver;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

import java.util.List;
import java.util.concurrent.Future;

public class ModDependenciesAdapter extends RecyclerView.Adapter<ModDependenciesAdapter.InnerHolder>{
    private final Fragment fragment;
    private final ModpackApi mModApi;
    private final List<ModDependencies> mData;
    private final boolean isModpack;
    private final String modsPath;
    private SetOnClickListener onClickListener;

    public ModDependenciesAdapter(Fragment fragment, ModpackApi api, List<ModDependencies> mData, boolean isModpack, String modsPath) {
        this.fragment = fragment;
        this.mModApi = api;
        this.mData = mData;
        this.isModpack = isModpack;
        this.modsPath = modsPath;
    }

    @NonNull
    @Override
    public ModDependenciesAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mod_dependencies, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModDependenciesAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public void setOnItemCLickListener(SetOnClickListener listener) {
        this.onClickListener = listener;
    }

    public interface SetOnClickListener {
        void onItemClick();
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private final View mainView;
        private final ImageView mSourceImage, mModIcon;
        private final TextView mTitle, mDesc, mDependencies, mDownloadCount, mModloaders;
        private Future<?> mExtensionFuture;
        private final ModIconCache mIconCache = new ModIconCache();
        private ImageReceiver mImageReceiver;
        private Bitmap mThumbnailBitmap;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            mTitle = itemView.findViewById(R.id.mod_title_textview);
            mSourceImage = itemView.findViewById(R.id.mod_source_imageview);
            mModIcon = itemView.findViewById(R.id.mod_thumbnail_imageview);
            mDesc = itemView.findViewById(R.id.mod_body_textview);
            mDependencies = itemView.findViewById(R.id.zh_mod_dependencies_textview);
            mDownloadCount = itemView.findViewById(R.id.zh_mod_download_count_textview);
            mModloaders = itemView.findViewById(R.id.zh_mod_modloader_textview);
        }

        public void setData(ModDependencies modVersionItem) {
            ModItem item = modVersionItem.item;
            if (mThumbnailBitmap != null) {
                mModIcon.setImageBitmap(null);
                mThumbnailBitmap.recycle();
            }
            if (mImageReceiver != null) {
                mIconCache.cancelImage(mImageReceiver);
            }
            if (mExtensionFuture != null) {
                mExtensionFuture.cancel(true);
                mExtensionFuture = null;
            }

            mImageReceiver = bm -> {
                mImageReceiver = null;
                mThumbnailBitmap = bm;
                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mainView.getResources(), bm);
                mModIcon.setImageDrawable(drawable);
            };
            mIconCache.getImage(mImageReceiver, item.getIconCacheTag(), item.imageUrl);
            mSourceImage.setImageResource(getSourceDrawable(item.apiSource));
            mTitle.setText(item.title);
            String dependencies = fragment.getString(R.string.zh_profile_mods_information_dependencies) + " " +
                    ModDependencies.getTextFromType(fragment.getContext(), modVersionItem.dependencyType);
            mDependencies.setText(dependencies);
            mDesc.setText(item.description);

            String downloaderCount = ResourceManager.getString(R.string.zh_profile_mods_information_download_count) + " " + NumberWithUnits.formatNumberWithUnit(item.downloadCount,
                    //判断当前系统语言是否为英文
                    PojavZHTools.isEnglish());
            mDownloadCount.setText(downloaderCount);
            String modloaderText = ResourceManager.getString(R.string.zh_profile_mods_information_modloader) + " ";
            if (item.modloader != null && !item.modloader.isEmpty()) {
                modloaderText += item.modloader;
            } else {
                modloaderText += ResourceManager.getString(R.string.zh_unknown);
            }
            mModloaders.setText(modloaderText);

            mainView.setOnClickListener(v -> {
                ModApiViewModel viewModel = new ViewModelProvider(fragment.requireActivity()).get(ModApiViewModel.class);
                viewModel.setModApi(mModApi);
                viewModel.setModItem(item);
                viewModel.setModpack(isModpack);
                viewModel.setModsPath(modsPath);
                PojavZHTools.addFragment(fragment, DownloadModFragment.class, DownloadModFragment.TAG, null);

                if (onClickListener != null) onClickListener.onItemClick();
            });
        }

        private int getSourceDrawable(int apiSource) {
            switch (apiSource) {
                case Constants.SOURCE_CURSEFORGE:
                    return R.drawable.ic_curseforge;
                case Constants.SOURCE_MODRINTH:
                    return R.drawable.ic_modrinth;
                default:
                    throw new RuntimeException("Unknown API source");
            }
        }
    }
}
