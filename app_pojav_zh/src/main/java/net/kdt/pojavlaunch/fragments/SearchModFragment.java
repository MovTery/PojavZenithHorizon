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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
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
import com.movtery.pojavzh.utils.stringutils.StringUtils;
import com.skydoves.powerspinner.DefaultSpinnerAdapter;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;

import net.kdt.pojavlaunch.databinding.FragmentModSearchBinding;
import net.kdt.pojavlaunch.modloaders.modpacks.ModItemAdapter;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;

import java.util.ArrayList;
import java.util.List;

public class SearchModFragment extends FragmentWithAnim implements ModItemAdapter.SearchResultCallback {
    public static final String TAG = "SearchModFragment";
    public static final String BUNDLE_SEARCH_MODPACK = "BundleSearchModPack";
    public static final String BUNDLE_MOD_PATH = "BundleModPath";
    private FragmentModSearchBinding binding;
    private ModFilters mModFilters;
    private boolean isModpack;
    private String mModsPath;
    private ModItemAdapter mModItemAdapter;
    private ColorStateList mDefaultTextColor;
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
        binding = FragmentModSearchBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        parseBundle();
        mModFilters = new ModFilters();
        mModFilters.setModpack(this.isModpack);

        initFilterView(requireContext());

        mDefaultTextColor = binding.statusText.getTextColors();

        // You can only access resources after attaching to current context
        mModItemAdapter = new ModItemAdapter(new ModDependencies.SelectedMod(SearchModFragment.this,
                null, modpackApi, isModpack, mModsPath),
                binding.searchModList, this);
        mModItemAdapter.setOnAddFragmentListener(this::closeSpinner);
        binding.searchModList.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        binding.searchModList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchModList.setAdapter(mModItemAdapter);

        binding.searchModList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && recyclerView.getAdapter() != null) {
                    int lastPosition = layoutManager.findLastVisibleItemPosition();
                    boolean b = lastPosition >= 12;

                    AnimUtils.setVisibilityAnim(binding.backToTop, b);
                }
            }
        });

        binding.nameEdit.setOnEditorActionListener((v, actionId, event) -> {
            searchMods(binding.nameEdit.getText().toString());
            binding.nameEdit.clearFocus();
            return false;
        });

        binding.backToTop.setOnClickListener(v -> binding.searchModList.smoothScrollToPosition(0));
        binding.returnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        searchMods(null); //自动搜索一次
    }

    @Override
    public void onSearchFinished() {
        AnimUtils.playVisibilityAnim(binding.loadingLayout, false);
        AnimUtils.setVisibilityAnim(binding.statusText, false);
        binding.searchModList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchError(int error) {
        binding.searchModList.setVisibility(View.GONE);
        AnimUtils.playVisibilityAnim(binding.loadingLayout, false);
        AnimUtils.setVisibilityAnim(binding.statusText, true);
        switch (error) {
            case ERROR_INTERNAL:
                binding.statusText.setTextColor(Color.RED);
                binding.statusText.setText(isModpack ? R.string.search_modpack_error : R.string.profile_mods_search_mod_failed);
                break;
            case ERROR_NO_RESULTS:
                binding.statusText.setTextColor(mDefaultTextColor);
                binding.statusText.setText(isModpack ? R.string.search_modpack_no_result : R.string.profile_mods_search_mod_no_result);
                break;
        }
    }

    @Override
    public void onStop() {
        closeSpinner();
        super.onStop();
    }

    private void closeSpinner() {
        binding.sortSpinner.dismiss();
        binding.platformSpinner.dismiss();
        binding.categorySpinner.dismiss();
        binding.modloaderSpinner.dismiss();
    }

    private void searchMods(String name) {
        binding.searchModList.scrollToPosition(0);
        binding.searchModList.setVisibility(View.GONE);
        AnimUtils.playVisibilityAnim(binding.loadingLayout, true);
        AnimUtils.playVisibilityAnim(binding.statusText, false);

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

    private void initFilterView(Context context) {
        if (this.isModpack) {
            binding.searchTitle.setText(R.string.search_modpack);
        }

        binding.searchView.setOnClickListener(v -> searchMods(binding.nameEdit.getText().toString()));

        // 打开版本选择弹窗
        binding.mcVersionButton.setOnClickListener(v -> {
            SelectVersionDialog selectVersionDialog = new SelectVersionDialog(context);
            selectVersionDialog.setOnVersionSelectedListener(new VersionSelectedListener() {
                @Override
                public void onVersionSelected(String version) {
                    binding.selectedMcVersionView.setText(version);
                    mModFilters.setMcVersion(version);
                    selectVersionDialog.dismiss();
                }
            });

            selectVersionDialog.show();
        });

        binding.selectedMcVersionView.setText(mModFilters.getMcVersion());

        List<String> categoriesList = this.isModpack ? ModCategory.getModPackCategories(context) : ModCategory.getModCategories(context);
        DefaultSpinnerAdapter adapter = new DefaultSpinnerAdapter(binding.categorySpinner);
        adapter.setItems(categoriesList);

        binding.categorySpinner.setSpinnerAdapter(adapter);
        binding.categorySpinner.selectItemByIndex(0);

        binding.categorySpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) ->
                mModFilters.setCategory(isModpack ?
                        ModCategory.getModPackCategoryFromIndex(i1) :
                        ModCategory.getModCategoryFromIndex(i1)));

        List<String> platfromList = SearchModPlatform.getIndexList(context);
        DefaultSpinnerAdapter platformAdapter = new DefaultSpinnerAdapter(binding.platformSpinner);
        platformAdapter.setItems(platfromList);
        binding.platformSpinner.setSpinnerAdapter(platformAdapter);
        binding.platformSpinner.selectItemByIndex(0);
        binding.platformSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) ->
                mModFilters.setPlatform(SearchModPlatform.getPlatform(i1)));

        List<String> sortList = new ArrayList<>(SearchModSort.getIndexList(context));
        DefaultSpinnerAdapter sortAdapter = new DefaultSpinnerAdapter(binding.sortSpinner);
        sortAdapter.setItems(sortList);
        binding.sortSpinner.setSpinnerAdapter(sortAdapter);
        binding.sortSpinner.selectItemByIndex(0);
        binding.sortSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> mModFilters.setSort(i1));

        List<String> modloaderList = new ArrayList<>(ModLoaderList.modloaderList);
        modloaderList.add(0, getString(R.string.generic_all));
        DefaultSpinnerAdapter modloaderAdapter = new DefaultSpinnerAdapter(binding.modloaderSpinner);
        modloaderAdapter.setItems(modloaderList);
        binding.modloaderSpinner.setSpinnerAdapter(modloaderAdapter);
        binding.modloaderSpinner.selectItemByIndex(0);
        binding.modloaderSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> {
            if (i1 == 0) mModFilters.setModloader(null);
            else mModFilters.setModloader(ModLoaderList.getModLoaderNameFromIndex(i1 - 1));
        });

        binding.searchModReset.setOnClickListener(v -> {
            //重置控件
            binding.nameEdit.setText("");
            binding.selectedMcVersionView.setText("");
            binding.platformSpinner.selectItemByIndex(0);
            binding.sortSpinner.selectItemByIndex(0);
            binding.categorySpinner.selectItemByIndex(0);
            binding.modloaderSpinner.selectItemByIndex(0);
        });
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.modsLayout, Animations.BounceInDown))
                .apply(new AnimPlayer.Entry(binding.operateLayout, Animations.BounceInLeft));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.modsLayout, Animations.FadeOutUp))
                .apply(new AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight));
    }
}
