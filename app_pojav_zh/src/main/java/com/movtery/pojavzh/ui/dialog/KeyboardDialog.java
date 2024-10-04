package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

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
            binding.keyboardUnknown.setText(R.string.zh_keycode_unspecified);
            binding.keyboardSpecialPri.setText(R.string.zh_keycode_mouse_right);
            binding.keyboardSpecialSec.setText(R.string.zh_keycode_mouse_middle);
            binding.keyboardSpecialMenu.setText(R.string.zh_keycode_mouse_left);
            binding.keyboardSpecialScrolldown.setText(R.string.zh_keycode_scroll_up);
            binding.keyboardSpecialScrollup.setText(R.string.zh_keycode_scroll_down);

            specialButtons.addAll(List.of(
                    binding.keyboardSpecialScrollup,
                    binding.keyboardSpecialScrolldown,
                    binding.keyboardSpecialMenu,
                    binding.keyboardSpecialSec,
                    binding.keyboardSpecialPri,
                    binding.keyboardUnknown));

            binding.keyboardSpecialKeyboard.setVisibility(View.GONE);
            binding.keyboardSpecialGui.setVisibility(View.GONE);
            binding.keyboardSpecialMouse.setVisibility(View.GONE);
            binding.keyboardSpecialMid.setVisibility(View.GONE);
        } else {
            specialButtons.addAll(List.of(
                    binding.keyboardSpecialKeyboard,
                    binding.keyboardSpecialGui,
                    binding.keyboardSpecialPri,
                    binding.keyboardSpecialSec,
                    binding.keyboardSpecialMouse,
                    binding.keyboardSpecialMid,
                    binding.keyboardSpecialScrollup,
                    binding.keyboardSpecialScrolldown,
                    binding.keyboardSpecialMenu));
        }

        List<View> buttons = new ArrayList<>(List.of(
                binding.keyboardUnknown, binding.keyboardHome, binding.keyboardEsc,
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

        if (showSpecialButtons) {
            //这里的按键比较特殊，它的顺序就是反着的
            int specialCount = specialButtons.size();
            for (View specialButton : specialButtons) {
                specialCount -= 1;
                int finalSpecialCount = specialCount;
                specialButton.setOnClickListener(v -> onKeycodeSelect(finalSpecialCount));
            }
        }

        int buttonCount = showSpecialButtons ? (specialButtons.size() - 1) : -1;
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
            findViewById(R.id.layout_0).setVisibility(View.GONE);
        }
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
