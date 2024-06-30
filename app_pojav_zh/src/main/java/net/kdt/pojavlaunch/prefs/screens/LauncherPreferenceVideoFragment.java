package net.kdt.pojavlaunch.prefs.screens;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_NOTCH_SIZE;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.movtery.pojavzh.feature.renderer.RendererManager;
import com.movtery.pojavzh.utils.ListAndArray;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.CustomSeekBarPreference;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * Fragment for any settings video related
 */
public class LauncherPreferenceVideoFragment extends LauncherPreferenceFragment {
    public static final String TAG = "LauncherPreferenceVideoFragment";
    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_video);

        int scaleFactor = LauncherPreferences.PREF_SCALE_FACTOR;

        //Disable notch checking behavior on android 8.1 and below.
        requirePreference("ignoreNotch").setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && PREF_NOTCH_SIZE > 0);

        CustomSeekBarPreference seek5 = requirePreference("resolutionRatio",
                CustomSeekBarPreference.class);
        seek5.setRange(25, 200);
        seek5.setValue(scaleFactor);
        seek5.setSuffix(" %");

        // #724 bug fix
        if (seek5.getValue() < 25) {
            seek5.setValue(100);
        }

        // Sustained performance is only available since Nougat
        SwitchPreference sustainedPerfSwitch = requirePreference("sustainedPerformance",
                SwitchPreference.class);
        sustainedPerfSwitch.setVisible(true);

        final ListPreference rendererListPreference = requirePreference("renderer", ListPreference.class);
        final ListPreference expRendererListPreference = requirePreference("renderer_exp", ListPreference.class);
        setListPreference(rendererListPreference, "renderer");
        setListPreference(expRendererListPreference, "renderer_exp");

        computeVisibility();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        computeVisibility();
    }

    private void computeVisibility(){
        requirePreference("force_vsync", SwitchPreferenceCompat.class)
                .setVisible(LauncherPreferences.PREF_USE_ALTERNATE_SURFACE);
    }

    private void setListPreference(ListPreference listPreference, String key) {
        boolean prefExpSetup = key.equals("renderer_exp") == LauncherPreferences.PREF_EXP_SETUP;
        listPreference.setVisible(prefExpSetup);
        if (!prefExpSetup) return;

        ListAndArray array = RendererManager.getCompatibleRenderers(requireContext());
        Tools.LOCAL_RENDERER = listPreference.getValue();
        listPreference.setEntries(array.getArray());
        listPreference.setEntryValues(array.getList().toArray(new String[0]));

        listPreference.setOnPreferenceChangeListener((pre, obj) -> updateRendererPref((String) obj));
    }

    private boolean updateRendererPref(String name) {
        Tools.LOCAL_RENDERER = name;
        return true;
    }
}
