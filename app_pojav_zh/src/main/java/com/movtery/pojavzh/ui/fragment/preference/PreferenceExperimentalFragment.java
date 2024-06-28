package com.movtery.pojavzh.ui.fragment.preference;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.movtery.pojavzh.ui.dialog.EditMesaVersionDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;

// Experimental Settings for Mesa renderer
public class PreferenceExperimentalFragment extends LauncherPreferenceFragment {
    public static final String TAG = "PreferenceRendererConfigFragment";

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_experimental);
        computeVisibility();

        final ListPreference CMesaLib = requirePreference("CMesaLibrary", ListPreference.class);
        final ListPreference CDriverModel = requirePreference("CDriverModels", ListPreference.class);

        setListPreference(CMesaLib, "CMesaLibrary");
        setListPreference(CDriverModel, "CDriverModels");

        CMesaLib.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.MESA_LIBS = (String) obj;
            setListPreference(CDriverModel, "CDriverModels");
            CDriverModel.setValueIndex(0);
            return true;
        });

        CDriverModel.setOnPreferenceChangeListener((pre, obj) -> {
            Tools.DRIVER_MODLE = (String) obj;
            return true;
        });

        // Custom GL/GLSL
        SwitchPreference switchPreference = requirePreference("ebSystem", SwitchPreference.class);
        if (switchPreference.isChecked() && DEFAULT_PREF.getBoolean("ebCustom", false)) {
            switchPreference.setChecked(false);
        }

        final PreferenceCategory customMesaVersionPref = requirePreference("customMesaVersionPref", PreferenceCategory.class);
        for (int i = 0; i < customMesaVersionPref.getPreferenceCount(); i++) {
            Preference preference = customMesaVersionPref.getPreference(i);
            if (preference instanceof SwitchPreference) {
                preference.setOnPreferenceClickListener(preference1 -> {
                    closeOtherCustomMesaPref(customMesaVersionPref);
                    ((SwitchPreference) preference).setChecked(true);
                    LauncherPreferences.PREF_EXP_ENABLE_CUSTOM = true;
                    LauncherPreferences.DEFAULT_PREF.edit().putBoolean("ebCustom", false).apply();
                    return true;
                });
            }
        }

        Preference setGLVersion = findPreference("SetGLVersion");
        if (setGLVersion != null) {
            setGLVersion.setOnPreferenceClickListener((preference) -> {
                new EditMesaVersionDialog(requireContext(), () -> closeOtherCustomMesaPref(customMesaVersionPref)).show();
                return true;
            });
        }
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
        Tools.IListAndArry array = null;
        String value = listPreference.getValue();
        if (preferenceKey.equals("CMesaLibrary")) {
            array = Tools.getCompatibleCMesaLib(getContext());
            Tools.MESA_LIBS = value;
        } else if (preferenceKey.equals("CDriverModels")) {
            array = Tools.getCompatibleCDriverModle(requireContext());
            Tools.DRIVER_MODLE = value;
        }
        listPreference.setEntries(array.getArray());
        listPreference.setEntryValues(array.getList().toArray(new String[0]));
    }
}
