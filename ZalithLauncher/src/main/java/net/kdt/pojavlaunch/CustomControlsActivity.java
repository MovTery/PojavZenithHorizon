package net.kdt.pojavlaunch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.FrameLayout;

import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.zalithlauncher.feature.background.BackgroundManager;
import com.movtery.zalithlauncher.feature.background.BackgroundType;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;
import com.movtery.zalithlauncher.ui.dialog.ControlSettingsDialog;
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper;

import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlDrawerData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;
import net.kdt.pojavlaunch.databinding.ActivityCustomControlsBinding;
import net.kdt.pojavlaunch.databinding.ViewControlSettingsBinding;

import java.io.IOException;

public class CustomControlsActivity extends BaseActivity implements EditorExitable {
	public static final String BUNDLE_CONTROL_PATH = "control_path";
	private ActivityCustomControlsBinding binding;
	private String mControlPath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mControlPath = bundle.getString(BUNDLE_CONTROL_PATH);
		}

		binding = ActivityCustomControlsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ControlLayout controlLayout = binding.customctrlControllayout;
		DrawerLayout drawerLayout = binding.customctrlDrawerlayout;
		FrameLayout drawerNavigationView = binding.customctrlNavigationView;

		new GameMenuViewWrapper(this, v -> {
			boolean open = drawerLayout.isDrawerOpen(drawerNavigationView);

			if (open) drawerLayout.closeDrawer(drawerNavigationView);
			else drawerLayout.openDrawer(drawerNavigationView);
		}).setVisibility(true);

		BackgroundManager.setBackgroundImage(this, BackgroundType.CUSTOM_CONTROLS, binding.backgroundView);

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		ViewControlSettingsBinding controlSettingsBinding = ViewControlSettingsBinding.inflate(getLayoutInflater());
		controlSettingsBinding.addButton.setOnClickListener(v -> controlLayout.addControlButton(new ControlData(getString(R.string.controls_add_control_button))));
		controlSettingsBinding.addDrawer.setOnClickListener(v -> controlLayout.addDrawer(new ControlDrawerData()));
		controlSettingsBinding.addJoystick.setOnClickListener(v -> controlLayout.addJoystickButton(new ControlJoystickData()));
		controlSettingsBinding.controlsSettings.setOnClickListener(v -> new ControlSettingsDialog(this).show());
		controlSettingsBinding.load.setOnClickListener(v -> controlLayout.openLoadDialog());
		controlSettingsBinding.save.setOnClickListener(v -> controlLayout.openSaveDialog());
		controlSettingsBinding.saveAndExit.setOnClickListener(v -> controlLayout.openSaveAndExitDialog(this));
		controlSettingsBinding.selectDefault.setOnClickListener(v -> controlLayout.openSetDefaultDialog());
		controlSettingsBinding.export.setOnClickListener(v -> {
			try { // Saving the currently shown control
				Uri contentUri = DocumentsContract.buildDocumentUri(getString(R.string.storageProviderAuthorities), controlLayout.saveToDirectory(controlLayout.mLayoutFileName));

				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
				shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				shareIntent.setType("application/json");
				startActivity(shareIntent);

				Intent sendIntent = Intent.createChooser(shareIntent, controlLayout.mLayoutFileName);
				startActivity(sendIntent);
			} catch (Exception e) {
				Tools.showError(this, e);
			}
		});

		drawerNavigationView.addView(controlSettingsBinding.getRoot());
		controlLayout.setModifiable(true);
		try {
			if (mControlPath == null) controlLayout.loadLayout(AllSettings.Companion.getDefaultCtrl());
			else controlLayout.loadLayout(mControlPath);
		}catch (IOException e) {
			Tools.showError(this, e);
		}
	}

	@Override
	public boolean shouldIgnoreNotch() {
		return AllSettings.Companion.getIgnoreNotch();
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		binding.customctrlControllayout.askToExit(this);
	}

	@Override
	public void exitEditor() {
		super.onBackPressed();
	}
}
