package net.kdt.pojavlaunch.modloaders.modpacks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.movtery.pojavzh.feature.mod.ModCategory;
import com.movtery.pojavzh.feature.mod.ModFilters;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.ui.fragment.DownloadModFragment;
import com.movtery.pojavzh.ui.subassembly.viewmodel.ModApiViewModel;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.viewmodel.RecyclerViewModel;
import com.movtery.pojavzh.utils.NumberWithUnits;

import net.kdt.pojavlaunch.PojavApplication;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.image.ImageUtils;
import com.movtery.pojavzh.utils.image.UrlImageCallback;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchResult;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

public class ModItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ModDependencies.SelectedMod mod;
    private static final ModItem[] MOD_ITEMS_EMPTY = new ModItem[0];
    private static final int VIEW_TYPE_MOD_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private final Set<ViewHolder> mViewHolderSet = Collections.newSetFromMap(new WeakHashMap<>());
    private final SearchResultCallback mSearchResultCallback;
    private final RecyclerView modsRecyclerView;
    private ModItem[] mModItems;

    private Future<?> mTaskInProgress;
    private ModFilters mModFilters;
    private SearchResult mCurrentResult;
    private boolean mLastPage;
    private OnAddFragmentListener onAddFragmentListener;


    public ModItemAdapter(ModDependencies.SelectedMod mod, RecyclerView modsRecyclerView, SearchResultCallback callback) {
        mModItems = new ModItem[]{};
        mSearchResultCallback = callback;

        this.mod = mod;
        this.modsRecyclerView = modsRecyclerView;
    }

    public void performSearchQuery(ModFilters modFilters) {
        if(mTaskInProgress != null) {
            mTaskInProgress.cancel(true);
            mTaskInProgress = null;
        }
        this.mModFilters = modFilters;
        this.mLastPage = false;
        mTaskInProgress = new SelfReferencingFuture(new SearchApiTask(mModFilters, null))
                .startOnExecutor(PojavApplication.sExecutorService);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view;
        switch (viewType) {
            case VIEW_TYPE_MOD_ITEM:
                // Create a new view, which defines the UI of the list item
                view = layoutInflater.inflate(R.layout.item_mod_view, viewGroup, false);
                return new ViewHolder(view);
            case VIEW_TYPE_LOADING:
                // Create a new view, which is actually just the progress bar
                view = layoutInflater.inflate(R.layout.view_loading, viewGroup, false);
                return new LoadingViewHolder(view);
            default:
                throw new RuntimeException("Unimplemented view type!");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_MOD_ITEM:
                ((ModItemAdapter.ViewHolder)holder).setStateLimited(mModItems[position]);
                break;
            case VIEW_TYPE_LOADING:
                loadMoreResults();
                break;
            default:
                throw new RuntimeException("Unimplemented view type!");
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof ModItemAdapter.ViewHolder) ((ViewHolder) holder).setItemShow(true);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof ModItemAdapter.ViewHolder) ((ViewHolder) holder).setItemShow(false);
    }

    @Override
    public int getItemCount() {
        if(mLastPage || mModItems.length == 0) return mModItems.length;
        return mModItems.length+1;
    }

    private void loadMoreResults() {
        if(mTaskInProgress != null) return;
        mTaskInProgress = new SelfReferencingFuture(new SearchApiTask(mModFilters, mCurrentResult))
                .startOnExecutor(PojavApplication.sExecutorService);
    }

    @Override
    public int getItemViewType(int position) {
        if(position < mModItems.length) return VIEW_TYPE_MOD_ITEM;
        return VIEW_TYPE_LOADING;
    }

    public void setOnAddFragmentListener(OnAddFragmentListener listener) {
        this.onAddFragmentListener = listener;
    }

    /**
     * Basic viewholder with expension capabilities
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final View view;
        private final FlexboxLayout mCategoriesLayout;
        private final TextView mTitle, mSubTitle, mDescription, mDownloadCount, mModloader;
        private final ImageView mIconView, mSourceView;
        private Future<?> mExtensionFuture;
        private ModItem item;

        public ViewHolder(View view) {
            super(view);
            this.context = view.getContext();
            this.view = view;
            mViewHolderSet.add(this);

            // Define click listener for the ViewHolder's View
            mTitle = view.findViewById(R.id.mod_title_textview);
            mSubTitle = view.findViewById(R.id.mod_subtitle_textview);
            mCategoriesLayout = view.findViewById(R.id.mod_categories_Layout);
            mDescription = view.findViewById(R.id.mod_body_textview);
            mDownloadCount = view.findViewById(R.id.zh_mod_download_count_textview);
            mModloader = view.findViewById(R.id.zh_mod_modloader_textview);
            mIconView = view.findViewById(R.id.mod_thumbnail_imageview);
            mSourceView = view.findViewById(R.id.mod_source_imageview);
        }

        /** Display basic info about the moditem */
        public void setStateLimited(ModItem item) {
            this.item = item;
            this.view.setOnClickListener(v -> {
                //防止用户同时点击多个Item
                if (!modsRecyclerView.isEnabled()) return;
                modsRecyclerView.setEnabled(false);
                //设置额外的数据传递
                FragmentActivity fragmentActivity = mod.fragment.requireActivity();
                ModApiViewModel viewModel = new ViewModelProvider(fragmentActivity).get(ModApiViewModel.class);
                RecyclerViewModel recyclerViewModel = new ViewModelProvider(fragmentActivity).get(RecyclerViewModel.class);
                viewModel.setModApi(mod.api);
                viewModel.setModItem(item);
                viewModel.setModpack(mod.isModpack);
                viewModel.setModsPath(mod.modsPath);
                recyclerViewModel.view = modsRecyclerView;

                if (onAddFragmentListener != null) onAddFragmentListener.onAdd();
                ZHTools.addFragment(mod.fragment, DownloadModFragment.class, DownloadModFragment.TAG, null);
            });

            if(mExtensionFuture != null) {
                /*
                 * Since this method reinitializes the ViewHolder for a new mod, this Future stops being ours, so we cancel it
                 * and null it. The rest is handled above
                 */
                mExtensionFuture.cancel(true);
                mExtensionFuture = null;
            }

            mIconView.setImageDrawable(null);

            mSourceView.setImageResource(getSourceDrawable(item.apiSource));
            mDescription.setText(item.description);

            if (item.subTitle != null) {
                mSubTitle.setVisibility(View.VISIBLE);
                mTitle.setText(item.subTitle);
                mSubTitle.setText(item.title);
            } else {
                mSubTitle.setVisibility(View.GONE);
                mTitle.setText(item.title);
            }

            mCategoriesLayout.removeAllViews();
            for (ModCategory.Category category : item.categories) {
                addCategoryView(context, mCategoriesLayout, context.getString(category.getResNameID()));
            }

            String downloaderCount = StringUtils.insertSpace(context.getString(R.string.profile_mods_information_download_count), NumberWithUnits.formatNumberWithUnit(item.downloadCount,
                    //判断当前系统语言是否为英文
                    ZHTools.isEnglish(context)));
            mDownloadCount.setText(downloaderCount);

            StringJoiner sj = new StringJoiner(", ");
            for (ModLoaderList.ModLoader modloader : item.modloaders) {
                sj.add(modloader.getLoaderName());
            }
            String modloaderText;
            if (sj.length() > 0) modloaderText = sj.toString();
            else modloaderText = context.getString(R.string.generic_unknown);

            mModloader.setText(StringUtils.insertSpace(context.getString(R.string.profile_mods_information_modloader), modloaderText));
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
                        if (Objects.equals(item.imageUrl, url)) mIconView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onImageLoaded(@Nullable Drawable drawable, @NonNull String url) {
                        if (Objects.equals(item.imageUrl, url)) mIconView.setImageDrawable(drawable);
                    }
                });
            }
        }
    }

    /**
     * The view holder used to hold the progress bar at the end of the list
     */
    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View view) {
            super(view);
        }
    }

    private class SearchApiTask implements SelfReferencingFuture.FutureInterface {
        private final ModFilters mModFilters;
        private final SearchResult mPreviousResult;

        private SearchApiTask(ModFilters modFilters, SearchResult previousResult) {
            this.mModFilters = modFilters;
            this.mPreviousResult = previousResult;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void run(Future<?> myFuture) {
            SearchResult result = mod.api.searchMod(mModFilters, mPreviousResult);
            ModItem[] resultModItems = result != null ? result.results : null;
            if(resultModItems != null && resultModItems.length != 0 && mPreviousResult != null) {
                ModItem[] newModItems = new ModItem[resultModItems.length + mModItems.length];
                System.arraycopy(mModItems, 0, newModItems, 0, mModItems.length);
                System.arraycopy(resultModItems, 0, newModItems, mModItems.length, resultModItems.length);
                resultModItems = newModItems;
            }
            ModItem[] finalModItems = resultModItems;
            Tools.runOnUiThread(() -> {
                if(myFuture.isCancelled()) return;
                mTaskInProgress = null;
                if(finalModItems == null) {
                    mSearchResultCallback.onSearchError(SearchResultCallback.ERROR_INTERNAL);
                }else if(finalModItems.length == 0) {
                    if(mPreviousResult != null) {
                        mLastPage = true;
                        notifyItemChanged(mModItems.length);
                        mSearchResultCallback.onSearchFinished();
                        return;
                    }
                    mSearchResultCallback.onSearchError(SearchResultCallback.ERROR_NO_RESULTS);
                }else{
                    mSearchResultCallback.onSearchFinished();
                }
                mCurrentResult = result;
                if(finalModItems == null) {
                    mModItems = MOD_ITEMS_EMPTY;
                    notifyDataSetChanged();
                    return;
                }
                if(mPreviousResult != null) {
                    int prevLength = mModItems.length;
                    mModItems = finalModItems;
                    notifyItemChanged(prevLength);
                    notifyItemRangeInserted(prevLength+1, mModItems.length);
                }else {
                    mModItems = finalModItems;
                    notifyDataSetChanged();
                }
                modsRecyclerView.scheduleLayoutAnimation();
            });
        }
    }

    public interface SearchResultCallback {
        int ERROR_INTERNAL = 0;
        int ERROR_NO_RESULTS = 1;
        void onSearchFinished();
        void onSearchError(int error);
    }

    public interface OnAddFragmentListener {
        void onAdd();
    }
}
