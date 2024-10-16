package net.kdt.pojavlaunch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.pojavzh.feature.background.BackgroundManager;
import com.movtery.pojavzh.feature.background.BackgroundType;
import com.movtery.pojavzh.setting.AllSettings;
import com.movtery.pojavzh.ui.activity.BaseActivity;
import com.movtery.pojavzh.ui.dialog.ControlSettingsDialog;
import com.movtery.pojavzh.ui.subassembly.view.GameMenuViewWrapper;

import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlDrawerData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;
import net.kdt.pojavlaunch.databinding.ActivityCustomControlsBinding;

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
		ListView drawerNavigationView = binding.customctrlNavigationView;

		new GameMenuViewWrapper(this, v -> {
			boolean open = drawerLayout.isDrawerOpen(drawerNavigationView);

			if (open) drawerLayout.closeDrawer(drawerNavigationView);
			else drawerLayout.openDrawer(drawerNavigationView);
		}).setVisibility(true);

		BackgroundManager.setBackgroundImage(this, BackgroundType.CUSTOM_CONTROLS, binding.backgroundView);

		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		drawerNavigationView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.menu_customcontrol_customactivity)));
		drawerNavigationView.setOnItemClickListener((parent, view, position, id) -> {
			switch(position) {
				case 0: controlLayout.addControlButton(new ControlData(getString(R.string.controls_add_control_button))); break;
				case 1: controlLayout.addDrawer(new ControlDrawerData()); break;
				case 2: controlLayout.addJoystickButton(new ControlJoystickData()); break;
				case 3: new ControlSettingsDialog(this).show(); break;
				case 4: controlLayout.openLoadDialog(); break;
				case 5: controlLayout.openSaveDialog(); break;
				case 6: controlLayout.openSaveAndExitDialog(this); break;
				case 7: controlLayout.openSetDefaultDialog(); break;
				case 8: // Saving the currently shown control
					try {
						Uri contentUri = DocumentsContract.buildDocumentUri(getString(R.string.storageProviderAuthorities), controlLayout.saveToDirectory(controlLayout.mLayoutFileName));

						Intent shareIntent = new Intent();
						shareIntent.setAction(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
						shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						shareIntent.setType("application/json");
						startActivity(shareIntent);

						Intent sendIntent = Intent.createChooser(shareIntent, controlLayout.mLayoutFileName);
						startActivity(sendIntent);
					}catch (Exception e) {
						Tools.showError(this, e);
					}
					break;
			}
			drawerLayout.closeDrawers();
		});
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
