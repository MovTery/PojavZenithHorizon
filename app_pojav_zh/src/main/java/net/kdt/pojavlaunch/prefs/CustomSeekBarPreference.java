package net.kdt.pojavlaunch.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;

import net.kdt.pojavlaunch.R;

public class CustomSeekBarPreference extends SeekBarPreference {

    /** The suffix displayed */
    private String mSuffix = "";
    /** Custom minimum value to provide the same behavior as the usual setMin */
    private int mMin;
    /** The textview associated by default to the preference */
    private TextView mTextView;
    private boolean isUserSeeking = false;
    private OnPreferenceClickDialog onPreferenceClickDialog;
    private OnProgressChanged onProgressChanged;


    @SuppressLint({"PrivateResource", "StringFormatInvalid"})
    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        try (TypedArray typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.SeekBarPreference,
                defStyleAttr, defStyleRes)
        ) {
            mMin = typedArray.getInt(R.styleable.SeekBarPreference_min, 0);
        }

        super.setOnPreferenceClickListener(preference -> {
            boolean listenerExist = onPreferenceClickDialog != null;
            EditTextDialog.Builder builder = new EditTextDialog.Builder(context)
                    .setEditText(String.valueOf(getValue()))
                    .setInputType(InputType.TYPE_CLASS_NUMBER);

            CharSequence seekTitle = getTitle();
            CharSequence seekSummary = getSummary();
            if (listenerExist) {
                String title = onPreferenceClickDialog.getTitle();
                String message = onPreferenceClickDialog.getMessage();

                if (title != null) builder.setTitle(title);
                else if (seekTitle != null) builder.setTitle(seekTitle.toString());
                if (message != null) builder.setMessage(message);
                else if (seekSummary != null) builder.setMessage(seekSummary.toString());
            } else {
                if (seekTitle != null) builder.setTitle(seekTitle.toString());
                if (seekSummary != null) builder.setMessage(seekSummary.toString());
            }

            builder.setConfirmListener(editBox -> {
                String string = editBox.getText().toString();
                if (string.isEmpty()) {
                    editBox.setError(context.getString(R.string.global_error_field_empty));
                    return false;
                }

                int value;
                try {
                    value = Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    Logging.e("Custom Seek Bar", e.toString());

                    editBox.setError(context.getString(R.string.zh_input_invalid));
                    return false;
                }

                if (value < mMin) {
                    String minValue = String.format("%s%s", mMin, mSuffix);
                    editBox.setError(context.getString(R.string.zh_input_too_small, minValue));
                    return false;
                }
                if (value > getMax()) {
                    String maxValue = String.format("%s%s", getMax(), mSuffix);
                    editBox.setError(context.getString(R.string.zh_input_too_big, maxValue));
                    return false;
                }

                changeValue(value);

                return true;
            }).buildDialog();
            return true;
        });
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    @SuppressWarnings("unused") public CustomSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void setMin(int min) {
        //Note: since the max (setMax is a final function) is not taken into account properly, setting the min over the max may produce funky results
        super.setMin(min);
        if (min != mMin) mMin = min;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView titleTextView = (TextView) view.findViewById(android.R.id.title);
        titleTextView.setTextColor(getContext().getResources().getColor(R.color.primary_text, getContext().getTheme()));

        mTextView = (TextView) view.findViewById(R.id.seekbar_value);
        mTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += mMin;
                progress = progress / getSeekBarIncrement();
                progress = progress * getSeekBarIncrement();
                progress -= mMin;

                mTextView.setText(String.valueOf(progress + mMin));
                updateTextViewWithSuffix();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;

                int progress = seekBar.getProgress() + mMin;
                progress /= getSeekBarIncrement();
                progress *= getSeekBarIncrement();
                progress -= mMin;

                changeValue(progress + mMin);
                updateTextViewWithSuffix();
            }
        });

        updateTextViewWithSuffix();
    }

    /**
     * Set a suffix to be appended on the TextView associated to the value
     * @param suffix The suffix to append as a String
     */
    public void setSuffix(String suffix) {
        this.mSuffix = suffix;
    }

    /**
     * Convenience function to set both min and max at the same time.
     * @param min The minimum value
     * @param max The maximum value
     */
    public void setRange(int min, int max){
        setMin(min);
        setMax(max);
    }

    public void setOnDialogInitListener(OnPreferenceClickDialog listener) {
        this.onPreferenceClickDialog = listener;
    }

    public void setOnProgressChangedListener(OnProgressChanged listener) {
        this.onProgressChanged = listener;
    }

    public boolean isUserSeeking() {
        return isUserSeeking;
    }

    private void updateTextViewWithSuffix(){
        if(!mTextView.getText().toString().endsWith(mSuffix)){
            mTextView.setText(String.format("%s%s", mTextView.getText(), mSuffix));
        }
    }

    private void changeValue(int value) {
        setValue(value);
        if (onProgressChanged != null) onProgressChanged.onChanged(value);
    }

    public interface OnPreferenceClickDialog {
        String getTitle();
        String getMessage();
    }

    public interface OnProgressChanged {
        void onChanged(int value);
    }
}
