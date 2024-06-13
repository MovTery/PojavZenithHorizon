package com.movtery.ui.subassembly.twolevellist;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.utils.PojavZHTools;

import net.kdt.pojavlaunch.R;

import java.util.concurrent.Future;

public abstract class TwoLevelListFragment extends Fragment {
    private RecyclerView.Adapter<?> parentAdapter = null;
    private RecyclerView mRecyclerView;
    private View mLoadeingView;
    private TextView mNameText, mSelectTitle, mFailedToLoad;
    private ImageView mIcon;
    private Button mReturnButton, mRefreshButton;
    private CheckBox mReleaseCheckBox;
    private Future<?> currentTask;
    private boolean releaseCheckBoxVisible = true;

    public TwoLevelListFragment() {
        super(R.layout.fragment_mod_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        init();
    }

    protected void init() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        if (PREF_ANIMATION) mRecyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards)));
        mRecyclerView.setLayoutManager(layoutManager);

        mRefreshButton.setOnClickListener(v -> refreshTask());
        mReleaseCheckBox.setOnClickListener(v -> refreshTask());
        mReturnButton.setOnClickListener(v -> {
            if (parentAdapter != null) {
                hideParentElement(false);
                mRecyclerView.setAdapter(parentAdapter);
                mRecyclerView.scheduleLayoutAnimation();
                parentAdapter = null;
            } else {
                PojavZHTools.onBackPressed(requireActivity());
            }
        });

        refreshTask();
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    private void hideParentElement(boolean visible) {
        int titleVisibility = visible ? View.VISIBLE : View.GONE;
        int checkBoxVisibility = visible ? View.GONE : View.VISIBLE;

        if (PREF_ANIMATION) {
            int titleStartAlpha = visible ? 1 : 0;
            int titleEndAlpha = visible ? 0 : 1;
            int checkBoxStartAlpha = visible ? 0 : 1;
            int checkBoxEndAlpha = visible ? 1 : 0;

            PojavZHTools.fadeAnim(mSelectTitle, 0, titleEndAlpha, titleStartAlpha, 200,
                    () -> mSelectTitle.setVisibility(titleVisibility));

            if (releaseCheckBoxVisible) {
                mReleaseCheckBox.setClickable(false);
                PojavZHTools.fadeAnim(mReleaseCheckBox, 0, checkBoxEndAlpha, checkBoxStartAlpha, 200,
                        () -> {
                            mReleaseCheckBox.setVisibility(checkBoxVisibility);
                            mReleaseCheckBox.setClickable(true);
                        });
            }
        } else {
            mSelectTitle.setVisibility(titleVisibility);
            if (releaseCheckBoxVisible) {
                mReleaseCheckBox.setVisibility(checkBoxVisibility);
            }
        }
    }

    private void cancelTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    private void refreshTask() {
        currentTask = refresh();
    }

    protected abstract Future<?> refresh();

    protected void componentProcessing(boolean state) {
        mLoadeingView.setVisibility(state ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(state ? View.GONE : View.VISIBLE);

        mRefreshButton.setClickable(!state);
        mReleaseCheckBox.setClickable(!state);
    }

    private void bindViews(View view) {
        mRecyclerView = view.findViewById(R.id.zh_mod);
        mLoadeingView = view.findViewById(R.id.zh_mod_loading);
        mIcon = view.findViewById(R.id.zh_mod_icon);
        mNameText = view.findViewById(R.id.zh_mod_name);
        mSelectTitle = view.findViewById(R.id.zh_select_title);
        mFailedToLoad = view.findViewById(R.id.zh_mod_failed_to_load);

        mReturnButton = view.findViewById(R.id.zh_mod_return_button);
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button);
        mReleaseCheckBox = view.findViewById(R.id.zh_mod_release_version);
    }

    protected void setNameText(String nameText) {
        this.mNameText.setText(nameText);
    }

    protected void setIcon(Drawable icon) {
        this.mIcon.setImageDrawable(icon);
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected CheckBox getReleaseCheckBox() {
        return mReleaseCheckBox;
    }

    protected Future<?> getCurrentTask() {
        return currentTask;
    }

    protected void setReleaseCheckBoxGone() {
        releaseCheckBoxVisible = false;
        mReleaseCheckBox.setVisibility(View.GONE);
    }

    protected void setFailedToLoad(boolean failed) {
        if (PREF_ANIMATION) {
            PojavZHTools.fadeAnim(mFailedToLoad, 0, failed ? 0 : 1, failed ? 1 : 0, 200,
                    () -> mFailedToLoad.setVisibility(failed ? View.VISIBLE : View.GONE));
        } else {
            mFailedToLoad.setVisibility(failed ? View.VISIBLE : View.GONE);
        }
    }

    protected void switchToChild(RecyclerView.Adapter<?> adapter, String title) {
        if (adapter != null) {
            //保存父级，设置选中的标题文本，切换至子级
            parentAdapter = mRecyclerView.getAdapter();
            mSelectTitle.setText(title);
            hideParentElement(true);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.scheduleLayoutAnimation();
        }
    }
}
