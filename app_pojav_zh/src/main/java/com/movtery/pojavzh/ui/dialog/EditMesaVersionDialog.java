package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

public class EditMesaVersionDialog extends FullScreenDialog implements DraggableDialog.DialogInitializationListener {
    public EditMesaVersionDialog(@NonNull Context context) {
        super(context);

        setCancelable(false);
        setContentView(R.layout.dialog_edit_mesa_version);
        init(context);
        DraggableDialog.initDialog(this);
    }

    private void init(Context context) {
        EditText mMesaGLVersion = findViewById(R.id.zh_edit_mesa_version_gl_version);
        EditText mMesaGLSLVersion = findViewById(R.id.zh_edit_mesa_version_glsl_version);
        Button mCancelButton = findViewById(R.id.zh_edit_mesa_version_cancel_button);
        Button mConfirmButton = findViewById(R.id.zh_edit_mesa_version_confirm_button);

        // Set text for GL/GLSL values
        mMesaGLVersion.setText(LauncherPreferences.PREF_MESA_GL_VERSION);
        mMesaGLSLVersion.setText(LauncherPreferences.PREF_MESA_GLSL_VERSION);

        mCancelButton.setOnClickListener(v -> dismiss());
        mConfirmButton.setOnClickListener(v -> {
            // Gets the GL and GLSL version of the user input
            String glVersion = mMesaGLVersion.getText().toString();
            String glslVersion = mMesaGLSLVersion.getText().toString();

            // Verify that the GL version is within the allowed range
            if (isInvalidVersion(glVersion, "2.8", "4.6") && isInvalidVersion(glslVersion, "280", "460")) {
                mMesaGLVersion.setError(context.getString(R.string.customglglsl_alertdialog_error_gl));
                mMesaGLVersion.requestFocus();
                mMesaGLSLVersion.setError(context.getString(R.string.customglglsl_alertdialog_error_glsl));
                mMesaGLSLVersion.requestFocus();
                return;
            } else if (isInvalidVersion(glVersion, "2.8", "4.6")) {
                mMesaGLVersion.setError(context.getString(R.string.customglglsl_alertdialog_error_gl));
                mMesaGLVersion.requestFocus();
                return;
            } else if (isInvalidVersion(glslVersion, "280", "460")) {
                mMesaGLSLVersion.setError(context.getString(R.string.customglglsl_alertdialog_error_glsl));
                mMesaGLSLVersion.requestFocus();
                return;
            }

            // Update preferences
            LauncherPreferences.PREF_MESA_GL_VERSION = glVersion;
            LauncherPreferences.PREF_MESA_GLSL_VERSION = glslVersion;
            LauncherPreferences.PREF_EXP_ENABLE_CUSTOM = true;

            // Modify the value of GL/GLSL according to the text content
            LauncherPreferences.DEFAULT_PREF.edit()
                    .putString("mesaGLVersion", glVersion)
                    .putString("mesaGLSLVersion", glslVersion)
                    .putBoolean("ebCustom", true)
                    .apply();

            dismiss();
        });
    }

    // Check whether the GL/GLSL version is within the acceptable range
    private boolean isInvalidVersion(String version, String minVersion, String maxVersion) {
        try {
            float versionNumber = Float.parseFloat(version);
            float minVersionNumber = Float.parseFloat(minVersion);
            float maxVersionNumber = Float.parseFloat(maxVersion);

            return !(versionNumber >= minVersionNumber) || !(versionNumber <= maxVersionNumber);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public Window onInit() {
        return getWindow();
    }
}
