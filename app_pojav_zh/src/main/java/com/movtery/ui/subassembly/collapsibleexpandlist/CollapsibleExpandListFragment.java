package com.movtery.ui.subassembly.collapsibleexpandlist;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;

import java.util.concurrent.Future;

public class CollapsibleExpandListFragment extends Fragment {
    private RecyclerView mModVersionView;
    private ProgressBar mProgressBar;
    private TextView mLoadingText, mModNameText;
    private ImageView mModIcon;
    private Button mReturnButton, mRefreshButton;
    private CheckBox mReleaseCheckBox;
    private Future<?> currentTask;
    private OnRefreshListener onRefreshListener;

    public CollapsibleExpandListFragment() {
        super(R.layout.fragment_mod_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        init();
    }

    protected void init() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        if (PREF_ANIMATION) mModVersionView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        mModVersionView.setLayoutManager(layoutManager);

        mRefreshButton.setOnClickListener(v -> refresh());
        mReleaseCheckBox.setOnClickListener(v -> refresh());
        mReturnButton.setOnClickListener(v -> PojavZHTools.onBackPressed(requireActivity()));

        refresh();
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    private void cancelTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    private void refresh() {
        if (onRefreshListener != null) {
            cancelTask();
            currentTask = onRefreshListener.onRefresh();
        }
    }

    protected void componentProcessing(boolean state) {
        mProgressBar.setVisibility(state ? View.VISIBLE : View.GONE);
        mLoadingText.setVisibility(state ? View.VISIBLE : View.GONE);
        mModVersionView.setVisibility(state ? View.GONE : View.VISIBLE);

        mRefreshButton.setClickable(!state);
        mReleaseCheckBox.setClickable(!state);
    }

    private void bindViews(View view) {
        mModVersionView = view.findViewById(R.id.zh_mod);
        mProgressBar = view.findViewById(R.id.zh_mod_loading);
        mLoadingText = view.findViewById(R.id.zh_mod_loading_text);
        mModIcon = view.findViewById(R.id.zh_mod_icon);
        mModNameText = view.findViewById(R.id.zh_mod_name);
        mReturnButton = view.findViewById(R.id.zh_mod_return_button);
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button);
        mReleaseCheckBox = view.findViewById(R.id.zh_mod_release_version);
    }

    protected void setOnRefreshListener(OnRefreshListener listener) {
        this.onRefreshListener = listener;
    }

    protected void setModNameText(String modNameText) {
        this.mModNameText.setText(modNameText);
    }

    protected void setModIcon(Drawable modIcon) {
        this.mModIcon.setImageDrawable(modIcon);
    }

    public RecyclerView getModVersionView() {
        return mModVersionView;
    }

    public CheckBox getReleaseCheckBox() {
        return mReleaseCheckBox;
    }

    public Future<?> getCurrentTask() {
        return currentTask;
    }

    protected interface OnRefreshListener {
        Future<?> onRefresh();
    }
}
