package com.movtery.pojavzh.ui.fragment.preference;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.movtery.pojavzh.feature.renderer.RendererManager;
import com.movtery.pojavzh.ui.dialog.EditMesaVersionDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.ListAndArray;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

// Experimental Settings for Mesa renderer
public class PreferenceExperimentalFragment extends LauncherPreferenceFragment {
    public static final String TAG = "PreferenceExperimentalFragment";

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_experimental);
        computeVisibility();

        final ListPreference CMesaLib = requirePreference("CMesaLibrary", ListPreference.class);
        final ListPreference CDriverModel = requirePreference("CDriverModels", ListPreference.class);

        setListPreference(CMesaLib, "CMesaLibrary");
        setListPreference(CDriverModel, "CDriverModels");

        CMesaLib.setOnPreferenceChangeListener((pre, obj) -> {
            RendererManager.MESA_LIBS = (String) obj;
            setListPreference(CDriverModel, "CDriverModels");
            CDriverModel.setValueIndex(0);
            return true;
        });

        CDriverModel.setOnPreferenceChangeListener((pre, obj) -> {
            RendererManager.DRIVER_MODEL = (String) obj;
            return true;
        });

        // Custom GL/GLSL
        final PreferenceCategory customMesaVersionPref = requirePreference("customMesaVersionPref", PreferenceCategory.class);
        SwitchPreference setSystemVersion = requirePreference("ebSystem", SwitchPreference.class);
        setSystemVersion.setOnPreferenceChangeListener((p, v) -> {
            closeOtherCustomMesaPref(customMesaVersionPref);
            LauncherPreferences.PREF_EXP_ENABLE_SYSTEM = (boolean) v;
            return true;
        });

        SwitchPreference setSpecificVersion = requirePreference("ebSpecific", SwitchPreference.class);
        setSpecificVersion.setOnPreferenceChangeListener((p, v) -> {
            closeOtherCustomMesaPref(customMesaVersionPref);
            LauncherPreferences.PREF_EXP_ENABLE_SPECIFIC = (boolean) v;
            return true;
        });

        SwitchPreference setGLVersion = requirePreference("SetGLVersion", SwitchPreference.class);
        setGLVersion.setOnPreferenceChangeListener((preference, value) -> {
            boolean value1 = (boolean) value;
            if (value1) {
                closeOtherCustomMesaPref(customMesaVersionPref);
            }
            LauncherPreferences.PREF_EXP_ENABLE_CUSTOM = value1;
            LauncherPreferences.DEFAULT_PREF.edit().putBoolean("ebCustom", value1).apply();
            return value1;
        });
        setGLVersion.setOnPreferenceClickListener(preference -> {
            new EditMesaVersionDialog(requireContext()).show();
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);

        // Warning pops up when using experimental settings
        if (s.equals("ExperimentalSetup")) {
            computeVisibility();

            SwitchPreference experimentalSetUpPreference = requirePreference("ExperimentalSetup", SwitchPreference.class);
            boolean isExperimentalSetUpEnabled = p.getBoolean("ExperimentalSetup", false);

            if (isExperimentalSetUpEnabled) {
                new TipDialog.Builder(requireContext())
                        .setTitle(R.string.zh_warning)
                        .setMessage(R.string.preference_rendererexp_alertdialog_message)
                        .setCancelClickListener(() -> {
                            experimentalSetUpPreference.setChecked(false);
                            SharedPreferences.Editor editor = p.edit();
                            editor.putBoolean("ExperimentalSetup", false);
                            editor.apply();
                        }).setCancelable(false).buildDialog();
            }
        }
    }

    private void computeVisibility() {
        requirePreference("ExpFrameBuffer").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("MesaRendererChoose").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("customMesaVersionPref").setVisible(LauncherPreferences.PREF_EXP_SETUP);
    }

    private void closeOtherCustomMesaPref(PreferenceCategory customMesaVersionPref) {
        for (int i1 = 0; i1 < customMesaVersionPref.getPreferenceCount(); i1++) {
            Preference preference2 = customMesaVersionPref.getPreference(i1);
            if (preference2 instanceof SwitchPreference) {
                ((SwitchPreference) preference2).setChecked(false);
            }
        }
    }

    private void setListPreference(ListPreference listPreference, String preferenceKey) {
        ListAndArray array;
        String value = listPreference.getValue();
        if (preferenceKey.equals("CMesaLibrary")) {
            array = RendererManager.getCompatibleCMesaLib(requireContext());
            RendererManager.MESA_LIBS = value;
        } else {
            array = RendererManager.getCompatibleCDriverModel(requireContext());
            RendererManager.DRIVER_MODEL = value;
        }
        listPreference.setEntries(array.getArray());
        listPreference.setEntryValues(array.getList().toArray(new String[0]));
    }
}
