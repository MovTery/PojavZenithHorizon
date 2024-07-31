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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModDependencies;
import com.movtery.pojavzh.utils.anim.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.movtery.pojavzh.ui.dialog.ModFitersDialog;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.modloaders.modpacks.ModItemAdapter;
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchFilters;

public class SearchModFragment extends FragmentWithAnim implements ModItemAdapter.SearchResultCallback {
    public static final String TAG = "SearchModFragment";
    public static final String BUNDLE_SEARCH_MODPACK = "BundleSearchModPack";
    public static final String BUNDLE_MOD_PATH = "BundleModPath";
    private SearchFilters mSearchFilters;
    private boolean isModpack;
    private String mModsPath;
    private View mMainView, mOverlay;
    private float mOverlayTopCache; // Padding cache reduce resource lookup
    private final RecyclerView.OnScrollListener mOverlayPositionListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            mOverlay.setY(MathUtils.clamp(mOverlay.getY() - dy, -mOverlay.getHeight(), mOverlayTopCache));
        }
    };
    private EditText mSearchEditText;
    private RecyclerView mRecyclerview;
    private ModItemAdapter mModItemAdapter;
    private ProgressBar mSearchProgressBar;
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
        mMainView = view;
        mSearchFilters = new SearchFilters();
        mSearchFilters.isModpack = this.isModpack;

        mOverlay = view.findViewById(R.id.search_mod_overlay);
        mSearchEditText = view.findViewById(R.id.search_mod_edittext);
        mSearchProgressBar = view.findViewById(R.id.search_mod_progressbar);
        mRecyclerview = view.findViewById(R.id.search_mod_list);
        mStatusTextView = view.findViewById(R.id.search_mod_status_text);
        ImageButton mBackButton = view.findViewById(R.id.search_mod_back);
        ImageButton mFilterButton = view.findViewById(R.id.search_mod_filter);
        ImageButton mSearchButton = view.findViewById(R.id.zh_search_mod_search);

        mDefaultTextColor = mStatusTextView.getTextColors();

        // You can only access resources after attaching to current context
        mModItemAdapter = new ModItemAdapter(new ModDependencies.SelectedMod(SearchModFragment.this,
                null, modpackApi, isModpack, mModsPath),
                mRecyclerview, getResources(), this);
        mOverlayTopCache = Tools.dpToPx(20);
        mRecyclerview.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerview.setAdapter(mModItemAdapter);

        mRecyclerview.addOnScrollListener(mOverlayPositionListener);

        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            searchMods(mSearchEditText.getText().toString());
            mSearchEditText.clearFocus();
            return false;
        });
        if (!this.isModpack) {
            mSearchEditText.setHint(R.string.zh_profile_mods_search_mod);
        }

        mOverlay.post(() -> {
            int overlayHeight = (int) (mOverlay.getHeight() - Tools.dpToPx(20));
            mRecyclerview.setPadding(mRecyclerview.getPaddingLeft(),
                    mRecyclerview.getPaddingTop() + overlayHeight,
                    mRecyclerview.getPaddingRight(),
                    mRecyclerview.getPaddingBottom());
        });
        mBackButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mFilterButton.setOnClickListener(v -> displayFilterDialog());

        mSearchButton.setOnClickListener(v -> searchMods(mSearchEditText.getText().toString()));

        searchMods(null); //自动搜索一次

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerview.removeOnScrollListener(mOverlayPositionListener);
    }

    @Override
    public void onSearchFinished() {
        mSearchProgressBar.setVisibility(View.GONE);
        AnimUtils.setVisibilityAnim(mStatusTextView, false);
    }

    @Override
    public void onSearchError(int error) {
        mSearchProgressBar.setVisibility(View.GONE);
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
        mSearchProgressBar.setVisibility(View.VISIBLE);
        mSearchFilters.name = name == null ? "" : name;
        mModItemAdapter.performSearchQuery(mSearchFilters);
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        isModpack = bundle.getBoolean(BUNDLE_SEARCH_MODPACK, false);
        mModsPath = bundle.getString(BUNDLE_MOD_PATH, null);
    }

    private void displayFilterDialog() {
        ModFitersDialog modFitersDialog = new ModFitersDialog(requireContext(), mSearchFilters);
        modFitersDialog.setOnApplyButtonClickListener(() -> searchMods(mSearchEditText.getText().toString()));
        modFitersDialog.show();
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.BounceInDown);
        ViewAnimUtils.setViewAnim(mOverlay, Techniques.FadeInDown);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mMainView, Techniques.FadeOutUp);
        super.slideOut(listener);
    }
}
