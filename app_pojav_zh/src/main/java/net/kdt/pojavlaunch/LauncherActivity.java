package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.event.single.*;
import com.movtery.pojavzh.event.sticky.*;
import com.movtery.pojavzh.event.value.*;
import com.movtery.pojavzh.feature.CheckNewNotice;
import com.movtery.pojavzh.feature.UpdateLauncher;
import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils;
import com.movtery.pojavzh.feature.background.BackgroundManager;
import com.movtery.pojavzh.feature.background.BackgroundType;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.mod.modpack.install.InstallExtra;
import com.movtery.pojavzh.feature.mod.modpack.install.InstallLocalModPack;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.setting.Settings;
import com.movtery.pojavzh.ui.activity.BaseActivity;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.fragment.SelectAuthFragment;
import com.movtery.pojavzh.ui.fragment.SettingsFragment;
import com.movtery.pojavzh.ui.subassembly.settingsbutton.ButtonType;
import com.movtery.pojavzh.ui.subassembly.settingsbutton.SettingsButtonWrapper;
import com.movtery.pojavzh.ui.subassembly.view.DraggableViewWrapper;
import com.movtery.pojavzh.ui.view.AnimButton;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.stringutils.ShiftDirection;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;
import net.kdt.pojavlaunch.lifecycle.ContextAwareDoneListener;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.modloaders.modpacks.ModloaderInstallTracker;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.NotificationDownloadListener;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.services.ProgressServiceKeeper;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.tasks.MinecraftDownloader;
import net.kdt.pojavlaunch.utils.NotificationUtils;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

public class LauncherActivity extends BaseActivity {
    @SuppressLint("StaticFieldLeak") private static Activity activity;
    private final AccountsManager accountsManager = AccountsManager.getInstance();
    public final ActivityResultLauncher<Object> modInstallerLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("jar"), (data)->{
                if(data != null) Tools.launchModInstaller(this, data);
            });

    private TextView mAppTitle;
    private FragmentContainerView mFragmentView;
    private SettingsButtonWrapper mSettingsButtonWrapper;
    private ImageButton mSettingsButton;
    private ImageView mHair;
    private ProgressLayout mProgressLayout;
    private ProgressServiceKeeper mProgressServiceKeeper;
    private ModloaderInstallTracker mInstallTracker;
    private NotificationManager mNotificationManager;
    private final AnimPlayer noticeAnimPlayer = new AnimPlayer();
    private View noticeLayout;
    private Future<?> checkNotice;

    public static Activity getActivity() {
        return LauncherActivity.activity;
    }

    /* Allows to switch from one button "type" to another */
    private final FragmentManager.FragmentLifecycleCallbacks mFragmentCallbackListener = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            if (f instanceof MainMenuFragment) {
                mSettingsButtonWrapper.setButtonType(ButtonType.SETTINGS);
            } else {
                mSettingsButtonWrapper.setButtonType(ButtonType.HOME);
            }
        }
    };

    /* Listener for the settings fragment */
    private final View.OnClickListener mSettingButtonListener = v -> {
        ViewAnimUtils.setViewAnim(mSettingsButton, Animations.Pulse);
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        if(fragment instanceof MainMenuFragment){
            ZHTools.swapFragmentWithAnim(fragment, SettingsFragment.class, SettingsFragment.TAG, null);
        } else{
            // The setting button doubles as a home button now
            Tools.backToMainMenu(this);
        }
    };

    private final TaskCountListener mDoubleLaunchPreventionListener = taskCount -> {
        // Hide the notification that starts the game if there are tasks executing.
        // Prevents the user from trying to launch the game with tasks ongoing.
        if(taskCount > 0) {
            Tools.runOnUiThread(() ->
                    mNotificationManager.cancel(NotificationUtils.NOTIFICATION_ID_GAME_START)
            );
        }
    };

    private ActivityResultLauncher<String> mRequestNotificationPermissionLauncher;
    private WeakReference<Runnable> mRequestNotificationPermissionRunnable;

    @Subscribe()
    public void onPageOpacityChange(PageOpacityChangeEvent event) {
        setPageOpacity();
    }

    @Subscribe()
    public void onPageOpacityChange(MainBackgroundChangeEvent event) {
        refreshBackground();
    }

    @Subscribe()
    public void onSelectAuthMethod(SelectAuthMethodEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        // Allow starting the add account only from the main menu, should it be moved to fragment itself ?
        if (!(fragment instanceof MainMenuFragment)) return;
        ZHTools.swapFragmentWithAnim(fragment, SelectAuthFragment.class, SelectAuthFragment.TAG, null);
    }

    @Subscribe()
    public void onLaunchGameEvent(LaunchGameEvent event) {
        if (mProgressLayout.hasProcesses()) {
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return;
        }

        String selectedProfile = AllSettings.Companion.getCurrentProfile();
        if (LauncherProfiles.mainProfileJson == null || !LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile)) {
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return;
        }
        MinecraftProfile prof = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
        if (prof == null || prof.lastVersionId == null || "Unknown".equals(prof.lastVersionId)) {
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return;
        }

        if (accountsManager.getAllAccount().isEmpty()) {
            Toast.makeText(this, R.string.account_no_saved_accounts, Toast.LENGTH_LONG).show();
            EventBus.getDefault().post(new SelectAuthMethodEvent());
            return;
        }

        LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed() {
                launchGame(prof);
            }

            @Override
            public void onUsageDenied() {
                if (!AllSettings.Companion.getLocalAccountReminders()) {
                    launchGame(prof);
                } else {
                    LocalAccountUtils.openDialog(LauncherActivity.this, () -> launchGame(prof),
                            getString(R.string.account_no_microsoft_account) + getString(R.string.account_purchase_minecraft_account_tip),
                            R.string.account_continue_to_launch_the_game);
                }
            }
        });
    }

    @Subscribe()
    public void onMicrosoftLogin(MicrosoftLoginEvent event) {
        new MicrosoftBackgroundLogin(false, event.getUri().getQueryParameter("code")).performLogin(
                accountsManager.getProgressListener(), accountsManager.getDoneListener(), accountsManager.getErrorListener());
    }

    @Subscribe()
    public void onOtherLogin(OtherLoginEvent event) {
        try {
            event.getAccount().save();
            Logging.i("McAccountSpinner", "Saved the account : " + event.getAccount().username);
        } catch (IOException e) {
            Logging.e("McAccountSpinner", "Failed to save the account : " + e);
        }
        accountsManager.getDoneListener().onLoginDone(event.getAccount());
    }

    @Subscribe()
    public void onLocalLogin(LocalLoginEvent event) {
        String userName = event.getUserName();
        MinecraftAccount localAccount = new MinecraftAccount();
        localAccount.username = userName;
        try {
            localAccount.save();
            Logging.i("McAccountSpinner", "Saved the account : " + localAccount.username);
        } catch (IOException e) {
            Logging.e("McAccountSpinner", "Failed to save the account : " + e);
        }

        accountsManager.getDoneListener().onLoginDone(localAccount);
    }

    @Subscribe()
    public void onInstallLocalModpack(InstallLocalModpackEvent event) {
        InstallExtra installExtra = event.getInstallExtra();
        if (!installExtra.startInstall) return;

        if (mProgressLayout.hasProcesses()) {
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return;
        }

        File dirGameModpackFile = new File(installExtra.modpackPath);
        ModPackUtils.ModPackEnum type;
        type = ModPackUtils.determineModpack(dirGameModpackFile);

        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.generic_waiting);
        PojavApplication.sExecutorService.execute(() -> {
            try {
                ModLoader loaderInfo = InstallLocalModPack.installModPack(this, type, dirGameModpackFile, () -> runOnUiThread(installExtra.dialog::dismiss));
                if (loaderInfo == null) return;
                loaderInfo.getDownloadTask(new NotificationDownloadListener(this, loaderInfo)).run();
            } catch (Exception e) {
                installExtra.dialog.dismiss();
                Tools.showErrorRemote(this, R.string.modpack_install_download_failed, e);
            } finally {
                ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);
        FragmentManager fragmentManager = getSupportFragmentManager();
        // If we don't have a back stack root yet...
        if(fragmentManager.getBackStackEntryCount() < 1) {
            // Manually add the first fragment to the backstack to get easily back to it
            // There must be a better way to handle the root though...
            // (artDev: No, there is not. I've spent days researching this for another unrelated project.)
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack("ROOT")
                    .add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT").commit();
        }

        mRequestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isAllowed -> {
                    if(!isAllowed) handleNoNotificationPermission();
                    else {
                        Runnable runnable = Tools.getWeakReference(mRequestNotificationPermissionRunnable);
                        if(runnable != null) runnable.run();
                    }
                }
        );
        bindViews();
        setPageOpacity();
        refreshBackground();

        checkNotificationPermission();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ProgressKeeper.addTaskCountListener(mDoubleLaunchPreventionListener);
        ProgressKeeper.addTaskCountListener((mProgressServiceKeeper = new ProgressServiceKeeper(this)));

        mSettingsButton.setOnClickListener(mSettingButtonListener);
        mAppTitle.setOnClickListener(v -> mAppTitle.setText(StringUtils.shiftString(mAppTitle.getText().toString(), ShiftDirection.RIGHT, 1)));

        ProgressKeeper.addTaskCountListener(mProgressLayout);

        new AsyncVersionList().getVersionList(versions -> EventBus.getDefault().postSticky(
                new MinecraftVersionValueEvent(versions)),
                false
        );

        mInstallTracker = new ModloaderInstallTracker(this);

        mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
        mProgressLayout.observe(ProgressLayout.INSTALL_MODPACK);
        mProgressLayout.observe(ProgressLayout.AUTHENTICATE_MICROSOFT);
        mProgressLayout.observe(ProgressLayout.DOWNLOAD_VERSION_LIST);

        noticeLayout = findViewById(R.id.notice_layout);
        noticeLayout.findViewById(R.id.notice_got_button).setOnClickListener(v -> {
            setNotice(false);
            Settings.Manager.Companion.put("noticeDefault", false)
                    .save();
        });
        new DraggableViewWrapper(noticeLayout, new DraggableViewWrapper.AttributesFetcher() {
            @NonNull
            @Override
            public DraggableViewWrapper.ScreenPixels getScreenPixels() {
                return new DraggableViewWrapper.ScreenPixels(0, 0,
                        currentDisplayMetrics.widthPixels - noticeLayout.getWidth(),
                        currentDisplayMetrics.heightPixels - noticeLayout.getHeight());
            }

            @NonNull
            @Override
            public int[] get() {
                return new int[]{(int) noticeLayout.getX(), (int) noticeLayout.getY()};
            }

            @Override
            public void set(int x, int y) {
                noticeLayout.setX(x);
                noticeLayout.setY(y);
            }
        }).init();

        checkNotice();

        // 愚人节彩蛋
        if (ZHTools.checkDate(4, 1)) mHair.setVisibility(View.VISIBLE);
        else mHair.setVisibility(View.GONE);

        //检查已经下载后的包，或者检查更新
        PojavApplication.sExecutorService.execute(() -> UpdateLauncher.CheckDownloadedPackage(this, true));

        LauncherActivity.activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
        mInstallTracker.attach();
        setPageOpacity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContextExecutor.clearActivity();
        mInstallTracker.detach();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentCallbackListener, true);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressLayout.cleanUpObservers();
        ProgressKeeper.removeTaskCountListener(mProgressLayout);
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper);

        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(mFragmentCallbackListener);
    }

    /** Custom implementation to feel more natural when a backstack isn't present */
    @Override
    public void onBackPressed() {
        MicrosoftLoginFragment fragment = (MicrosoftLoginFragment) getVisibleFragment(MicrosoftLoginFragment.TAG);
        if(fragment != null){
            if(fragment.canGoBack()){
                fragment.goBack();
                return;
            }
        }

        // Check if we are at the root then
        if(getVisibleFragment("ROOT") != null){
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public void onAttachedToWindow() {
        LauncherPreferences.computeNotchSize(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LauncherActivity.activity = this;
    }

    private void checkNotice() {
        checkNotice = PojavApplication.sExecutorService.submit(() -> CheckNewNotice.checkNewNotice(this, noticeInfo -> {
            if (checkNotice.isCancelled() || noticeInfo == null) {
                return;
            }
            //当偏好设置内是开启通知栏 或者 检测到通知编号不为偏好设置里保存的值时，显示通知栏
            if (AllSettings.Companion.getNoticeDefault() ||
                    (noticeInfo.numbering != AllSettings.Companion.getNoticeNumbering())) {
                Tools.runOnUiThread(() -> setNotice(true));
                Settings.Manager.Companion.put("noticeDefault", true)
                        .put("noticeNumbering", noticeInfo.numbering)
                        .save();
            }
        }));
    }

    private void setNotice(boolean show) {
        AnimButton gotButton = noticeLayout.findViewById(R.id.notice_got_button);

        if (show) {
            CheckNewNotice.NoticeInfo noticeInfo = CheckNewNotice.getNoticeInfo();
            if (noticeInfo != null) {
                TextView title = noticeLayout.findViewById(R.id.notice_title_view);
                TextView message = noticeLayout.findViewById(R.id.notice_message_view);
                TextView date = noticeLayout.findViewById(R.id.notice_date_view);

                gotButton.setClickable(true);

                title.setText(noticeInfo.rawTitle);
                message.setText(noticeInfo.substance);
                date.setText(noticeInfo.rawDate);

                Linkify.addLinks(message, Linkify.WEB_URLS);
                message.setMovementMethod(LinkMovementMethod.getInstance());

                noticeAnimPlayer.clearEntries();
                noticeAnimPlayer.apply(new AnimPlayer.Entry(noticeLayout, Animations.BounceEnlarge))
                        .setOnStart(() -> noticeLayout.setVisibility(View.VISIBLE))
                        .start();
            }
        } else {
            gotButton.setClickable(false);

            noticeAnimPlayer.clearEntries();
            noticeAnimPlayer.apply(new AnimPlayer.Entry(noticeLayout, Animations.BounceShrink))
                    .setOnStart(() -> noticeLayout.setVisibility(View.VISIBLE))
                    .setOnEnd(() -> noticeLayout.setVisibility(View.GONE))
                    .start();
        }
    }

    private void refreshBackground() {
        BackgroundManager.setBackgroundImage(this, BackgroundType.MAIN_MENU, findViewById(R.id.background_view));
    }

    private void launchGame(MinecraftProfile prof) {
        String normalizedVersionId = AsyncMinecraftDownloader.normalizeVersionId(prof.lastVersionId);
        JMinecraftVersionList.Version mcVersion = AsyncMinecraftDownloader.getListedVersion(normalizedVersionId);
        new MinecraftDownloader().start(
                mcVersion,
                normalizedVersionId,
                new ContextAwareDoneListener(this, normalizedVersionId)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private Fragment getVisibleFragment(String tag){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment != null && fragment.isVisible()) {
            return fragment;
        }
        return null;
    }

    @SuppressWarnings("unused")
    private Fragment getVisibleFragment(int id){
        Fragment fragment = getSupportFragmentManager().findFragmentById(id);
        if(fragment != null && fragment.isVisible()) {
            return fragment;
        }
        return null;
    }

    private void checkNotificationPermission() {
        if(AllSettings.Companion.getSkipNotificationPermissionCheck() ||
            checkForNotificationPermission()) {
            return;
        }

        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS)) {
            showNotificationPermissionReasoning();
            return;
        }
        askForNotificationPermission(null);
    }

    private void showNotificationPermissionReasoning() {
        new TipDialog.Builder(this)
                .setTitle(R.string.notification_permission_dialog_title)
                .setMessage(R.string.notification_permission_dialog_text)
                .setConfirmClickListener(() -> askForNotificationPermission(null))
                .setCancelClickListener(this::handleNoNotificationPermission)
                .buildDialog();
    }

    private void handleNoNotificationPermission() {
        Settings.Manager.Companion
                .put("skipNotificationPermissionCheck", true)
                .save();
        Toast.makeText(this, R.string.notification_permission_toast, Toast.LENGTH_LONG).show();
    }

    public boolean checkForNotificationPermission() {
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_DENIED;
    }

    public void askForNotificationPermission(Runnable onSuccessRunnable) {
        if(Build.VERSION.SDK_INT < 33) return;
        if(onSuccessRunnable != null) {
            mRequestNotificationPermissionRunnable = new WeakReference<>(onSuccessRunnable);
        }
        mRequestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void setPageOpacity() {
        if (mFragmentView != null) {
            float v = (float) AllSettings.Companion.getPageOpacity() / 100;
            if (mFragmentView.getAlpha() != v) mFragmentView.setAlpha(v);
        }
    }

    /** Stuff all the view boilerplate here */
    private void bindViews(){
        mFragmentView = findViewById(R.id.container_fragment);
        mSettingsButton = findViewById(R.id.setting_button);
        mProgressLayout = findViewById(R.id.progress_layout);
        mAppTitle = findViewById(R.id.app_title_text);

        mSettingsButtonWrapper = new SettingsButtonWrapper(mSettingsButton);
        mSettingsButtonWrapper.setOnTypeChangeListener(type -> ViewAnimUtils.setViewAnim(mSettingsButton, Animations.Pulse));

        mHair = findViewById(R.id.zh_hair);
    }
}
