package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexboxLayout;
import com.movtery.pojavzh.ui.view.AnimButton;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.databinding.DialogKeyboardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyboardDialog extends FullScreenDialog {
    private final DialogKeyboardBinding binding = DialogKeyboardBinding.inflate(getLayoutInflater());
    private OnKeycodeSelectListener mOnKeycodeSelectListener;
    private boolean showSpecialButtons = true;
    private boolean isGamepadMapper = false;

    public KeyboardDialog(@NonNull Context context) {
        super(context);
        setContentView(binding.getRoot());
    }

    public KeyboardDialog(@NonNull Context context, boolean isGamepadMapper) {
        this(context);
        this.isGamepadMapper = isGamepadMapper;
    }

    public KeyboardDialog setShowSpecialButtons(boolean show) {
        showSpecialButtons = show;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }

        init(showSpecialButtons);
    }

    private void init(boolean showSpecialButtons) {
        binding.zhKeyboardClose.setOnClickListener(v -> this.dismiss());

        List<View> specialButtons = new ArrayList<>();

        if (isGamepadMapper) {
            specialButtons.add(getKey(getString(R.string.keycode_unspecified)));
            specialButtons.add(getKey(getString(R.string.keycode_mouse_right)));
            specialButtons.add(getKey(getString(R.string.keycode_mouse_middle)));
            specialButtons.add(getKey(getString(R.string.keycode_mouse_left)));
            specialButtons.add(getKey(getString(R.string.keycode_scroll_up)));
            specialButtons.add(getKey(getString(R.string.keycode_scroll_down)));
        } else {
            specialButtons.add(getKey(getString(R.string.keycode_special_keyboard)));
            specialButtons.add(getKey("GUI"));
            specialButtons.add(getKey(getString(R.string.keycode_special_pri)));
            specialButtons.add(getKey(getString(R.string.keycode_special_sec)));
            specialButtons.add(getKey(getString(R.string.keycode_special_mouse)));
            specialButtons.add(getKey(getString(R.string.keycode_special_mid)));
            specialButtons.add(getKey(getString(R.string.keycode_special_scrollup)));
            specialButtons.add(getKey(getString(R.string.keycode_special_scrolldown)));
            specialButtons.add(getKey(getString(R.string.keycode_special_menu)));
        }

        List<View> buttons = new ArrayList<>(List.of(
                binding.keyboardHome, binding.keyboardEsc,
                binding.keyboard0, binding.keyboard1, binding.keyboard2,
                binding.keyboard3, binding.keyboard4, binding.keyboard5,
                binding.keyboard6, binding.keyboard7, binding.keyboard8,
                binding.keyboard9, binding.keyboardUp, binding.keyboardDown,
                binding.keyboardLeft, binding.keyboardRight,
                binding.keyboardA, binding.keyboardB, binding.keyboardC,
                binding.keyboardD, binding.keyboardE, binding.keyboardF,
                binding.keyboardG, binding.keyboardH, binding.keyboardI,
                binding.keyboardJ, binding.keyboardK, binding.keyboardL,
                binding.keyboardM, binding.keyboardN, binding.keyboardO,
                binding.keyboardP, binding.keyboardQ, binding.keyboardR,
                binding.keyboardS, binding.keyboardT, binding.keyboardU,
                binding.keyboardV, binding.keyboardW, binding.keyboardX,
                binding.keyboardY, binding.keyboardZ, binding.keyboardComma,
                binding.keyboardPeriod, binding.keyboardLeftAlt,
                binding.keyboardRightAlt, binding.keyboardLeftShift,
                binding.keyboardRightShift, binding.keyboardTab,
                binding.keyboardSpace, binding.keyboardEnter,
                binding.keyboardBackspace, binding.keyboardGrave,
                binding.keyboardMinus, binding.keyboardEquals,
                binding.keyboardLeftBracket, binding.keyboardRightBracket,
                binding.keyboardBackslash, binding.keyboardSemicolon,
                binding.keyboardApostrophe, binding.keyboardSlash,
                binding.keyboardKpAdd, binding.keyboardPageUp,
                binding.keyboardPageDown, binding.keyboardLeftCtrl,
                binding.keyboardRightCtrl, binding.keyboardCapslock,
                binding.keyboardPause, binding.keyboardEnd,
                binding.keyboardInsert, binding.keyboardF1, binding.keyboardF2,
                binding.keyboardF3, binding.keyboardF4, binding.keyboardF5,
                binding.keyboardF6, binding.keyboardF7, binding.keyboardF8,
                binding.keyboardF9, binding.keyboardF10, binding.keyboardF11,
                binding.keyboardF12, binding.keyboardNumLock,
                binding.keyboardKp0, binding.keyboardKp1, binding.keyboardKp2,
                binding.keyboardKp3, binding.keyboardKp4, binding.keyboardKp5,
                binding.keyboardKp6, binding.keyboardKp7, binding.keyboardKp8,
                binding.keyboardKp9, binding.keyboardKpDivide,
                binding.keyboardKpMultiply, binding.keyboardKpSubract,
                binding.keyboardKpDecimal, binding.keyboardKpEnter));

        if (!isGamepadMapper) buttons.add(0, getKey(getString(R.string.keycode_unspecified)));

        if (showSpecialButtons) {
            //此处如果不是手柄映射模式，那么将反着加入
            int specialCount = isGamepadMapper ? 0 : specialButtons.size() - 1;
            for (View specialButton : specialButtons) {
                int finalSpecialCount = specialCount;
                specialButton.setOnClickListener(v -> onKeycodeSelect(finalSpecialCount));
                if (isGamepadMapper) specialCount += 1;
                else specialCount -= 1;
            }
        }

        int buttonCount = showSpecialButtons ? (specialButtons.size() - 1) : -1;
        if (isGamepadMapper) buttonCount++;

        for (View button : buttons) {
            buttonCount += 1;
            int finalButtonCount = buttonCount;
            button.setOnClickListener(v -> onKeycodeSelect(finalButtonCount));

            if (    //保证顺序正确
                    Objects.equals(button, binding.keyboard9) ||
                            Objects.equals(button, binding.keyboardSlash) ||
                            Objects.equals(button, binding.keyboardPageDown) ||
                            Objects.equals(button, binding.keyboardPause) ||
                            Objects.equals(button, binding.keyboardKpSubract) ||
                            Objects.equals(button, binding.keyboardKpDecimal) ||
                            Objects.equals(button, binding.keyboardKpEnter)
            ) buttonCount += 1;
        }

        if (!showSpecialButtons) {
            findViewById(R.id.special_key).setVisibility(View.GONE);
        }
    }

    private AnimButton getKey(String text) {
        AnimButton key = new AnimButton(getContext());
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        key.setLayoutParams(layoutParams);
        key.setText(text);
        binding.specialKey.addView(key);
        return key;
    }

    private String getString(int resId) {
        return getContext().getString(resId);
    }

    private void onKeycodeSelect(int index) {
        if (this.mOnKeycodeSelectListener != null) {
            this.mOnKeycodeSelectListener.onSelect(index);
            dismiss();
        }
    }

    public KeyboardDialog setOnKeycodeSelectListener(OnKeycodeSelectListener listener) {
        this.mOnKeycodeSelectListener = listener;
        return this;
    }

    public interface OnKeycodeSelectListener {
        void onSelect(int index);
    }
}
