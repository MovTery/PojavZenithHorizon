package com.movtery.pojavzh.ui.fragment.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.*;

import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

// Experimental Settings for Mesa renderer
public class PreferenceRendererConfigFragment extends LauncherPreference {
    public static final String TAG = "PreferenceRendererConfigFragment";

    private EditText mMesaGLVersion;
    private EditText mMesaGLSLVersion;

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_renderexp);
        computeVisibility();

        findPreference("SetGLVersion").setOnPreferenceClickListener((preference) -> {
            showSetGLVersionDialog();
            return true;
        });

        final ListPreference CMesaLibP = requirePreference("CMesaLibrarys", ListPreference.class);
        final ListPreference CDriverModleP = requirePreference("CDriverModles", ListPreference.class);
        
        setListPreference(CMesaLibP, "CMesaLibrarys");
        setListPreference(CDriverModleP, "CDriverModles");
        
        CMesaLibP.setOnPreferenceChangeListener((pre, obj) -> {
                Tools.MESA_LIBS = (String)obj;
                setListPreference(CDriverModleP, "CDriverModles");
                CDriverModleP.setValueIndex(0);
                return true;
        });
        
        CDriverModleP.setOnPreferenceChangeListener((pre, obj) -> {
                Tools.DRIVER_MODLE = (String)obj;
                return true;
        });

        // Custom GL/GLSL
        final PreferenceCategory customMesaVersionPref = findPreference("customMesaVersionPref");
        for (int i = 0; i < customMesaVersionPref.getPreferenceCount(); i++) {
            final Preference custommvs = customMesaVersionPref.getPreference(i);
            custommvs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference custommvs) {
                    for (int i = 0; i < customMesaVersionPref.getPreferenceCount(); i++) {
                        ((SwitchPreference) customMesaVersionPref.getPreference(i)).setChecked(false);
                    }
                    ((SwitchPreference) custommvs).setChecked(true);
                    if (custommvs.getKey().equals("ebCustom")) {
                        AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.preference_rendererexp_alertdialog_warning)
                            .setMessage(R.string.preference_exp_alertdialog_glmessage)
                            .setPositiveButton(R.string.preference_rendererexp_alertdialog_done, null)
                            .setNegativeButton(R.string.preference_rendererexp_alertdialog_cancel, (dia, which) ->{
                                ((SwitchPreference) custommvs).setChecked(false);
                            })
                            .create();
                        dialog.show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences p, String s) {
        super.onSharedPreferenceChanged(p, s);
        computeVisibility();

        // Warning pops up when using experimental settings
        if (s.equals("ExperimentalSetup")) {
            Preference experimentalSetUpPreference = requirePreference("ExperimentalSetup");
            boolean isExperimentalSetUpEnabled = p.getBoolean("ExperimentalSetup", false);

            if (isExperimentalSetUpEnabled) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.preference_rendererexp_alertdialog_warning)
                    .setMessage(R.string.preference_rendererexp_alertdialog_message)
                    .setPositiveButton(R.string.preference_rendererexp_alertdialog_done, (dia, which) -> {
                        showPopupDialogWithRandomCharacter();
                    })
                    .setNegativeButton(R.string.preference_rendererexp_alertdialog_cancel, (dia, which) -> {
                        ((SwitchPreference) experimentalSetUpPreference).setChecked(false);
                        SharedPreferences.Editor editor = p.edit();
                        editor.putBoolean("ExperimentalSetup", false);
                        editor.apply();
                    })
                    .create();
                dialog.show();
            }
        }
    }

    private void computeVisibility(){
        requirePreference("ExpFrameBuffer").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("CDriverModles").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("CMesaLibrarys").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("ebSystem").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("ebSpecific").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("ebCustom").setVisible(LauncherPreferences.PREF_EXP_SETUP);
        requirePreference("SetGLVersion").setVisible(LauncherPreferences.PREF_EXP_ENABLE_CUSTOM && LauncherPreferences.PREF_EXP_SETUP);
    }

    private void setListPreference(ListPreference listPreference, String preferenceKey) {
        Tools.IListAndArry array = null;
        String value = listPreference.getValue();
        if (preferenceKey.equals("CMesaLibrarys")) {
            array = Tools.getCompatibleCMesaLib(getContext());
            Tools.MESA_LIBS = value;
        } else if (preferenceKey.equals("CDriverModles")) {
            array = Tools.getCompatibleCDriverModle(getContext());
            Tools.DRIVER_MODLE = value;
        }
        listPreference.setEntries(array.getArray());
        listPreference.setEntryValues(array.getList().toArray(new String[0]));
    }

    // Extra dialog
    private void showPopupDialogWithRandomCharacter() {
        //Generate any of there characters
        String[] characters = {
        getString(R.string.alertdialog_tipa),
        getString(R.string.alertdialog_tipb),
        getString(R.string.alertdialog_tipc)
        };
        Random random = new Random();
        int index = random.nextInt(characters.length);
        String randomCharacter = characters[index];

        // Create AlertDialog. Builder and set dialog content
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setTitle("Tip:")
            .setMessage(randomCharacter)
            .setPositiveButton(R.string.preference_alertdialog_know, null)
            .create();
        dialog.show();
    }

    // Custom Mesa GL/GLSL Version
    private void showSetGLVersionDialog() {
        // Specify a layout films
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_mesa_version, null);

        // Define symbol content
        mMesaGLVersion = view.findViewById(R.id.mesa_gl_version);
        mMesaGLSLVersion = view.findViewById(R.id.mesa_glsl_version);

        // Set text for GL/GLSL values
        mMesaGLVersion.setText(LauncherPreferences.PREF_MESA_GL_VERSION);
        mMesaGLSLVersion.setText(LauncherPreferences.PREF_MESA_GLSL_VERSION);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
            // Dialog content
            .setTitle(R.string.preference_rendererexp_custom_glversion_title)
            .setView(view)
            .setPositiveButton(R.string.alertdialog_done, (dia, i) -> {
                // Gets the GL and GLSL version of the user input
                String glVersion = mMesaGLVersion.getText().toString();
                String glslVersion = mMesaGLSLVersion.getText().toString();

                // Verify that the GL version is within the allowed range
                if (!isValidVersion(glVersion, "2.8", "4.6") && !isValidVersion(glslVersion, "280", "460")) {
                    showSetGLVersionDialog();
                    mMesaGLVersion.setError(getString(R.string.customglglsl_alertdialog_error_gl));
                    mMesaGLVersion.requestFocus();
                    mMesaGLSLVersion.setError(getString(R.string.customglglsl_alertdialog_error_glsl));
                    mMesaGLSLVersion.requestFocus();
                    return;
                } else if (!isValidVersion(glVersion, "2.8", "4.6")) {
                    showSetGLVersionDialog();
                    mMesaGLVersion.setError(getString(R.string.customglglsl_alertdialog_error_gl));
                    mMesaGLVersion.requestFocus();
                    return;
                } else if (!isValidVersion(glslVersion, "280", "460")) {
                    showSetGLVersionDialog();
                    mMesaGLSLVersion.setError(getString(R.string.customglglsl_alertdialog_error_glsl));
                    mMesaGLSLVersion.requestFocus();
                    return;
                }

                // Update preferences
                LauncherPreferences.PREF_MESA_GL_VERSION = glVersion;
                LauncherPreferences.PREF_MESA_GLSL_VERSION = glslVersion;

                // Modify the value of GL/GLSL according to the text content
                LauncherPreferences.DEFAULT_PREF.edit()
                    .putString("mesaGLVersion", LauncherPreferences.PREF_MESA_GL_VERSION)
                    .putString("mesaGLSLVersion", LauncherPreferences.PREF_MESA_GLSL_VERSION)
                    .apply();
            })
            .setNegativeButton(R.string.alertdialog_cancel, null)
            .create();
        dialog.show();
    }

    // Check whether the GL/GLSL version is within the acceptable range
    private boolean isValidVersion(String version, String minVersion, String maxVersion) {
        try {
            float versionNumber = Float.parseFloat(version);
            float minVersionNumber = Float.parseFloat(minVersion);
            float maxVersionNumber = Float.parseFloat(maxVersion);

        return versionNumber >= minVersionNumber && versionNumber <= maxVersionNumber;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
