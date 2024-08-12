package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.Architecture.is32BitsDevice;
import static net.kdt.pojavlaunch.Tools.getTotalDeviceMemory;
import static net.kdt.pojavlaunch.Tools.runOnUiThread;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.preference.Preference;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;

import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.utils.file.FileTools;
import com.movtery.pojavzh.utils.platform.MemoryUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.util.Timer;
import java.util.TimerTask;

public class LauncherPreferenceJavaFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceJavaFragment";
    private final Timer timer = new Timer();
    private final ActivityResultLauncher<Object> mVmInstallLauncher =
            registerForActivityResult(new OpenDocumentWithExtension("xz"), (data) -> {
                if (data != null) Tools.installRuntimeFromUri(getContext(), data);
            });
    private MultiRTConfigDialog mDialogScreen;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        int ramAllocation = LauncherPreferences.PREF_RAM_ALLOCATION;
        // Triggers a write for some reason
        addPreferencesFromResource(R.xml.pref_java);

        CustomSeekBarPreference allocationSeek = requirePreference("allocation", CustomSeekBarPreference.class);

        int maxRAM;
        int deviceRam = getTotalDeviceMemory(allocationSeek.getContext());

        if (is32BitsDevice() || deviceRam < 2048) maxRAM = Math.min(1024, deviceRam);
        else maxRAM = deviceRam - (deviceRam < 3064 ? 800 : 1024); //To have a minimum for the device to breathe

        allocationSeek.setRange(256, maxRAM);
        allocationSeek.setValue(ramAllocation);
        allocationSeek.setSuffix(" MB");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!allocationSeek.isUserSeeking()) {
                        updateMemoryInfo(getContext(), allocationSeek);
                    }
                } catch (Exception e) {
                    Logging.e("updateMemoryInfo", e.toString());
                }
            }
        }, 0, 500);

        allocationSeek.setOnDialogInitListener(new CustomSeekBarPreference.OnPreferenceClickDialog() {
            @Override
            public String getTitle() {
                return getString(R.string.mcl_memory_allocation);
            }

            @Override
            public String getMessage() {
                return StringUtils.insertNewline(getMemoryInfoText(requireContext()), getString(R.string.zh_setting_java_memory_max, String.format("%s MB", maxRAM)));
            }
        });

        Preference editJVMArgs = findPreference("javaArgs");
        if (editJVMArgs != null) {
            editJVMArgs.setOnPreferenceClickListener(preference -> {
                new EditTextDialog.Builder(requireContext())
                        .setTitle(R.string.mcl_setting_title_javaargs)
                        .setMessage(R.string.mcl_setting_subtitle_javaargs)
                        .setEditText(DEFAULT_PREF.getString("javaArgs", ""))
                        .setConfirmListener(editBox -> {
                            DEFAULT_PREF.edit().putString("javaArgs", editBox.getText().toString()).apply();
                            return true;
                        }).buildDialog();
                return true;
            });
        }

        requirePreference("install_jre").setOnPreferenceClickListener(preference -> {
            openMultiRTDialog();
            return true;
        });

        updateMemoryInfo(requireContext(), allocationSeek);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void updateMemoryInfo(Context context, CustomSeekBarPreference seek) {
        long seekValue = (long) seek.getValue() * 1024 * 1024;
        long freeDeviceMemory = MemoryUtils.getFreeDeviceMemory(context);

        boolean isMemorySizeExceeded = seekValue > freeDeviceMemory;

        String summary = StringUtils.insertNewline(getString(R.string.zh_setting_java_memory_desc), getMemoryInfoText(context, freeDeviceMemory));
        if (isMemorySizeExceeded) summary = StringUtils.insertNewline(summary, getString(R.string.zh_setting_java_memory_exceeded));

        String finalSummary = summary;
        runOnUiThread(() -> seek.setSummary(finalSummary));
    }

    private String getMemoryInfoText(Context context) {
        return getMemoryInfoText(context, MemoryUtils.getFreeDeviceMemory(context));
    }

    private String getMemoryInfoText(Context context, long freeDeviceMemory) {
        return getString(
                R.string.zh_setting_java_memory_info,
                FileTools.formatFileSize(MemoryUtils.getUsedDeviceMemory(context)),
                FileTools.formatFileSize(MemoryUtils.getTotalDeviceMemory(context)),
                FileTools.formatFileSize(freeDeviceMemory));
    }

    private void openMultiRTDialog() {
        if (mDialogScreen == null) {
            mDialogScreen = new MultiRTConfigDialog();
            mDialogScreen.prepare(getContext(), mVmInstallLauncher);
        }
        mDialogScreen.show();
    }
}
