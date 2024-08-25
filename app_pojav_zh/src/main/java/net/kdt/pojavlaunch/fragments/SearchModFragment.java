package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.feature.mod.ModCategory;
import com.movtery.pojavzh.feature.mod.ModFilters;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.feature.mod.SearchModPlatform;
import com.movtery.pojavzh.feature.mod.SearchModSort;
import com.movtery.pojavzh.feature.mod.translate.ModPackTranslateManager;
import com.movtery.pojavzh.feature.mod.translate.ModTranslateManager;
import com.movtery.pojavzh.ui.dialog.SelectVersionDialog;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener;
import com.movtery.pojavzh.utils.anim.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;
import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;
import com.skydoves.powerspinner.DefaultSpinnerAdapter;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import net.kdt.pojavlaunch.modloaders.modpacks.ModItemAdapter;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;

import java.util.ArrayList;
import java.util.List;

public class SearchModFragment extends FragmentWithAnim implements ModItemAdapter.SearchResultCallback {
    public static final String TAG = "SearchModFragment";
    public static final String BUNDLE_SEARCH_MODPACK = "BundleSearchModPack";
    public static final String BUNDLE_MOD_PATH = "BundleModPath";
    private ModFilters mModFilters;
    private boolean isModpack;
    private String mModsPath;
    private View mModsLayout, mOperateLayout, mLoadingView;
    private EditText mSearchEditText;
    private RecyclerView mRecyclerview;
    private ModItemAdapter mModItemAdapter;
    private TextView mStatusTextView;
    private ColorStateList mDefaultTextColor;
    private PowerSpinnerView mSortBy, mPlatform, mCategory, mModloader;
    private ModpackApi modpackApi;

    public SearchModFragment() {
        super(R.layout.fragment_mod_search);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        modpackApi = new CommonApi(context.getString(R.string.curseforge_api_key));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        parseBundle();
        mModsLayout = view.findViewById(R.id.mods_layout);
        mOperateLayout = view.findViewById(R.id.operate_layout);
        mModFilters = new ModFilters();
        mModFilters.setModpack(this.isModpack);

        mRecyclerview = view.findViewById(R.id.search_mod_list);
        mLoadingView = view.findViewById(R.id.zh_mods_loading);
        mStatusTextView = view.findViewById(R.id.search_mod_status_text);
        Button mBackButton = view.findViewById(R.id.search_mod_back);

        initFilterView(requireContext(), view);

        mDefaultTextColor = mStatusTextView.getTextColors();

        // You can only access resources after attaching to current context
        mModItemAdapter = new ModItemAdapter(new ModDependencies.SelectedMod(SearchModFragment.this,
                null, modpackApi, isModpack, mModsPath),
                mRecyclerview, getResources(), this);
        mModItemAdapter.setOnAddFragmentListener(this::closeSpinner);
        mRecyclerview.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerview.setAdapter(mModItemAdapter);

        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            searchMods(mSearchEditText.getText().toString());
            mSearchEditText.clearFocus();
            return false;
        });

        mBackButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        searchMods(null); //自动搜索一次

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public void onSearchFinished() {
        AnimUtils.setVisibilityAnimYoYo(mLoadingView, false);
        AnimUtils.setVisibilityAnim(mStatusTextView, false);
        mRecyclerview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchError(int error) {
        mRecyclerview.setVisibility(View.GONE);
        AnimUtils.setVisibilityAnimYoYo(mLoadingView, false);
        AnimUtils.setVisibilityAnim(mStatusTextView, true);
        switch (error) {
            case ERROR_INTERNAL:
                mStatusTextView.setTextColor(Color.RED);
                mStatusTextView.setText(isModpack ? R.string.search_modpack_error : R.string.zh_profile_mods_search_mod_failed);
                break;
            case ERROR_NO_RESULTS:
                mStatusTextView.setTextColor(mDefaultTextColor);
                mStatusTextView.setText(isModpack ? R.string.search_modpack_no_result : R.string.zh_profile_mods_search_mod_no_result);
                break;
        }
    }

    @Override
    public void onStop() {
        closeSpinner();
        super.onStop();
    }

    private void closeSpinner() {
        mSortBy.dismiss();
        mPlatform.dismiss();
        mCategory.dismiss();
        mModloader.dismiss();
    }

    private void searchMods(String name) {
        mRecyclerview.scrollToPosition(0);
        mRecyclerview.setVisibility(View.GONE);
        AnimUtils.setVisibilityAnimYoYo(mLoadingView, true);
        AnimUtils.setVisibilityAnimYoYo(mStatusTextView, false);

        if (ZHTools.areaChecks("zh") && StringUtils.containsChinese(name)) {
            name = this.isModpack ?
                    ModPackTranslateManager.INSTANCE.searchToOrigin(name) :
                    ModTranslateManager.INSTANCE.searchToOrigin(name);
        }

        mModFilters.setName(name == null ? "" : name);
        mModItemAdapter.performSearchQuery(mModFilters);
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        isModpack = bundle.getBoolean(BUNDLE_SEARCH_MODPACK, false);
        mModsPath = bundle.getString(BUNDLE_MOD_PATH, null);
    }

    private void initFilterView(Context context, View view) {
        mSearchEditText = view.findViewById(R.id.search_mod_edittext);
        mSortBy = view.findViewById(R.id.zh_search_mod_sort);
        mPlatform = view.findViewById(R.id.zh_search_mod_platform);
        mCategory = view.findViewById(R.id.zh_search_mod_category);
        mModloader = view.findViewById(R.id.zh_search_mod_modloader);
        TextView mTitleTextView = view.findViewById(R.id.search_mod_title);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_mod_search);
        TextView mSelectedVersion = view.findViewById(R.id.search_mod_selected_mc_version_textview);
        Button mSelectVersionButton = view.findViewById(R.id.search_mod_mc_version_button);
        Button mResetButton = view.findViewById(R.id.search_mod_reset);

        if (this.isModpack) {
            mTitleTextView.setText(R.string.hint_search_modpack);
        }

        mSearchButton.setOnClickListener(v -> searchMods(mSearchEditText.getText().toString()));

        // 打开版本选择弹窗
        mSelectVersionButton.setOnClickListener(v -> {
            SelectVersionDialog selectVersionDialog = new SelectVersionDialog(context);
            selectVersionDialog.setOnVersionSelectedListener(new VersionSelectedListener() {
                @Override
                public void onVersionSelected(String version) {
                    mSelectedVersion.setText(version);
                    mModFilters.setMcVersion(version);
                    selectVersionDialog.dismiss();
                }
            });

            selectVersionDialog.show();
        });

        mSelectedVersion.setText(mModFilters.getMcVersion());

        List<String> categoriesList = this.isModpack ? ModCategory.getModPackCategories(context) : ModCategory.getModCategories(context);
        if (mCategory != null) {
            DefaultSpinnerAdapter adapter = new DefaultSpinnerAdapter(mCategory);
            adapter.setItems(categoriesList);

            mCategory.setSpinnerAdapter(adapter);
            mCategory.selectItemByIndex(0);

            mCategory.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) ->
                    mModFilters.setCategory(isModpack ?
                    ModCategory.getModPackCategoryFromIndex(i1) :
                    ModCategory.getModCategoryFromIndex(i1)));
        }

        List<String> platfromList = SearchModPlatform.getIndexList(context);
        if (mPlatform != null) {
            DefaultSpinnerAdapter adapter = new DefaultSpinnerAdapter(mPlatform);
            adapter.setItems(platfromList);

            mPlatform.setSpinnerAdapter(adapter);
            mPlatform.selectItemByIndex(0);

            mPlatform.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) ->
                    mModFilters.setPlatform(SearchModPlatform.getPlatform(i1)));
        }

        List<String> sortList = new ArrayList<>(SearchModSort.getIndexList(context));
        if (mSortBy != null) {
            DefaultSpinnerAdapter adapter = new DefaultSpinnerAdapter(mSortBy);
            adapter.setItems(sortList);

            mSortBy.setSpinnerAdapter(adapter);
            mSortBy.selectItemByIndex(0);

            mSortBy.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> mModFilters.setSort(i1));
        }

        List<String> modloaderList = new ArrayList<>(ModLoaderList.modloaderList);
        modloaderList.add(0, getString(R.string.zh_all));
        if (mModloader != null) {
            DefaultSpinnerAdapter adapter = new DefaultSpinnerAdapter(mModloader);
            adapter.setItems(modloaderList);

            mModloader.setSpinnerAdapter(adapter);
            mModloader.selectItemByIndex(0);

            mModloader.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> {
                if (i1 == 0) mModFilters.setModloader(null);
                else mModFilters.setModloader(ModLoaderList.getModLoaderNameFromIndex(i1 - 1));
            });
        }

        mResetButton.setOnClickListener(v -> {
            mModFilters.setName("");
            mModFilters.setMcVersion("");
            mModFilters.setModloader(null);
            mModFilters.setSort(0);
            mModFilters.setPlatform(ModFilters.ApiPlatform.BOTH);
            mModFilters.setCategory(ModCategory.Category.ALL);

            //重置控件
            mSelectedVersion.setText("");
            if (mSortBy != null) mSortBy.selectItemByIndex(0);
            if (mPlatform != null) mPlatform.selectItemByIndex(0);
            if (mCategory != null) mCategory.selectItemByIndex(0);
            if (mModloader != null) mModloader.selectItemByIndex(0);
        });
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mModsLayout, Techniques.BounceInDown));
        yoYos.add(ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.BounceInLeft));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mModsLayout, Techniques.FadeOutUp));
        yoYos.add(ViewAnimUtils.setViewAnim(mOperateLayout, Techniques.FadeOutRight));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }
}
