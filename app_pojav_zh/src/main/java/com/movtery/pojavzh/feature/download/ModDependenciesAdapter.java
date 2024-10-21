package com.movtery.pojavzh.feature.download;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.movtery.pojavzh.feature.download.enums.Category;
import com.movtery.pojavzh.feature.download.enums.ModLoader;
import com.movtery.pojavzh.feature.download.enums.Platform;
import com.movtery.pojavzh.feature.download.item.DependenciesInfoItem;
import com.movtery.pojavzh.feature.download.item.InfoItem;
import com.movtery.pojavzh.feature.download.utils.DependencyUtils;
import com.movtery.pojavzh.ui.fragment.DownloadModFragment;
import com.movtery.pojavzh.utils.NumberWithUnits;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.ItemModDependenciesBinding;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;

import java.io.File;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Future;

public class ModDependenciesAdapter extends RecyclerView.Adapter<ModDependenciesAdapter.InnerHolder> {
    private final Fragment mParentFragment;
    private final InfoItem mInfoItem;
    private final List<DependenciesInfoItem> mData;
    private SetOnClickListener onClickListener;

    public ModDependenciesAdapter(Fragment fragment, InfoItem item, List<DependenciesInfoItem> mData) {
        this.mParentFragment = fragment;
        this.mInfoItem = item;
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

        public InnerHolder(@NonNull ItemModDependenciesBinding binding) {
            super(binding.getRoot());
            context = binding.getRoot().getContext();
            this.binding = binding;
        }

        public void setData(DependenciesInfoItem infoItem) {
            if (mExtensionFuture != null) {
                mExtensionFuture.cancel(true);
                mExtensionFuture = null;
            }

            binding.sourceImageview.setImageDrawable(getPlatformIcon(infoItem.getPlatform()));
            binding.titleTextview.setText(infoItem.getTitle());

            binding.categoriesLayout.removeAllViews();
            binding.tagsLayout.removeAllViews();
            for (Category category : infoItem.getCategory()) {
                addCategoryView(binding.categoriesLayout, context.getString(category.getResNameID()));
            }

            binding.bodyTextview.setText(infoItem.getDescription());

            binding.tagsLayout.addView(
                    getTagTextView(
                            R.string.download_info_dependencies,
                            DependencyUtils.Companion.getTextFromType(context, infoItem.getDependencyType())
                    ));

            binding.tagsLayout.addView(
                    getTagTextView(R.string.download_info_downloads, NumberWithUnits.formatNumberWithUnit(
                            infoItem.getDownloadCount(),
                            //判断当前系统语言是否为英文
                            ZHTools.isEnglish(mParentFragment.requireActivity()))));

            StringJoiner modloaderSJ = new StringJoiner(", ");
            for (ModLoader modloader : infoItem.getModloaders()) {
                modloaderSJ.add(modloader.getLoaderName());
            }
            String modloaderText;
            if (modloaderSJ.length() > 0) modloaderText = modloaderSJ.toString();
            else modloaderText = context.getString(R.string.generic_unknown);
            binding.tagsLayout.addView(
                    getTagTextView(R.string.download_info_modloader, modloaderText)
            );

            binding.thumbnailImageview.setImageDrawable(null);
            Glide.with(context).load(infoItem.getIconUrl()).into(binding.thumbnailImageview);

            itemView.setOnClickListener(v -> {
                InfoViewModel viewModel = new ViewModelProvider(mParentFragment.requireActivity()).get(InfoViewModel.class);
                viewModel.setInfoItem(infoItem);
                viewModel.setPlatformHelper(mInfoItem.getPlatform().getHelper());
                viewModel.setTargetPath(new File(ZHTools.getGameDirPath(LauncherProfiles.getCurrentProfile().gameDir), "/mods"));
                ZHTools.addFragment(mParentFragment, DownloadModFragment.class, DownloadModFragment.TAG, null);

                if (onClickListener != null) onClickListener.onItemClick();
            });
        }

        private Drawable getPlatformIcon(Platform platform) {
            if (platform == Platform.MODRINTH) return ContextCompat.getDrawable(context, R.drawable.ic_modrinth);
            if (platform == Platform.CURSEFORGE) return ContextCompat.getDrawable(context, R.drawable.ic_curseforge);
            return null;
        }

        private void addCategoryView(FlexboxLayout layout, String text) {
            LayoutInflater inflater = LayoutInflater.from(context);
            TextView textView = (TextView) inflater.inflate(R.layout.item_mod_category_textview, layout, false);
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Tools.dpToPx(9F));
            layout.addView(textView);
        }

        private TextView getTagTextView(int string, String value) {
            TextView textView = new TextView(context);
            textView.setText(StringUtils.insertSpace(context.getString(string), value));
            FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 0, (int) Tools.dpToPx(10f), 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Tools.dpToPx(9F));
            textView.setLayoutParams(layoutParams);
            return textView;
        }
    }
}
