package com.movtery.pojavzh.ui.subassembly.downloadmod;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.movtery.pojavzh.feature.mod.ModCategory;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.ui.fragment.DownloadModFragment;
import com.movtery.pojavzh.ui.subassembly.viewmodel.ModApiViewModel;
import com.movtery.pojavzh.utils.NumberWithUnits;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.image.ImageUtils;
import com.movtery.pojavzh.utils.image.UrlImageCallback;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.ItemModDependenciesBinding;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.Future;

public class ModDependenciesAdapter extends RecyclerView.Adapter<ModDependenciesAdapter.InnerHolder> {
    private final ModDependencies.SelectedMod mod;
    private final List<ModDependencies> mData;
    private SetOnClickListener onClickListener;

    public ModDependenciesAdapter(ModDependencies.SelectedMod mod, List<ModDependencies> mData) {
        this.mod = mod;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ModDependenciesAdapter.InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InnerHolder(ItemModDependenciesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModDependenciesAdapter.InnerHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull InnerHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.setItemShow(true);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull InnerHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setItemShow(false);
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
        private final Context context;
        private final ItemModDependenciesBinding binding;
        private Future<?> mExtensionFuture;
        private ModItem item;

        public InnerHolder(@NonNull ItemModDependenciesBinding binding) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            this.binding = binding;
        }

        public void setData(ModDependencies modVersionItem) {
            item = modVersionItem.item;
            if (mExtensionFuture != null) {
                mExtensionFuture.cancel(true);
                mExtensionFuture = null;
            }

            binding.thumbnailImageview.setImageDrawable(null);

            binding.sourceImageview.setImageResource(getSourceDrawable(item.apiSource));

            if (item.subTitle != null) {
                binding.subtitleTextview.setVisibility(View.VISIBLE);
                binding.titleTextview.setText(item.subTitle);
                binding.subtitleTextview.setText(item.title);
            } else {
                binding.subtitleTextview.setVisibility(View.GONE);
                binding.titleTextview.setText(item.title);
            }

            binding.categoriesLayout.removeAllViews();
            for (ModCategory.Category category : item.categories) {
                addCategoryView(context, binding.categoriesLayout, context.getString(category.getResNameID()));
            }

            FragmentActivity fragmentActivity = mod.fragment.requireActivity();
            String dependencies = StringUtils.insertSpace(fragmentActivity.getString(R.string.profile_mods_information_dependencies),
                    ModDependencies.getTextFromType(fragmentActivity, modVersionItem.dependencyType));
            binding.dependenciesTextview.setText(dependencies);
            binding.bodyTextview.setText(item.description);

            String downloaderCount = StringUtils.insertSpace(fragmentActivity.getString(R.string.profile_mods_information_download_count), NumberWithUnits.formatNumberWithUnit(item.downloadCount,
                    //判断当前系统语言是否为英文
                    ZHTools.isEnglish(fragmentActivity)));
            binding.downloadCountTextview.setText(downloaderCount);

            StringJoiner sj = new StringJoiner(", ");
            for (ModLoaderList.ModLoader modloader : item.modloaders) {
                sj.add(modloader.getLoaderName());
            }
            String modloaderText;
            if (sj.length() > 0) modloaderText = sj.toString();
            else modloaderText = fragmentActivity.getString(R.string.generic_unknown);

            binding.modloaderTextview.setText(StringUtils.insertSpace(fragmentActivity.getString(R.string.profile_mods_information_modloader), modloaderText));

            itemView.setOnClickListener(v -> {
                ModApiViewModel viewModel = new ViewModelProvider(fragmentActivity).get(ModApiViewModel.class);
                viewModel.setModApi(mod.api);
                viewModel.setModItem(item);
                viewModel.setModpack(mod.isModpack);
                viewModel.setModsPath(mod.modsPath);
                ZHTools.addFragment(mod.fragment, DownloadModFragment.class, DownloadModFragment.TAG, null);

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

        private void addCategoryView(Context context, FlexboxLayout layout, String text) {
            LayoutInflater inflater = LayoutInflater.from(context);
            TextView textView = (TextView) inflater.inflate(R.layout.item_mod_category_textview, layout, false);
            textView.setText(text);

            layout.addView(textView);
        }

        public void setItemShow(boolean b) {
            if (b && item.imageUrl != null) {
                ImageUtils.loadDrawableFromUrl(context, item.imageUrl, new UrlImageCallback() {
                    @Override
                    public void onImageCleared(@Nullable Drawable placeholder, @NonNull String url) {
                        if (Objects.equals(item.imageUrl, url)) binding.thumbnailImageview.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onImageLoaded(@Nullable Drawable drawable, @NonNull String url) {
                        if (Objects.equals(item.imageUrl, url)) binding.thumbnailImageview.setImageDrawable(drawable);
                    }
                });
            }
        }
    }
}
