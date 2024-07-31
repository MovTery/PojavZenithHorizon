package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.kdt.mcgui.mcVersionSpinner;
import com.movtery.pojavzh.feature.accounts.AccountUpdateListener;
import com.movtery.pojavzh.ui.fragment.AboutFragment;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.ControlButtonFragment;
import com.movtery.pojavzh.ui.fragment.FilesFragment;

import com.movtery.pojavzh.feature.CheckNewNotice;
import com.movtery.pojavzh.ui.fragment.ProfileManagerFragment;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import com.movtery.pojavzh.ui.dialog.ShareLogDialog;
import com.movtery.pojavzh.ui.fragment.ProfilePathManagerFragment;
import com.movtery.pojavzh.ui.subassembly.account.AccountView;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.OnSlideOutListener;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;

import java.io.File;
import java.util.concurrent.Future;

public class MainMenuFragment extends FragmentWithAnim implements TaskCountListener, AccountUpdateListener {
    public static final String TAG = "MainMenuFragment";
    private AccountView accountView;
    private CheckNewNotice.NoticeInfo noticeInfo = null;
    private ImageButton mPathManagerButton, mManagerProfileButton;
    private Button mPlayButton;
    private mcVersionSpinner mVersionSpinner;
    private View mMenuLayout, mPlayLayout, mShadowView;
    private View mLauncherNoticeView, mDividingLineView;
    private Button mNoticeCloseButton;
    private boolean mTasksRunning;
    private Future<?> future;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindValues(view);
        ProgressKeeper.addTaskCountListener(this);

        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);

        mAboutButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, AboutFragment.class, AboutFragment.TAG, null));
        mAboutButton.setOnLongClickListener(v -> {
            setNotice(true, true, view);
            SharedPreferences.Editor editor = DEFAULT_PREF.edit();
            editor.putBoolean("noticeDefault", true);
            editor.apply();
            return true;
        });
        mCustomControlButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        mPathManagerButton.setOnClickListener(v -> {
            if (!mTasksRunning) {
                ViewAnimUtils.setViewAnim(mPathManagerButton, Techniques.Bounce);
                ZHTools.swapFragmentWithAnim(this, ProfilePathManagerFragment.class, ProfilePathManagerFragment.TAG, null);
            } else {
                ViewAnimUtils.setViewAnim(mPathManagerButton, Techniques.Shake);
                runOnUiThread(() -> Toast.makeText(requireContext(), R.string.zh_profiles_path_task_in_progress, Toast.LENGTH_SHORT).show());
            }
        });
        mManagerProfileButton.setOnClickListener(v -> {
            ViewAnimUtils.setViewAnim(mManagerProfileButton, Techniques.Bounce);
            ZHTools.swapFragmentWithAnim(this, ProfileManagerFragment.class, ProfileManagerFragment.TAG, null);
        });

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext(), new File(Tools.DIR_GAME_HOME + "/latestlog.txt"));
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
            bundle.putString(FilesFragment.BUNDLE_LIST_PATH, Tools.DIR_GAME_HOME);
            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        });

        initNotice(view);

        ViewAnimUtils.slideInAnim(this);
    }

    private void initNotice(View view) {
        mNoticeCloseButton.setOnClickListener(v -> {
            DEFAULT_PREF.edit().putBoolean("noticeDefault", false).apply();
            setNotice(false, true, view);
        });

        setNotice(false, false, view);

        future = PojavApplication.sExecutorService.submit(() -> CheckNewNotice.checkNewNotice(requireContext(), noticeInfo -> {
            if (future.isCancelled() || noticeInfo == null) {
                return;
            }

            //当偏好设置内是开启通知栏 或者 检测到通知编号不为偏好设置里保存的值时，显示通知栏
            if (DEFAULT_PREF.getBoolean("noticeDefault", false) ||
                    (noticeInfo.numbering != DEFAULT_PREF.getInt("noticeNumbering", 0))) {
                runOnUiThread(() -> setNotice(true, false, view));
                SharedPreferences.Editor editor = DEFAULT_PREF.edit();
                editor.putBoolean("noticeDefault", true);
                editor.putInt("noticeNumbering", noticeInfo.numbering);
                editor.apply();
            }
        }));
    }

    private void bindValues(View view) {
        mMenuLayout = view.findViewById(R.id.launcher_menu);
        mPlayLayout = view.findViewById(R.id.play_layout);
        mShadowView = view.findViewById(R.id.shadowView);
        mPathManagerButton = view.findViewById(R.id.path_manager_button);
        mManagerProfileButton = view.findViewById(R.id.manager_profile_button);
        mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);
        accountView = new AccountView(view.findViewById(R.id.view_account));
        accountView.refreshAccountInfo();

        mLauncherNoticeView = view.findViewById(R.id.zh_menu_notice);
        mDividingLineView = view.findViewById(R.id.dividing_line);
        mNoticeCloseButton = view.findViewById(R.id.zh_menu_notice_close_button);

        mVersionSpinner.setParentFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    @Override
    public void onDestroy() {
        future.cancel(true);
        super.onDestroy();
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    private void setNotice(boolean show, boolean anim, View view) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mDividingLineView.getLayoutParams();
        if ((params.horizontalBias == 0 && !show) || ((params.horizontalBias > 0 && params.horizontalBias <= 0.5) && show)) return;
        mNoticeCloseButton.setClickable(false);

        if (anim) {
            //通过分割线来设置通知栏划出动画
            mLauncherNoticeView.setVisibility(View.VISIBLE);

            @SuppressLint("ObjectAnimatorBinding")
            ObjectAnimator animator = ObjectAnimator.ofFloat(params, "horizontalBias", show ? 0f : 0.5f, show ? 0.5f : 0f);
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                params.horizontalBias = (float) animation.getAnimatedValue();
                mDividingLineView.setLayoutParams(params);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (!show) mLauncherNoticeView.setVisibility(View.GONE); //关闭通知栏隐藏通知栏
                }
            });
            animator.start();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (show) {
                    checkNewNotice(view);
                }
                mNoticeCloseButton.setClickable(show);
            }, 250);
        } else {
            mLauncherNoticeView.setVisibility(show ? View.VISIBLE : View.GONE);
            mNoticeCloseButton.setClickable(show);
            //设置分割线位置
            params.horizontalBias = show ? 0.5f : 0f;
            mDividingLineView.setLayoutParams(params);
            checkNewNotice(view);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkNewNotice(View view) {
        noticeInfo = CheckNewNotice.getNoticeInfo();

        if (noticeInfo == null) {
            return;
        }

        runOnUiThread(() -> {
            //初始化
            TextView noticeTitleView = view.findViewById(R.id.zh_menu_notice_title);
            TextView noticeDateView = view.findViewById(R.id.zh_menu_notice_date);
            TextView noticeSubstanceTextView = view.findViewById(R.id.zh_menu_notice_substance);

            if (!noticeInfo.rawTitle.equals("NONE")) {
                noticeTitleView.setText(noticeInfo.rawTitle);
            }

            noticeDateView.setText(noticeInfo.rawDate);
            noticeSubstanceTextView.setText(noticeInfo.substance);

            //文本内的网址可以被点击
            Linkify.addLinks(noticeSubstanceTextView, Linkify.WEB_URLS);
            noticeSubstanceTextView.setMovementMethod(LinkMovementMethod.getInstance());
        });
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        mTasksRunning = taskCount != 0;
    }

    @Override
    public void onUpdate() {
        accountView.refreshAccountInfo();
    }

    @Override
    public void slideIn() {
        ViewAnimUtils.setViewAnim(mMenuLayout, Techniques.BounceInDown);
        ViewAnimUtils.setViewAnim(mPlayLayout, Techniques.BounceInLeft);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.BounceInLeft);

        ViewAnimUtils.setViewAnim(accountView.getMainView(), Techniques.FlipInY);
        ViewAnimUtils.setViewAnim(mPathManagerButton, Techniques.FadeInLeft);
        ViewAnimUtils.setViewAnim(mManagerProfileButton, Techniques.FadeInLeft);
        ViewAnimUtils.setViewAnim(mVersionSpinner, Techniques.FadeInLeft);
        ViewAnimUtils.setViewAnim(mPlayButton, Techniques.FadeInLeft);
    }

    @Override
    public void slideOut(@NonNull OnSlideOutListener listener) {
        ViewAnimUtils.setViewAnim(mMenuLayout, Techniques.FadeOutUp);
        ViewAnimUtils.setViewAnim(mPlayLayout, Techniques.FadeOutRight);
        ViewAnimUtils.setViewAnim(mShadowView, Techniques.FadeOutRight);
        super.slideOut(listener);
    }
}
