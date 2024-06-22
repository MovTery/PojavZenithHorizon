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
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.platform.MemoryUtils;

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

        CustomSeekBarPreference seek7 = requirePreference("allocation",
                CustomSeekBarPreference.class);

        int maxRAM;
        int deviceRam = getTotalDeviceMemory(seek7.getContext());

        if (is32BitsDevice() || deviceRam < 2048) maxRAM = Math.min(1024, deviceRam);
        else maxRAM = deviceRam - (deviceRam < 3064 ? 800 : 1024); //To have a minimum for the device to breathe

        seek7.setMin(256);
        seek7.setMax(maxRAM);
        seek7.setValue(ramAllocation);
        seek7.setSuffix(" MB");

        updateMemoryInfo(requireContext(), seek7);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updateMemoryInfo(requireContext(), seek7));
            }
        }, 0, 1000);

        Preference editJVMArgs = findPreference("javaArgs");
        if (editJVMArgs != null) {
            editJVMArgs.setOnPreferenceClickListener(preference -> {
                EditTextDialog editTextDialog = new EditTextDialog(requireContext(), getString(R.string.mcl_setting_title_javaargs), null, DEFAULT_PREF.getString("javaArgs", ""), null);
                editTextDialog.setConfirm(view -> {
                    DEFAULT_PREF.edit().putString("javaArgs", editTextDialog.getEditBox().getText().toString()).apply();
                    editTextDialog.dismiss();
                });
                editTextDialog.show();
                return true;
            });
        }

        requirePreference("install_jre").setOnPreferenceClickListener(preference -> {
            openMultiRTDialog();
            return true;
        });
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void updateMemoryInfo(Context context, CustomSeekBarPreference seek) {
        String summary = getString(
                R.string.zh_setting_java_memory_info,
                ZHTools.formatFileSize(MemoryUtils.getUsedDeviceMemory(context)),
                ZHTools.formatFileSize(MemoryUtils.getTotalDeviceMemory(context)),
                ZHTools.formatFileSize(MemoryUtils.getFreeDeviceMemory(context)));
        runOnUiThread(() -> seek.setSummary(summary));
    }

    private void openMultiRTDialog() {
        if (mDialogScreen == null) {
            mDialogScreen = new MultiRTConfigDialog();
            mDialogScreen.prepare(getContext(), mVmInstallLauncher);
        }
        mDialogScreen.show();
    }
}
