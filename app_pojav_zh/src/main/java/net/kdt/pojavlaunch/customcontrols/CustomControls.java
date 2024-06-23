package net.kdt.pojavlaunch.customcontrols;

import androidx.annotation.Keep;

import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlInfoData;

import java.io.IOException;
import java.util.*;
import net.kdt.pojavlaunch.*;

@Keep
public class CustomControls {
	public int version = 7;
    public float scaledAt;
	public List<ControlData> mControlDataList;
	public List<ControlDrawerData> mDrawerDataList;
	public List<ControlJoystickData> mJoystickDataList;
	public ControlInfoData mControlInfoDataList;

	public CustomControls() {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ControlInfoData());
	}

	public CustomControls(List<ControlData> mControlDataList, List<ControlDrawerData> mDrawerDataList, List<ControlJoystickData> mJoystickDataList, ControlInfoData mControlInfoDataList) {
		this.mControlDataList = mControlDataList;
		this.mDrawerDataList = mDrawerDataList;
		this.mJoystickDataList = mJoystickDataList;
		this.mControlInfoDataList = mControlInfoDataList;
		this.scaledAt = 100f;
	}

	public void save(String path) throws IOException {
		//Current version is the V3.1 so the version as to be marked as 7 !
		version = 7;
		Tools.write(path, Tools.GLOBAL_GSON.toJson(this));
	}
}
