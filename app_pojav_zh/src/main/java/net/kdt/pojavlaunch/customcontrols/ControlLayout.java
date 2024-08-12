package net.kdt.pojavlaunch.customcontrols;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import static org.lwjgl.glfw.CallbackBridge.isGrabbing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlButton;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlInterface;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlJoystick;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlSubButton;
import net.kdt.pojavlaunch.customcontrols.handleview.ActionRow;
import net.kdt.pojavlaunch.customcontrols.handleview.ControlHandleView;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlPopup;
import com.movtery.pojavzh.ui.dialog.EditControlInfoDialog;
import com.movtery.pojavzh.ui.dialog.SelectControlsDialog;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ControlLayout extends FrameLayout {
	protected CustomControls mLayout;
	private ControlInfoData mInfoData;
	/* Accessible when inside the game by ControlInterface implementations, cached for perf. */
	private MinecraftGLSurface mGameSurface = null;

	/* Cache to buttons for performance purposes */
	private List<ControlInterface> mButtons;
	private boolean mModifiable = false;
	private boolean mIsModified;
	private boolean mControlVisible = false;

	private EditControlPopup mControlPopup = null;
	private ControlHandleView mHandleView;
	private ControlButtonMenuListener mMenuListener;
	public ActionRow mActionRow = null;
	public String mLayoutFileName;

	public ControlLayout(Context ctx) {
		super(ctx);
	}

	public ControlLayout(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}


	public void loadLayout(String jsonPath) throws IOException, JsonSyntaxException {
		File jsonFile = new File(jsonPath);

		CustomControls layout;
		if (jsonFile.exists()) {
			layout = LayoutConverter.loadAndConvertIfNecessary(getContext(), jsonPath);
		} else {
			layout = LayoutConverter.loadFromAssets(getContext(), "default.json");
		}
		if(layout != null) {
			loadLayout(layout);
			if (jsonFile.exists()) {
				updateLoadedFileName(jsonPath);
			} else {
				mLayoutFileName = "default";
			}
		}
	}

	public void loadLayout(CustomControls controlLayout) {
		mInfoData = controlLayout == null ? null : controlLayout.mControlInfoDataList;
		if (mInfoData == null) {
			mInfoData = new ControlInfoData();
		}

		if(mActionRow == null){
			mActionRow = new ActionRow(getContext());
			addView(mActionRow);
		}

		removeAllButtons();
		if(mLayout != null) {
			mLayout.mControlDataList = null;
			mLayout = null;
		}

		System.gc();
		mapTable.clear();

		// Cleanup buttons only when input layout is null
		if (controlLayout == null) return;

		mLayout = controlLayout;
		

		// Joystick(s) first, to workaround the touch dispatch
		for(ControlJoystickData joystick : mLayout.mJoystickDataList){
			addJoystickView(joystick);
		}

		//CONTROL BUTTON
		for (ControlData button : controlLayout.mControlDataList) {
			addControlView(button);
		}

		//CONTROL DRAWER
		for(ControlDrawerData drawerData : controlLayout.mDrawerDataList){
			ControlDrawer drawer = addDrawerView(drawerData);
			if(mModifiable) drawer.areButtonsVisible = true;
		}

		mLayout.scaledAt = LauncherPreferences.PREF_BUTTONSIZE;

		setModified(false);
		mButtons = null;
		getButtonChildren(); // Force refresh
	} // loadLayout

	//CONTROL BUTTON
	public void addControlButton(ControlData controlButton) {
		mLayout.mControlDataList.add(controlButton);
		addControlView(controlButton);
	}

	private void addControlView(ControlData controlButton) {
		final ControlButton view = new ControlButton(this, controlButton);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);

		setModified(true);
	}

	// CONTROL DRAWER
	public void addDrawer(ControlDrawerData drawerData){
		mLayout.mDrawerDataList.add(drawerData);
		addDrawerView();
	}

	private void addDrawerView(){
		addDrawerView(null);
	}

	private ControlDrawer addDrawerView(ControlDrawerData drawerData){

		final ControlDrawer view = new ControlDrawer(this,drawerData == null ? mLayout.mDrawerDataList.get(mLayout.mDrawerDataList.size()-1) : drawerData);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);
		//CONTROL SUB BUTTON
		for (ControlData subButton : view.getDrawerData().buttonProperties) {
			addSubView(view, subButton);
		}

		setModified(true);
		return view;
	}

	//CONTROL SUB-BUTTON
	public void addSubButton(ControlDrawer drawer, ControlData controlButton){
		//Yep there isn't much here
		drawer.getDrawerData().buttonProperties.add(controlButton);
		addSubView(drawer, drawer.getDrawerData().buttonProperties.get(drawer.getDrawerData().buttonProperties.size()-1 ));
	}

	private void addSubView(ControlDrawer drawer, ControlData controlButton){
		final ControlSubButton view = new ControlSubButton(this, controlButton, drawer);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}else{
			view.setVisible(true);
		}

		addView(view);
		drawer.addButton(view);


		setModified(true);
	}

	// JOYSTICK BUTTON
	public void addJoystickButton(ControlJoystickData data){
		mLayout.mJoystickDataList.add(data);
		addJoystickView(data);
	}

	private void addJoystickView(ControlJoystickData data){
		ControlJoystick view = new ControlJoystick(this, data);

		if (!mModifiable) {
			view.setAlpha(view.getProperties().opacity);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}
		addView(view);

	}


	private void removeAllButtons() {
		for(ControlInterface button : getButtonChildren()){
			removeView(button.getControlView());
		}

		System.gc();
		//i wanna be sure that all the removed Views will be removed after a reload
		//because if frames will slowly go down after many control changes it will be warm and bad
	}

	public void saveLayout(String path) throws Exception {
		mLayout.save(path);
		setModified(false);
	}

	public void toggleControlVisible(){
		mControlVisible = !mControlVisible;
		setControlVisible(mControlVisible);
	}

	public float getLayoutScale(){
		return mLayout.scaledAt;
	}

	public CustomControls getLayout(){
		return mLayout;
	}

	public void setControlVisible(boolean isVisible) {
		if (mModifiable) return; // Not using on custom controls activity

		mControlVisible = isVisible;
		for(ControlInterface button : getButtonChildren()){
			button.setVisible(((button.getProperties().displayInGame && isGrabbing()) || (button.getProperties().displayInMenu && !isGrabbing())) && isVisible);
		}
	}

	public void setModifiable(boolean isModifiable) {
		if(!isModifiable && mModifiable){
			removeEditWindow();
		}
		mModifiable = isModifiable;
		if(isModifiable){
			// In edit mode, all controls have to be shown
			for(ControlInterface button : getButtonChildren()){
				button.setVisible(true);
			}
		}
	}

	public boolean getModifiable(){
		return mModifiable;
	}

	public void setModified(boolean isModified) {
		mIsModified = isModified;
	}

	public List<ControlInterface> getButtonChildren(){
		if(mModifiable || mButtons == null){
			mButtons = new ArrayList<>();
			for(int i=0; i<getChildCount(); ++i){
				View v = getChildAt(i);
				if(v instanceof ControlInterface)
					mButtons.add(((ControlInterface) v));
			}
		}

		return mButtons;
	}

	public void refreshControlButtonPositions(){
		for(ControlInterface button : getButtonChildren()){
			button.setDynamicX(button.getProperties().dynamicX);
			button.setDynamicY(button.getProperties().dynamicY);
		}
	}

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if(child instanceof ControlInterface && mControlPopup != null){
            mControlPopup.disappearColor();
            mControlPopup.disappear();
        }
    }

    /**
	 * Load the layout if needed, and pass down the burden of filling values
	 * to the button at hand.
	 */
	public void editControlButton(ControlInterface button){
		if(mControlPopup == null){
			// When the panel is null, it needs to inflate first.
			// So inflate it, then process it on the next frame
			mControlPopup = new EditControlPopup(getContext(), this);
			post(() -> editControlButton(button));
			return;
		}

		mControlPopup.internalChanges = true;
		mControlPopup.setCurrentlyEditedButton(button);
		button.loadEditValues(mControlPopup);

		mControlPopup.internalChanges = false;

		mControlPopup.appear(button.getControlView().getX() + button.getControlView().getWidth()/2f < currentDisplayMetrics.widthPixels/2f);
		mControlPopup.disappearColor();

		if(mHandleView == null){
			mHandleView = new ControlHandleView(getContext());
			addView(mHandleView);
		}
		mHandleView.setControlButton(button);

		//mHandleView.show();
	}

	/** Swap the panel if the button position requires it */
	public void adaptPanelPosition(){
		if(mControlPopup != null)
			mControlPopup.adaptPanelPosition();
	}


	final HashMap<View, ControlInterface> mapTable = new HashMap<>();

	//While this is called onTouch, this should only be called from a ControlButton.
	public void onTouch(View v, MotionEvent ev) {
		ControlInterface lastControlButton = mapTable.get(v);

		// Map location to screen coordinates
		ev.offsetLocation(v.getX(), v.getY());


		//Check if the action is cancelling, reset the lastControl button associated to the view
		if (ev.getActionMasked() == MotionEvent.ACTION_UP
				|| ev.getActionMasked() == MotionEvent.ACTION_CANCEL
				|| ev.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
			if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
			mapTable.put(v, null);
			return;
		}

		if (ev.getActionMasked() != MotionEvent.ACTION_MOVE) return;


		//Optimization pass to avoid looking at all children again
		if (lastControlButton != null) {
			System.out.println("last control button check" + ev.getX() + "-" + ev.getY() + "-" + lastControlButton.getControlView().getX() + "-" + lastControlButton.getControlView().getY());
			if (ev.getX() > lastControlButton.getControlView().getX()
					&& ev.getX() < lastControlButton.getControlView().getX() + lastControlButton.getControlView().getWidth()
					&& ev.getY() > lastControlButton.getControlView().getY()
					&& ev.getY() < lastControlButton.getControlView().getY() + lastControlButton.getControlView().getHeight()) {
				return;
			}
		}

		//Release last keys
		if (lastControlButton != null) lastControlButton.sendKeyPresses(false);
		mapTable.remove(v);

		// Update the state of all swipeable buttons
		for (ControlInterface button : getButtonChildren()) {
			if (!button.getProperties().isSwipeable) continue;

			if (ev.getX() > button.getControlView().getX()
					&& ev.getX() < button.getControlView().getX() + button.getControlView().getWidth()
					&& ev.getY() > button.getControlView().getY()
					&& ev.getY() < button.getControlView().getY() + button.getControlView().getHeight()) {

				//Press the new key
				if (!button.equals(lastControlButton)) {
					button.sendKeyPresses(true);
					mapTable.put(v, button);
					return;
				}

			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mModifiable && event.getActionMasked() != MotionEvent.ACTION_UP || mControlPopup == null)
			return true;

		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		// When the input window cannot be hidden, it returns false
		if(!imm.hideSoftInputFromWindow(getWindowToken(), 0)){
			if(mControlPopup.disappearLayer()){
				mActionRow.setFollowedButton(null);
				mHandleView.hide();
			}
		}
		return true;
	}

	public void removeEditWindow() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

		// When the input window cannot be hidden, it returns false
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
		if(mControlPopup != null) {
			mControlPopup.disappearColor();
			mControlPopup.disappear();
		}

		if(mActionRow != null) mActionRow.setFollowedButton(null);
		if(mHandleView != null) mHandleView.hide();
	}

	public void save(String path){
		try {
			mLayout.save(path);
		} catch (IOException e) {
			Logging.e("ControlLayout", "Failed to save the layout at:" + path);}
	}


	public boolean hasMenuButton() {
		for(ControlInterface controlInterface : getButtonChildren()){
			for (int keycode : controlInterface.getProperties().keycodes) {
				if (keycode == ControlData.SPECIALBTN_MENU) return true;
			}
		}
		return false;
	}

	public void setMenuListener(ControlButtonMenuListener menuListener) {
		this.mMenuListener = menuListener;
	}

	public void notifyAppMenu() {
		if(mMenuListener != null) mMenuListener.onClickedMenu();
	}

	/** Cached getter for perf purposes */
	public MinecraftGLSurface getGameSurface(){
		if(mGameSurface == null){
			mGameSurface = findViewById(R.id.main_game_render_view);
		}
		return mGameSurface;
	}

	public void askToExit(EditorExitable editorExitable) {
		if(mIsModified) {
			openSaveAndExitDialog(editorExitable);
		}else{
			openExitDialog(editorExitable);
		}
	}

	public void updateLoadedFileName(String path) {
		path = path.replace(PathAndUrlManager.DIR_CTRLMAP_PATH, ".");
		path = path.substring(0, path.length() - 5);
		mLayoutFileName = path;
	}

	public String saveToDirectory(String name) throws Exception{
		String jsonPath = PathAndUrlManager.DIR_CTRLMAP_PATH + "/" + name + ".json";
		saveLayout(jsonPath);
		return jsonPath;
	}

	private void saveDialog(String title, Runnable confirmRunnable) {
		EditControlInfoDialog infoDialog = new EditControlInfoDialog(getContext(), true, mLayoutFileName, mInfoData);

		if (title != null && !title.isEmpty()) infoDialog.setTitle(title);

		infoDialog.setOnConfirmClickListener((fileName, controlInfoData) -> {
			try {
				String jsonPath = saveToDirectory(fileName);
				Toast.makeText(getContext(), getContext().getString(R.string.global_save) + ": " + jsonPath, Toast.LENGTH_SHORT).show();
				if (confirmRunnable != null) PojavApplication.sExecutorService.execute(confirmRunnable);
			} catch (Throwable th) {
				Tools.showError(getContext(), th, true);
			}

			infoDialog.dismiss();
		});
		infoDialog.show();
	}

	public void openSaveDialog() {
		saveDialog(getContext().getString(R.string.global_save), null);
	}

	public void openSaveAndExitDialog(EditorExitable editorExitable) {
		saveDialog(getContext().getString(R.string.global_save_and_exit), () -> runOnUiThread(editorExitable::exitEditor));
	}

	public void openLoadDialog() {
		SelectControlsDialog dialog = new SelectControlsDialog(getContext());
		dialog.setOnSelectedListener(file -> {
			try {
				loadLayout(file.getAbsolutePath());
			} catch (IOException e) {
				Tools.showError(getContext(), e);
			}
		});
		dialog.show();
	}

	public void openSetDefaultDialog() {
		SelectControlsDialog dialog = new SelectControlsDialog(getContext());
		dialog.setOnSelectedListener(file -> {
            String absolutePath = file.getAbsolutePath();
            try {
				LauncherPreferences.DEFAULT_PREF.edit().putString("defaultCtrl", absolutePath).apply();
				LauncherPreferences.PREF_DEFAULTCTRL_PATH = absolutePath;
				loadLayout(absolutePath);
            }catch (IOException|JsonSyntaxException e) {
                Tools.showError(getContext(), e);
            }
            dialog.dismiss();
        });
		dialog.show();
	}

	public void openExitDialog(EditorExitable exitListener) {
		new TipDialog.Builder(getContext())
				.setTitle(R.string.customctrl_editor_exit_title)
				.setMessage(R.string.customctrl_editor_exit_msg)
				.setConfirmClickListener(exitListener::exitEditor)
				.buildDialog();
	}

	public boolean areControlVisible(){
		return mControlVisible;
	}
}
