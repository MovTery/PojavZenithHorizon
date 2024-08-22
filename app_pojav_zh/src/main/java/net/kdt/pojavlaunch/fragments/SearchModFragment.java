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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.feature.mod.ModLoaderList;
import com.movtery.pojavzh.feature.mod.SearchModPlatform;
import com.movtery.pojavzh.feature.mod.SearchModSort;
import com.movtery.pojavzh.ui.dialog.SelectVersionDialog;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener;
import com.movtery.pojavzh.utils.anim.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;
import net.kdt.pojavlaunch.R;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.modloaders.modpacks.ModItemAdapter;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;

import java.util.ArrayList;
import java.util.List;

public class SearchModFragment extends FragmentWithAnim implements ModItemAdapter.SearchResultCallback {
    public static final String TAG = "SearchModFragment";
    public static final String BUNDLE_SEARCH_MODPACK = "BundleSearchModPack";
    public static final String BUNDLE_MOD_PATH = "BundleModPath";
    private SearchFilters mSearchFilters;
    private boolean isModpack;
    private String mModsPath;
    private View mModsLayout, mOperateLayout, mLoadingView;
    private EditText mSearchEditText;
    private RecyclerView mRecyclerview;
    private ModItemAdapter mModItemAdapter;
    private TextView mStatusTextView;
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        parseBundle();
        mModsLayout = view.findViewById(R.id.mods_layout);
        mOperateLayout = view.findViewById(R.id.operate_layout);
        mSearchFilters = new SearchFilters();
        mSearchFilters.isModpack = this.isModpack;

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

    private void searchMods(String name) {
        mRecyclerview.scrollToPosition(0);
        mRecyclerview.setVisibility(View.GONE);
        AnimUtils.setVisibilityAnimYoYo(mLoadingView, true);
        mSearchFilters.name = name == null ? "" : name;
        mModItemAdapter.performSearchQuery(mSearchFilters);
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        isModpack = bundle.getBoolean(BUNDLE_SEARCH_MODPACK, false);
        mModsPath = bundle.getString(BUNDLE_MOD_PATH, null);
    }

    private void initFilterView(Context context, View view) {
        mSearchEditText = view.findViewById(R.id.search_mod_edittext);
        TextView mTitleTextView = view.findViewById(R.id.search_mod_title);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_mod_search);
        TextView mSelectedVersion = view.findViewById(R.id.search_mod_selected_mc_version_textview);
        Button mSelectVersionButton = view.findViewById(R.id.search_mod_mc_version_button);
        Spinner mSortBy = view.findViewById(R.id.zh_search_mod_sort);
        Spinner mPlatform = view.findViewById(R.id.zh_search_mod_platform);
        CheckBox mModloaderForge = view.findViewById(R.id.zh_search_forge_checkBox);
        CheckBox mModloaderFabric = view.findViewById(R.id.zh_search_fabric_checkBox);
        CheckBox mModloaderQuilt = view.findViewById(R.id.zh_search_quilt_checkBox);
        CheckBox mModloaderNeoForge = view.findViewById(R.id.zh_search_neoforge_checkBox);
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
                    mSearchFilters.mcVersion = version;
                    selectVersionDialog.dismiss();
                }
            });

            selectVersionDialog.show();
        });

        mSelectedVersion.setText(mSearchFilters.mcVersion);

        List<String> platfromList = SearchModPlatform.getIndexList(context);
        if (mPlatform != null) {
            mPlatform.setAdapter(new ArrayAdapter<>(context, R.layout.item_simple_list_1, platfromList));
            mPlatform.setSelection(SearchModPlatform.getIndex(mSearchFilters.platform));

            mPlatform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSearchFilters.platform = SearchModPlatform.getPlatform(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        List<String> sortList = new ArrayList<>(SearchModSort.getIndexList(context));
        if (mSortBy != null) {
            mSortBy.setAdapter(new ArrayAdapter<>(context, R.layout.item_simple_list_1, sortList));
            //默认选中筛选器设置的排序索引
            mSortBy.setSelection(mSearchFilters.sort);

            mSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSearchFilters.sort = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        mModloaderForge.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.modloaderList.get(0)));
        mModloaderFabric.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.modloaderList.get(1)));
        mModloaderQuilt.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.modloaderList.get(2)));
        mModloaderNeoForge.setChecked(mSearchFilters.modloaders.contains(ModLoaderList.modloaderList.get(3)));

        mModloaderForge.setOnClickListener(v -> {
            String forge = ModLoaderList.modloaderList.get(0);
            if (mModloaderForge.isChecked() && !mSearchFilters.modloaders.contains(forge)) {
                mSearchFilters.modloaders.add(forge);
            } else mSearchFilters.modloaders.remove(forge);
        });
        mModloaderFabric.setOnClickListener(v -> {
            String fabric = ModLoaderList.modloaderList.get(1);
            if (mModloaderFabric.isChecked() && !mSearchFilters.modloaders.contains(fabric)) {
                mSearchFilters.modloaders.add(fabric);
            } else mSearchFilters.modloaders.remove(fabric);
        });
        mModloaderQuilt.setOnClickListener(v -> {
            String quilt = ModLoaderList.modloaderList.get(2);
            if (mModloaderQuilt.isChecked() && !mSearchFilters.modloaders.contains(quilt)) {
                mSearchFilters.modloaders.add(quilt);
            } else mSearchFilters.modloaders.remove(quilt);
        });
        mModloaderNeoForge.setOnClickListener(v -> {
            String neoforge = ModLoaderList.modloaderList.get(3);
            if (mModloaderNeoForge.isChecked() && !mSearchFilters.modloaders.contains(neoforge)) {
                mSearchFilters.modloaders.add(neoforge);
            } else mSearchFilters.modloaders.remove(neoforge);
        });

        mResetButton.setOnClickListener(v -> {
            mSearchFilters.name = "";
            mSearchFilters.mcVersion = "";
            mSearchFilters.modloaders = new ArrayList<>();
            mSearchFilters.sort = 0;
            mSearchFilters.platform = SearchFilters.ApiPlatform.BOTH;

            //重置控件
            mSelectedVersion.setText("");
            if (mSortBy != null) mSortBy.setSelection(0);
            if (mPlatform != null) mPlatform.setSelection(0);
            mModloaderForge.setChecked(false);
            mModloaderFabric.setChecked(false);
            mModloaderQuilt.setChecked(false);
            mModloaderNeoForge.setChecked(false);
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
