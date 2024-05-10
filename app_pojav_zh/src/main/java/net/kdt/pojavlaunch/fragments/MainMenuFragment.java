package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ADVANCED_FEATURES;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_ANIMATION;
import static net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles.getCurrentProfile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.CheckNewNotice;
import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.dialog.ShareLogDialog;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";
    private CheckNewNotice.NoticeInfo noticeInfo;
    private mcVersionSpinner mVersionSpinner;
    private View mLauncherNoticeView, mDividingLineView;
    private ImageButton mNoticeSummonButton;
    private Button mNoticeCloseButton;
    private Timer mCheckNoticeTimer;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindValues(view);

        Button mAboutButton = view.findViewById(R.id.about_button);
        Button mCustomControlButton = view.findViewById(R.id.custom_control_button);
        Button mInstallJarButton = view.findViewById(R.id.install_jar_button);
        Button mShareLogsButton = view.findViewById(R.id.share_logs_button);
        Button mOpenMainDirButton = view.findViewById(R.id.zh_open_main_dir_button);
        Button mOpenInstanceDirButton = view.findViewById(R.id.zh_open_instance_dir_button);

        ImageButton mEditProfileButton = view.findViewById(R.id.edit_profile_button);
        Button mPlayButton = view.findViewById(R.id.play_button);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        mAboutButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), AboutFragment.class, AboutFragment.TAG, null));
        mCustomControlButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        mInstallJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        mInstallJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        mEditProfileButton.setOnClickListener(v -> mVersionSpinner.openProfileEditor(requireActivity()));

        mPlayButton.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true));

        mShareLogsButton.setOnClickListener(v -> {
            ShareLogDialog shareLogDialog = new ShareLogDialog(requireContext(), new File(Tools.DIR_GAME_HOME + "/latestlog.txt"));
            shareLogDialog.show();
        });

        mOpenMainDirButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, Tools.DIR_GAME_HOME);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        mOpenInstanceDirButton.setOnClickListener(v -> {
            String path = PojavZHTools.getGameDirPath(getCurrentProfile().gameDir).getAbsolutePath();
            File file = new File(path);
            if (!file.exists()) file.mkdirs(); //必须保证此路径存在

            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_PATH, path);
            Tools.swapFragment(requireActivity(), FilesFragment.class, FilesFragment.TAG, bundle);
        });

        initNotice(view);
        mOpenMainDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
        mOpenInstanceDirButton.setVisibility(PREF_ADVANCED_FEATURES ? View.VISIBLE : View.GONE);
    }

    private void initNotice(View view) {
        mNoticeSummonButton.setOnClickListener(v -> {
            DEFAULT_PREF.edit().putBoolean("noticeDefault", true).apply();
            setNotice(true, true, view);
        });

        mNoticeCloseButton.setOnClickListener(v -> {
            DEFAULT_PREF.edit().putBoolean("noticeDefault", false).apply();
            setNotice(false, true, view);
        });

        setNotice(false, false, view);

        mCheckNoticeTimer = new Timer();
        mCheckNoticeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (CheckNewNotice.isFailure()) { //如果访问失败了，那么取消计时器
                    mCheckNoticeTimer.cancel();
                    return;
                }

                noticeInfo = CheckNewNotice.getNoticeInfo();
                if (noticeInfo == null) return;

                //当偏好设置内是开启通知栏 或者 检测到通知编号不为偏好设置里保存的值时，显示通知栏
                if (DEFAULT_PREF.getBoolean("noticeDefault", false) ||
                        (noticeInfo.getNumbering() != DEFAULT_PREF.getInt("numbering", 0))) {
                    runOnUiThread(() -> setNotice(true, false, view));
                    SharedPreferences.Editor editor = DEFAULT_PREF.edit();
                    editor.putBoolean("noticeDefault", true);
                    editor.putInt("numbering", noticeInfo.getNumbering());
                    editor.apply();
                    mCheckNoticeTimer.cancel();
                }
            }
        }, 0, 500); // 0.5秒钟检查一次
    }

    private void bindValues(View view) {
        mLauncherNoticeView = view.findViewById(R.id.zh_menu_notice);
        mDividingLineView = view.findViewById(R.id.dividing_line);
        mNoticeSummonButton = view.findViewById(R.id.zh_menu_notice_summon_button);
        mNoticeCloseButton = view.findViewById(R.id.zh_menu_notice_close_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVersionSpinner.reloadProfiles();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCheckNoticeTimer != null) {
            mCheckNoticeTimer.cancel();
        }
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    private void setNotice(boolean show, boolean anim, View view) {
        mNoticeSummonButton.setClickable(false);
        mNoticeCloseButton.setClickable(false);

        if (PREF_ANIMATION && anim) {
            //通过分割线来设置通知栏划出动画
            mLauncherNoticeView.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mDividingLineView.getLayoutParams();

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

            //召唤按钮动画
            mNoticeSummonButton.setVisibility(View.VISIBLE);
            mNoticeSummonButton.animate()
                    .alpha(show ? 0 : 1)
                    .setDuration(200)
                    .start();

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (show) {
                    checkNewNotice(view);
                }
                mNoticeCloseButton.setClickable(show);
                mNoticeSummonButton.setClickable(!show);
                mNoticeSummonButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }, 250);
        } else {
            mLauncherNoticeView.setVisibility(show ? View.VISIBLE : View.GONE);
            mNoticeSummonButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mNoticeCloseButton.setClickable(show);
            mNoticeSummonButton.setClickable(!show);
            //设置分割线位置
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mDividingLineView.getLayoutParams();
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
            TextView noticeSubstanceWebView = view.findViewById(R.id.zh_menu_notice_substance);

            if (!noticeInfo.getRawTitle().equals("NONE")) {
                noticeTitleView.setText(noticeInfo.getRawTitle());
            }

            noticeDateView.setText(noticeInfo.getRawDate());
            noticeSubstanceWebView.setText(noticeInfo.getSubstance());
        });
    }
}
