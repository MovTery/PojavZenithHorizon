package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static org.lwjgl.glfw.CallbackBridge.sendKeyPress;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.zalithlauncher.feature.ProfileLanguageSelector;
import com.movtery.zalithlauncher.feature.background.BackgroundManager;
import com.movtery.zalithlauncher.feature.background.BackgroundType;
import com.movtery.zalithlauncher.feature.log.Logging;
import com.movtery.zalithlauncher.launch.LaunchGame;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.Settings;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;
import com.movtery.zalithlauncher.ui.dialog.ControlSettingsDialog;
import com.movtery.zalithlauncher.ui.dialog.KeyboardDialog;
import com.movtery.zalithlauncher.ui.dialog.MouseSettingsDialog;
import com.movtery.zalithlauncher.ui.dialog.SeekbarDialog;
import com.movtery.zalithlauncher.ui.dialog.SelectControlsDialog;
import com.movtery.zalithlauncher.ui.dialog.TipDialog;
import com.movtery.zalithlauncher.ui.fragment.settings.VideoSettingsFragment;
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper;
import com.movtery.zalithlauncher.utils.PathAndUrlManager;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.anim.AnimUtils;
import com.movtery.zalithlauncher.utils.file.FileTools;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.customcontrols.ControlButtonMenuListener;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlDrawerData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.CustomControls;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;
import net.kdt.pojavlaunch.customcontrols.keyboard.LwjglCharSender;
import net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput;
import net.kdt.pojavlaunch.customcontrols.mouse.GyroControl;
import net.kdt.pojavlaunch.databinding.ActivityBasemainBinding;
import net.kdt.pojavlaunch.databinding.ViewControlSettingsBinding;
import net.kdt.pojavlaunch.databinding.ViewGameSettingsBinding;
import net.kdt.pojavlaunch.lifecycle.ContextExecutor;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.services.GameService;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.lwjgl.glfw.CallbackBridge;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity implements ControlButtonMenuListener, EditorExitable, ServiceConnection {
    public static volatile ClipboardManager GLOBAL_CLIPBOARD;
    public static final String INTENT_MINECRAFT_VERSION = "intent_version";

    volatile public static boolean isInputStackCall;

    @SuppressLint("StaticFieldLeak")
    private static ActivityBasemainBinding binding = null;
    private GameMenuViewWrapper mGameMenuWrapper;
    private GyroControl mGyroControl = null;
    private KeyboardDialog keyboardDialog;
    public static TouchCharInput touchCharInput;
    public static ControlLayout mControlLayout;

    MinecraftProfile minecraftProfile;

    private ViewGameSettingsBinding mGameSettingsBinding;
    private ViewControlSettingsBinding mControlSettingsBinding;
    private GameService.LocalBinder mServiceBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        minecraftProfile = LauncherProfiles.getCurrentProfile();
        MCOptionUtils.load(Tools.getGameDirPath(minecraftProfile).getAbsolutePath());
        if (AllSettings.Companion.getAutoSetGameLanguage())
            ProfileLanguageSelector.setGameLanguage(minecraftProfile, AllSettings.Companion.getGameLanguageOverridden());

        Intent gameServiceIntent = new Intent(this, GameService.class);
        // Start the service a bit early
        ContextCompat.startForegroundService(this, gameServiceIntent);
        initLayout();
        CallbackBridge.addGrabListener(binding.mainTouchpad);
        CallbackBridge.addGrabListener(binding.mainGameRenderView);
        if(AllSettings.Companion.getEnableGyro()) mGyroControl = new GyroControl(this);

        // Enabling this on TextureView results in a broken white result
        if(AllSettings.Companion.getAlternateSurface()) getWindow().setBackgroundDrawable(null);
        else getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        // Set the sustained performance mode for available APIs
        getWindow().setSustainedPerformanceMode(AllSettings.Companion.getSustainedPerformance());

        ControlLayout controlLayout = binding.mainControlLayout;
        mControlSettingsBinding = ViewControlSettingsBinding.inflate(getLayoutInflater());
        mControlSettingsBinding.addButton.setOnClickListener(v -> controlLayout.addControlButton(new ControlData(getString(R.string.controls_add_control_button))));
        mControlSettingsBinding.addDrawer.setOnClickListener(v -> controlLayout.addDrawer(new ControlDrawerData()));
        mControlSettingsBinding.addJoystick.setOnClickListener(v -> controlLayout.addJoystickButton(new ControlJoystickData()));
        mControlSettingsBinding.controlsSettings.setOnClickListener(v -> new ControlSettingsDialog(this).show());
        mControlSettingsBinding.load.setOnClickListener(v -> controlLayout.openLoadDialog());
        mControlSettingsBinding.save.setOnClickListener(v -> controlLayout.openSaveDialog());
        mControlSettingsBinding.saveAndExit.setOnClickListener(v -> controlLayout.openSaveAndExitDialog(this));
        mControlSettingsBinding.selectDefault.setOnClickListener(v -> controlLayout.openSetDefaultDialog());
        mControlSettingsBinding.export.setOnClickListener(v -> controlLayout.openExitDialog(this));
        mControlSettingsBinding.export.setText(R.string.customctrl_editor_exit);

        // Recompute the gui scale when options are changed
        MCOptionUtils.MCOptionListener optionListener = MCOptionUtils::getMcScale;
        MCOptionUtils.addMCOptionListener(optionListener);
        binding.mainControlLayout.setModifiable(false);

        // Set the activity for the executor. Must do this here, or else Tools.showErrorRemote() may not
        // execute the correct method
        ContextExecutor.setActivity(this);
        //Now, attach to the service. The game will only start when this happens, to make sure that we know the right state.
        bindService(gameServiceIntent, this, 0);
    }

    protected void initLayout() {
        binding = ActivityBasemainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mGameMenuWrapper = new GameMenuViewWrapper(this, v -> onClickedMenu());
        touchCharInput = binding.mainTouchCharInput;
        mControlLayout = binding.mainControlLayout;

        BackgroundManager.setBackgroundImage(this, BackgroundType.IN_GAME, findViewById(R.id.background_view));

        keyboardDialog = new KeyboardDialog(this).setShowSpecialButtons(false);

        binding.mainControlLayout.setMenuListener(this);

        binding.mainDrawerOptions.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        try {
            File latestLogFile = new File(PathAndUrlManager.DIR_GAME_HOME, "latestlog.txt");
            if(!latestLogFile.exists() && !latestLogFile.createNewFile())
                throw new IOException("Failed to create a new log file");
            Logger.begin(latestLogFile.getAbsolutePath());
            // FIXME: is it safe for multi thread?
            GLOBAL_CLIPBOARD = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            binding.mainTouchCharInput.setCharacterSender(new LwjglCharSender());

            if(minecraftProfile.pojavRendererName != null) {
                Logging.i("RdrDebug","__P_renderer="+minecraftProfile.pojavRendererName);
                Tools.LOCAL_RENDERER = minecraftProfile.pojavRendererName;
            }

            setTitle("Minecraft " + minecraftProfile.lastVersionId);

            // Minecraft 1.13+

            String version = getIntent().getStringExtra(INTENT_MINECRAFT_VERSION);
            version = version == null ? minecraftProfile.lastVersionId : version;

            JMinecraftVersionList.Version mVersionInfo = Tools.getVersionInfo(version);
            isInputStackCall = mVersionInfo.arguments != null;
            CallbackBridge.nativeSetUseInputStackQueue(isInputStackCall);

            Tools.getDisplayMetrics(this);
            windowWidth = Tools.getDisplayFriendlyRes(currentDisplayMetrics.widthPixels, 1f);
            windowHeight = Tools.getDisplayFriendlyRes(currentDisplayMetrics.heightPixels, 1f);


            // Menu
            mGameSettingsBinding = ViewGameSettingsBinding.inflate(getLayoutInflater());
            mGameSettingsBinding.forceClose.setOnClickListener(v -> dialogForceClose(this));
            mGameSettingsBinding.logOutput.setOnClickListener(v -> openLogOutput());
            mGameSettingsBinding.sendCustomKey.setOnClickListener(v -> dialogSendCustomKey());
            mGameSettingsBinding.mouseSettings.setOnClickListener(v -> openMouseSettings());
            mGameSettingsBinding.resolutionScaler.setOnClickListener(v -> openResolutionAdjuster());
            mGameSettingsBinding.gyroSensitivity.setOnClickListener(v -> adjustGyroSensitivityLive());
            mGameSettingsBinding.replacementCustomcontrol.setOnClickListener(v -> replacementCustomControls());
            mGameSettingsBinding.editControl.setOnClickListener(v -> openCustomControls());

            binding.mainNavigationView.removeAllViews();
            binding.mainNavigationView.addView(mGameSettingsBinding.getRoot());
            binding.mainDrawerOptions.closeDrawers();

            final String finalVersion = version;
            binding.mainGameRenderView.setSurfaceReadyListener(() -> {
                try {
                    // Setup virtual mouse right before launching
                    if (AllSettings.Companion.getVirtualMouseStart()) {
                        binding.mainTouchpad.post(() -> binding.mainTouchpad.switchState());
                    }
                    LaunchGame.runGame(this, mServiceBinder, minecraftProfile, finalVersion, mVersionInfo);
                } catch (Throwable e) {
                    Tools.showErrorRemote(e);
                }
            });

            if (AllSettings.Companion.getEnableLogOutput()) openLogOutput();

            String tipString = StringUtils.insertNewline(binding.gameTip.getText(), StringUtils.insertSpace(getString(R.string.game_tip_version), minecraftProfile.lastVersionId));
            binding.gameTip.setText(tipString);
            AnimUtils.setVisibilityAnim(binding.gameTipView, 1000, true, 300, new AnimUtils.AnimationListener() {
                @Override
                public void onStart() {
                }
                @Override
                public void onEnd() {
                    AnimUtils.setVisibilityAnim(binding.gameTipView, 15000, false, 300, null);
                }
            });
        } catch (Throwable e) {
            Tools.showError(this, e, true);
        }
    }

    private void openResolutionAdjuster() {
        new SeekbarDialog.Builder(this)
                .setTitle(R.string.setting_resolution_scaler_title)
                .setMessage(R.string.setting_resolution_scaler_desc)
                .setMin(25)
                .setMax(300)
                .setSuffix("%")
                .setValue(AllSettings.Companion.getResolutionRatio())
                .setPreviewTextContentGetter(value -> VideoSettingsFragment.getResolutionRatioPreview(getResources(), value))
                .setOnSeekbarChangeListener(value -> {
                    binding.mainGameRenderView.refreshSize(value);
                    binding.hotbarView.refreshScaleFactor(value / 100f);
                })
                .setOnSeekbarStopTrackingTouch(value -> Settings.Manager.Companion.put("resolutionRatio", value).save())
                .buildDialog();
    }

    private void loadControls() {
        try {
            // Load keys
            binding.mainControlLayout.loadLayout(
                    minecraftProfile.controlFile == null
                            ? AllSettings.Companion.getDefaultCtrl()
                            : PathAndUrlManager.DIR_CTRLMAP_PATH + "/" + minecraftProfile.controlFile);
        } catch(IOException e) {
            try {
                Logging.w("MainActivity", "Unable to load the control file, loading the default now", e);
                binding.mainControlLayout.loadLayout((String) null);
            } catch (IOException ioException) {
                Tools.showError(this, ioException);
            }
        } catch (Throwable th) {
            Tools.showError(this, th);
        }
        mGameMenuWrapper.setVisibility(!binding.mainControlLayout.hasMenuButton());
        binding.mainControlLayout.toggleControlVisible();
    }

    @Override
    public void onAttachedToWindow() {
        LauncherPreferences.computeNotchSize(this);
        loadControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mGyroControl != null) mGyroControl.enable();
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1);
    }

    @Override
    protected void onPause() {
        if(mGyroControl != null) mGyroControl.disable();
        if (CallbackBridge.isGrabbing()){
            sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE);
        }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_VISIBLE, 1);
    }

    @Override
    protected void onStop() {
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_VISIBLE, 0);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallbackBridge.removeGrabListener(binding.mainTouchpad);
        CallbackBridge.removeGrabListener(binding.mainGameRenderView);
        ContextExecutor.clearActivity();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(mGyroControl != null) mGyroControl.updateOrientation();
        Tools.updateWindowSize(this);
        binding.mainGameRenderView.refreshSize();
        runOnUiThread(() -> binding.mainControlLayout.refreshControlButtonPositions());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Tools.MAIN_HANDLER.postDelayed(() -> binding.mainGameRenderView.refreshSize(), 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                binding.mainControlLayout.loadLayout(AllSettings.Companion.getDefaultCtrl());
            } catch (IOException e) {
                Logging.e("LoadLayout", Tools.printToString(e));
            }
        }
    }

    @Override
    public boolean shouldIgnoreNotch() {
        return AllSettings.Companion.getIgnoreNotch();
    }

    private void dialogSendCustomKey() {
        keyboardDialog.setOnKeycodeSelectListener(EfficientAndroidLWJGLKeycode::execKeyIndex).show();
    }

    private void replacementCustomControls() {
        SelectControlsDialog dialog = new SelectControlsDialog(this);
        dialog.setOnSelectedListener(file -> {
            try {
                binding.mainControlLayout.loadLayout(file.getAbsolutePath());
                //刷新：是否隐藏菜单按钮
                mGameMenuWrapper.setVisibility(!binding.mainControlLayout.hasMenuButton());
            } catch (IOException ignored) {}
            dialog.dismiss();
        });
        dialog.show();
    }

    boolean isInEditor;
    private void openCustomControls() {
        binding.mainControlLayout.setModifiable(true);
        binding.mainNavigationView.removeAllViews();
        binding.mainNavigationView.addView(mControlSettingsBinding.getRoot());
        mGameMenuWrapper.setVisibility(true);
        isInEditor = true;
    }

    private void openLogOutput() {
        binding.mainLoggerView.setVisibilityWithAnim(true);
    }

    public static void toggleMouse(Context ctx) {
        if (CallbackBridge.isGrabbing()) return;

        if (binding != null) {
            Toast.makeText(ctx, binding.mainTouchpad.switchState()
                            ? R.string.control_mouseon : R.string.control_mouseoff,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void dialogForceClose(Context ctx) {
        new TipDialog.Builder(ctx)
                .setMessage(R.string.force_exit_confirm)
                .setConfirmClickListener(() -> {
                    try {
                        ZHTools.killProcess();
                    } catch (Throwable th) {
                        Logging.w(Tools.APP_NAME, "Could not enable System.exit() method!", th);
                    }
                }).buildDialog();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isInEditor) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) binding.mainControlLayout.askToExit(this);
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
        boolean handleEvent;
        if(!(handleEvent = binding.mainGameRenderView.processKeyEvent(event))) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !binding.mainTouchCharInput.isEnabled()) {
                if(event.getAction() != KeyEvent.ACTION_UP) return true; // We eat it anyway
                sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE);
                return true;
            }
        }
        return handleEvent;
    }

    public static void switchKeyboardState() {
        if (binding != null) binding.mainTouchCharInput.switchKeyboardState();
    }

    public void openMouseSettings() {
        new MouseSettingsDialog(this, (mouseSpeed, mouseScale) -> {
            Settings.Manager.Companion.put("mousespeed", mouseSpeed).save();
            Settings.Manager.Companion.put("mousescale", mouseScale).save();
            binding.mainTouchpad.updateMouseScale();
        }, () -> binding.mainTouchpad.updateMouseDrawable()).show();
    }

    public void adjustGyroSensitivityLive() {
        if(!AllSettings.Companion.getEnableGyro()) {
            Toast.makeText(this, R.string.toast_turn_on_gyro, Toast.LENGTH_LONG).show();
            return;
        }
        new SeekbarDialog.Builder(this)
                .setTitle(R.string.setting_gyro_sensitivity_title)
                .setMin(25)
                .setMax(300)
                .setValue((int) (AllSettings.Companion.getGyroSensitivity() * 100))
                .setSuffix("%")
                .setOnSeekbarStopTrackingTouch(value -> Settings.Manager.Companion.put("gyroSensitivity", value).save())
                .buildDialog();
    }

    private static void setUri(Context context, String input) {
        if(input.startsWith("file:")) {
            int truncLength = 5;
            if(input.startsWith("file://")) truncLength = 7;
            input = input.substring(truncLength);
            Logging.i("MainActivity", input);

            File inputFile = new File(input);
            FileTools.shareFile(context, inputFile);
            Logging.i("In-game Share File/Folder", "Start!");
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(input), "*/*");
            context.startActivity(intent);
        }
    }

    public static void openLink(String link) {
        Context ctx = binding.mainTouchpad.getContext(); // no more better way to obtain a context statically
        ((Activity)ctx).runOnUiThread(() -> {
            try {
                setUri(ctx, link);
            } catch (Throwable th) {
                Tools.showError(ctx, th);
            }
        });
    }
    @SuppressWarnings("unused") //TODO: actually use it
    public static void openPath(String path) {
        Context ctx = binding.mainTouchpad.getContext(); // no more better way to obtain a context statically
        ((Activity)ctx).runOnUiThread(() -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(DocumentsContract.buildDocumentUri(ctx.getString(R.string.storageProviderAuthorities), path), "*/*");
                ctx.startActivity(intent);
            } catch (Throwable th) {
                Tools.showError(ctx, th);
            }
        });
    }

    public static void querySystemClipboard() {
        Tools.runOnUiThread(()->{
            ClipData clipData = GLOBAL_CLIPBOARD.getPrimaryClip();
            if(clipData == null) {
                AWTInputBridge.nativeClipboardReceived(null, null);
                return;
            }
            ClipData.Item firstClipItem = clipData.getItemAt(0);
            //TODO: coerce to HTML if the clip item is styled
            CharSequence clipItemText = firstClipItem.getText();
            if(clipItemText == null) {
                AWTInputBridge.nativeClipboardReceived(null, null);
                return;
            }
            AWTInputBridge.nativeClipboardReceived(clipItemText.toString(), "plain");
        });
    }

    public static void putClipboardData(String data, String mimeType) {
        Tools.runOnUiThread(()-> {
            ClipData clipData = null;
            switch(mimeType) {
                case "text/plain":
                    clipData = ClipData.newPlainText("AWT Paste", data);
                    break;
                case "text/html":
                    clipData = ClipData.newHtmlText("AWT Paste", data, data);
            }
            if(clipData != null) GLOBAL_CLIPBOARD.setPrimaryClip(clipData);
        });
    }

    @Override
    public void onClickedMenu() {
        DrawerLayout drawerLayout = binding.mainDrawerOptions;
        View navigationView = binding.mainNavigationView;

        boolean open = drawerLayout.isDrawerOpen(navigationView);
        if (open) drawerLayout.closeDrawer(navigationView);
        else drawerLayout.openDrawer(navigationView);

        navigationView.requestLayout();
    }

    @Override
    public void exitEditor() {
        try {
            MainActivity.binding.mainControlLayout.loadLayout((CustomControls)null);
            MainActivity.binding.mainControlLayout.setModifiable(false);
            System.gc();
            MainActivity.binding.mainControlLayout.loadLayout(
                    minecraftProfile.controlFile == null
                            ? AllSettings.Companion.getDefaultCtrl()
                            : PathAndUrlManager.DIR_CTRLMAP_PATH + "/" + minecraftProfile.controlFile);
            mGameMenuWrapper.setVisibility(!binding.mainControlLayout.hasMenuButton());
        } catch (IOException e) {
            Tools.showError(this,e);
        }
        binding.mainNavigationView.removeAllViews();
        binding.mainNavigationView.addView(mGameSettingsBinding.getRoot());
        isInEditor = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        GameService.LocalBinder localBinder = (GameService.LocalBinder) service;
        mServiceBinder = localBinder;
        binding.mainGameRenderView.start(localBinder.isActive, binding.mainTouchpad);
        localBinder.isActive = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /*
     * Android 14 (or some devices, at least) seems to dispatch the the captured mouse events as trackball events
     * due to a bug(?) somewhere(????)
     */
    private boolean checkCaptureDispatchConditions(MotionEvent event) {
        int eventSource = event.getSource();
        // On my device, the mouse sends events as a relative mouse device.
        // Not comparing with == here because apparently `eventSource` is a mask that can
        // sometimes indicate multiple sources, like in the case of InputDevice.SOURCE_TOUCHPAD
        // (which is *also* an InputDevice.SOURCE_MOUSE when controlling a cursor)
        return (eventSource & InputDevice.SOURCE_MOUSE_RELATIVE) != 0 ||
                (eventSource & InputDevice.SOURCE_MOUSE) != 0;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if(checkCaptureDispatchConditions(ev))
            return binding.mainGameRenderView.dispatchCapturedPointerEvent(ev);
        else return super.dispatchTrackballEvent(ev);
    }
}
