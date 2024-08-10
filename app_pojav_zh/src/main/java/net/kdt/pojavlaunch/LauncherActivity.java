package net.kdt.pojavlaunch;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import com.daimajia.androidanimations.library.Techniques;
import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.UpdateLauncher;
import com.movtery.pojavzh.feature.accounts.AccountUpdateListener;
import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils;
import com.movtery.pojavzh.feature.mod.modpack.install.InstallExtra;
import com.movtery.pojavzh.feature.mod.modpack.install.InstallLocalModPack;
import com.movtery.pojavzh.feature.mod.modpack.install.ModPackUtils;
import com.movtery.pojavzh.ui.activity.SettingsActivity;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType;
import com.movtery.pojavzh.ui.subassembly.settingsbutton.ButtonType;
import com.movtery.pojavzh.ui.subassembly.settingsbutton.SettingsButtonWrapper;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.stringutils.ShiftDirection;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.authenticator.microsoft.MicrosoftBackgroundLogin;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;
import net.kdt.pojavlaunch.fragments.SelectAuthFragment;
import net.kdt.pojavlaunch.lifecycle.ContextAwareDoneListener;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.modloaders.modpacks.ModloaderInstallTracker;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.NotificationDownloadListener;
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.IconCacheJanitor;
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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class LauncherActivity extends BaseActivity {
    @SuppressLint("StaticFieldLeak") private static Activity activity;
    private final AccountsManager accountsManager = AccountsManager.getInstance();
    public final ActivityResultLauncher<Object> modInstallerLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("jar"), (data)->{
                if(data != null) Tools.launchModInstaller(this, data);
            });

    private View mBackgroundView;
    private TextView mAppTitle;
    private FragmentContainerView mFragmentView;
    private SettingsButtonWrapper mSettingsButtonWrapper;
    private ImageButton mSettingsButton;
    private ImageView mHair;
    private ProgressLayout mProgressLayout;
    private ProgressServiceKeeper mProgressServiceKeeper;
    private ModloaderInstallTracker mInstallTracker;
    private NotificationManager mNotificationManager;

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

    /* Listener for the auth method selection screen */
    private final ExtraListener<Boolean> mSelectAuthMethod = (key, value) -> {
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        // Allow starting the add account only from the main menu, should it be moved to fragment itself ?
        if(!(fragment instanceof MainMenuFragment)) return false;

        ZHTools.swapFragmentWithAnim(fragment, SelectAuthFragment.class, SelectAuthFragment.TAG, null);
        return false;
    };

    private final ExtraListener<InstallExtra> mInstallLocalModpack = (key, value) -> {
        if (!value.startInstall) return false;

        if (mProgressLayout.hasProcesses()) {
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return false;
        }

        File dirGameModpackFile = new File(value.modpackPath);
        ModPackUtils.ModPackEnum type;
        type = ModPackUtils.determineModpack(dirGameModpackFile);

        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.global_waiting);
        PojavApplication.sExecutorService.execute(() -> {
            try {
                ModLoader loaderInfo = InstallLocalModPack.installModPack(this, type, dirGameModpackFile, () -> runOnUiThread(value.dialog::dismiss));
                if (loaderInfo == null) return;
                loaderInfo.getDownloadTask(new NotificationDownloadListener(this, loaderInfo)).run();
            }catch (Exception e) {
                value.dialog.dismiss();
                Tools.showErrorRemote(this, R.string.modpack_install_download_failed, e);
            }finally {
                ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
            }
        });

        return false;
    };

    /* Listener for the settings fragment */
    private final View.OnClickListener mSettingButtonListener = v -> {
        ViewAnimUtils.setViewAnim(mSettingsButton, Techniques.Pulse);
        Fragment fragment = getSupportFragmentManager().findFragmentById(mFragmentView.getId());
        if(fragment instanceof MainMenuFragment){
            startActivity(new Intent(this, SettingsActivity.class));
        } else{
            // The setting button doubles as a home button now
            Tools.backToMainMenu(this);
        }
    };

    private final ExtraListener<Uri> mMicrosoftLoginListener = (key, value) -> {
        new MicrosoftBackgroundLogin(false, value.getQueryParameter("code")).performLogin(
                accountsManager.getProgressListener(), accountsManager.getDoneListener(), accountsManager.getErrorListener());
        return false;
    };

    // Test mode
    private final ExtraListener<String[]> mLocalLoginListener = (key, value) -> {
        if (value[1].isEmpty()) { // Test mode
            MinecraftAccount account = new MinecraftAccount();
            account.username = value[0];
            try {
                account.save();
            } catch (IOException e) {
                Log.e("McAccountSpinner", "Failed to save the account : " + e);
            }

            accountsManager.getDoneListener().onLoginDone(account);
        }
        return false;
    };

    private final ExtraListener<MinecraftAccount> mOtherLoginListener = (key, value) -> {
        try {
            value.save();
        } catch (IOException e) {
            Log.e("McAccountSpinner", "Failed to save the account : " + e);
        }
        accountsManager.getDoneListener().onLoginDone(value);
        return false;
    };

    private final ExtraListener<Boolean> mAccountUpdateListener = (key, value) -> {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AccountUpdateListener) {
                ((AccountUpdateListener) fragment).onUpdate();
                return false;
            }
        }
        return false;
    };

    private final ExtraListener<Boolean> mLaunchGameListener = (key, value) -> {
        if(mProgressLayout.hasProcesses()){
            Toast.makeText(this, R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            return false;
        }

        String selectedProfile = DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE,"");
        if (LauncherProfiles.mainProfileJson == null || !LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile)){
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return false;
        }
        MinecraftProfile prof = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
        if (prof == null || prof.lastVersionId == null || "Unknown".equals(prof.lastVersionId)){
            Toast.makeText(this, R.string.error_no_version, Toast.LENGTH_LONG).show();
            return false;
        }

        if (accountsManager.getAllAccount().isEmpty()) {
            Toast.makeText(this, R.string.no_saved_accounts, Toast.LENGTH_LONG).show();
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            return false;
        }

        LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed() {
                launchGame(prof);
            }

            @Override
            public void onUsageDenied() {
                if (!DEFAULT_PREF.getBoolean("localAccountReminders", true)) {
                    launchGame(prof);
                } else {
                    LocalAccountUtils.openDialog(LauncherActivity.this, () -> launchGame(prof),
                            getString(R.string.zh_account_no_microsoft_account) + getString(R.string.zh_account_purchase_minecraft_account_tip),
                            R.string.zh_account_continue_to_launch_the_game);
                }
            }
        });

        return false;
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

    @Override
    protected boolean shouldIgnoreNotch() {
        return getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT || super.shouldIgnoreNotch();
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

        IconCacheJanitor.runJanitor();
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
        ZHTools.setBackgroundImage(this, BackgroundType.MAIN_MENU, mBackgroundView);

        checkNotificationPermission();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ProgressKeeper.addTaskCountListener(mDoubleLaunchPreventionListener);
        ProgressKeeper.addTaskCountListener((mProgressServiceKeeper = new ProgressServiceKeeper(this)));

        mSettingsButton.setOnClickListener(mSettingButtonListener);
        mAppTitle.setOnClickListener(v -> mAppTitle.setText(StringUtils.shiftString(mAppTitle.getText().toString(), ShiftDirection.RIGHT, 1)));

        ProgressKeeper.addTaskCountListener(mProgressLayout);

        ExtraCore.addExtraListener(ExtraConstants.MICROSOFT_LOGIN_TODO, mMicrosoftLoginListener);
        ExtraCore.addExtraListener(ZHExtraConstants.LOCAL_LOGIN_TODO, mLocalLoginListener);
        ExtraCore.addExtraListener(ZHExtraConstants.OTHER_LOGIN_TODO, mOtherLoginListener);
        ExtraCore.addExtraListener(ZHExtraConstants.ACCOUNT_UPDATE, mAccountUpdateListener);

        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);

        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);

        ExtraCore.addExtraListener(ZHExtraConstants.INSTALL_LOCAL_MODPACK, mInstallLocalModpack);

        new AsyncVersionList().getVersionList(versions -> ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions), false);

        mInstallTracker = new ModloaderInstallTracker(this);

        mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
        mProgressLayout.observe(ProgressLayout.INSTALL_MODPACK);
        mProgressLayout.observe(ProgressLayout.AUTHENTICATE_MICROSOFT);
        mProgressLayout.observe(ProgressLayout.DOWNLOAD_VERSION_LIST);

        // 愚人节彩蛋
        if (ZHTools.checkDate(4, 1)) mHair.setVisibility(View.VISIBLE);
        else mHair.setVisibility(View.GONE);

        //检查已经下载后的包，或者检查更新
        PojavApplication.sExecutorService.execute(() -> UpdateLauncher.CheckDownloadedPackage(this, true));

        requestSponsorship();


        LauncherActivity.activity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
        mInstallTracker.attach();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressLayout.cleanUpObservers();
        ProgressKeeper.removeTaskCountListener(mProgressLayout);
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);
        ExtraCore.removeExtraListenerFromValue(ZHExtraConstants.INSTALL_LOCAL_MODPACK, mInstallLocalModpack);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.MICROSOFT_LOGIN_TODO, mMicrosoftLoginListener);
        ExtraCore.removeExtraListenerFromValue(ZHExtraConstants.LOCAL_LOGIN_TODO, mLocalLoginListener);
        ExtraCore.removeExtraListenerFromValue(ZHExtraConstants.OTHER_LOGIN_TODO, mOtherLoginListener);
        ExtraCore.removeExtraListenerFromValue(ZHExtraConstants.ACCOUNT_UPDATE, mAccountUpdateListener);

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
        if(LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK ||
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
        LauncherPreferences.PREF_SKIP_NOTIFICATION_PERMISSION_CHECK = true;
        DEFAULT_PREF.edit()
                .putBoolean(LauncherPreferences.PREF_KEY_SKIP_NOTIFICATION_CHECK, true)
                .apply();
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

    private void requestSponsorship() {
        if ((ZHTools.getCurrentTimeMillis() - LauncherPreferences.PREF_FIRST_LAUNCH_TIME > 5 * 60 * 60 * 1000) && !DEFAULT_PREF.getBoolean("noLongerRequestingSponsorship", false)) {
            new TipDialog.Builder(this)
                    .setTitle(R.string.zh_request_sponsorship_title)
                    .setMessage(R.string.zh_request_sponsorship_message)
                    .setConfirm(R.string.zh_about_button_support_development)
                    .setConfirmClickListener(() -> Tools.openURL(this, PathAndUrlManager.URL_SUPPORT))
                    .setDialogDismissListener(() -> {
                        DEFAULT_PREF.edit().putBoolean("noLongerRequestingSponsorship", true).apply();
                        return true;
                    })
                    .buildDialog();
        }
    }

    /** Stuff all the view boilerplate here */
    private void bindViews(){
        mBackgroundView = findViewById(R.id.background_view);

        mFragmentView = findViewById(R.id.container_fragment);
        mSettingsButton = findViewById(R.id.setting_button);
        mProgressLayout = findViewById(R.id.progress_layout);
        mAppTitle = findViewById(R.id.app_title_text);

        mSettingsButtonWrapper = new SettingsButtonWrapper(mSettingsButton);
        mSettingsButtonWrapper.setOnTypeChangeListener(type -> ViewAnimUtils.setViewAnim(mSettingsButton, Techniques.Pulse));

        mHair = findViewById(R.id.zh_hair);
    }
}
