package com.movtery.pojavzh.ui.subassembly.twolevellist;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.utils.AnimUtils;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;

import java.util.concurrent.Future;

public abstract class TwoLevelListFragment extends Fragment {
    protected FragmentActivity activity;
    private RecyclerView.Adapter<?> parentAdapter = null;
    private RecyclerView mRecyclerView;
    private View mLoadeingView;
    private TextView mNameText, mSelectTitle, mFailedToLoad;
    private ImageView mIcon;
    private ImageButton mBackToTop;
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
                ZHTools.onBackPressed(requireActivity());
            }
        });

        mBackToTop.setOnClickListener(v -> mRecyclerView.smoothScrollToPosition(0));

        refreshTask();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = requireActivity();
    }

    @Override
    public void onPause() {
        cancelTask();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        cancelTask();
        super.onDestroy();
    }

    private void hideParentElement(boolean visible) {
        cancelTask(); //中断当前正在执行的任务

        int titleVisibility = visible ? View.VISIBLE : View.GONE;
        int refreshVisibility = visible ? View.GONE : View.VISIBLE;

        mRefreshButton.setClickable(!visible);
        mReleaseCheckBox.setClickable(!visible);

        if (PREF_ANIMATION) {
            AnimUtils.setVisibilityAnim(mSelectTitle, visible);
            AnimUtils.setVisibilityAnim(mRefreshButton, !visible);

            if (releaseCheckBoxVisible) AnimUtils.setVisibilityAnim(mReleaseCheckBox, !visible);
        } else {
            mSelectTitle.setVisibility(titleVisibility);
            mRefreshButton.setVisibility(refreshVisibility);
            if (releaseCheckBoxVisible) {
                mReleaseCheckBox.setVisibility(refreshVisibility);
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
        mBackToTop = view.findViewById(R.id.zh_mod_back_to_top);
        mLoadeingView = view.findViewById(R.id.zh_mod_loading);
        mIcon = view.findViewById(R.id.zh_mod_icon);
        mNameText = view.findViewById(R.id.zh_mod_name);
        mSelectTitle = view.findViewById(R.id.zh_select_title);
        mFailedToLoad = view.findViewById(R.id.zh_mod_failed_to_load);

        mReturnButton = view.findViewById(R.id.zh_mod_return_button);
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button);
        mReleaseCheckBox = view.findViewById(R.id.zh_mod_release_version);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (layoutManager != null && adapter != null) {
                    int firstPosition = layoutManager.findFirstVisibleItemPosition();
                    boolean b = firstPosition >= adapter.getItemCount() / 3;

                    AnimUtils.setVisibilityAnim(mBackToTop, b);
                }
            }
        });
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
            AnimUtils.setVisibilityAnim(mFailedToLoad, failed);
        } else {
            mFailedToLoad.setVisibility(failed ? View.VISIBLE : View.GONE);
        }
    }

    protected void switchToChild(RecyclerView.Adapter<?> adapter, String title) {
        if (currentTask.isDone() && adapter != null) {
            //保存父级，设置选中的标题文本，切换至子级
            parentAdapter = mRecyclerView.getAdapter();
            mSelectTitle.setText(title);
            hideParentElement(true);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.scheduleLayoutAnimation();
        }
    }
}
