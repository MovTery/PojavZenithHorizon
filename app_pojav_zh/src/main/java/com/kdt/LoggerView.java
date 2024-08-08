package com.kdt;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.Logger;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

/**
 * A class able to display logs to the user.
 * It has support for the Logger class
 */
public class LoggerView extends ConstraintLayout {
    private Logger.eventLogListener mLogListener;
    private ToggleButton mLogToggle;
    private DefocusableScrollView mScrollView;
    private TextView mLogTextView;


    public LoggerView(@NonNull Context context) {
        this(context, null);
    }

    public LoggerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        // Triggers the log view shown state by default when viewing it
        mLogToggle.setChecked(visibility == VISIBLE);
    }

    public void setVisibilityWithAnim(boolean visibility) {
        ViewAnimUtils.setViewAnim(this,
                visibility ? Techniques.BounceInUp : Techniques.SlideOutDown,
                (long) (LauncherPreferences.PREF_ANIMATION_SPEED * 0.7),
                animator -> setVisibility(VISIBLE),
                animator -> setVisibility(visibility ? VISIBLE : GONE));
    }

    /**
     * Inflate the layout, and add component behaviors
     */
    private void init(){
        inflate(getContext(), R.layout.view_logger, this);
        mLogTextView = findViewById(R.id.content_log_view);
        mLogTextView.setTypeface(Typeface.MONOSPACE);
        //TODO clamp the max text so it doesn't go oob
        mLogTextView.setMaxLines(Integer.MAX_VALUE);
        mLogTextView.setEllipsize(null);
        mLogTextView.setVisibility(GONE);

        // Toggle log visibility
        mLogToggle = findViewById(R.id.content_log_toggle_log);
        mLogToggle.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    mLogTextView.setVisibility(isChecked ? VISIBLE : GONE);
                    if(isChecked) {
                        Logger.setLogListener(mLogListener);
                    }else{
                        mLogTextView.setText("");
                        Logger.setLogListener(null); // Makes the JNI code be able to skip expensive logger callbacks
                        // NOTE: was tested by rapidly smashing the log on/off button, no sync issues found :)
                    }
                });
        mLogToggle.setChecked(false);

        // Remove the loggerView from the user View
        ImageButton cancelButton = findViewById(R.id.log_view_cancel);
        cancelButton.setOnClickListener(view -> setVisibilityWithAnim(false));

        // Set the scroll view
        mScrollView = findViewById(R.id.content_log_scroll);
        mScrollView.setKeepFocusing(true);

        //Set up the autoscroll switch
        ToggleButton autoscrollToggle = findViewById(R.id.content_log_toggle_autoscroll);
        autoscrollToggle.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    if(isChecked) mScrollView.fullScroll(View.FOCUS_DOWN);
                    mScrollView.setKeepFocusing(isChecked);
                }
        );
        autoscrollToggle.setChecked(true);

        // Listen to logs
        mLogListener = text -> {
            if(mLogTextView.getVisibility() != VISIBLE) return;
            post(() -> {
                mLogTextView.append(text + '\n');
                if(mScrollView.isKeepFocusing()) mScrollView.fullScroll(View.FOCUS_DOWN);
            });

        };
    }
}
