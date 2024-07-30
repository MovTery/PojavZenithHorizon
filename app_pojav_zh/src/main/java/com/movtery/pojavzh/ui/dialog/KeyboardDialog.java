package com.movtery.pojavzh.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyboardDialog extends FullScreenDialog {
    private OnKeycodeSelectListener mOnKeycodeSelectListener;

    public KeyboardDialog(@NonNull Context context, boolean showSpecialButtons) {
        super(context);

        setContentView(R.layout.dialog_keyboard);
        init(showSpecialButtons);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }
    }

    private void init(boolean showSpecialButtons) {
        ImageView closeButton = findViewById(R.id.zh_keyboard_close);

        List<View> specialButtons = new ArrayList<>();
        List<View> buttons = new ArrayList<>();

        specialButtons.add(findViewById(R.id.keyboard_special_keyboard));
        specialButtons.add(findViewById(R.id.keyboard_special_gui));
        specialButtons.add(findViewById(R.id.keyboard_special_pri));
        specialButtons.add(findViewById(R.id.keyboard_special_sec));
        specialButtons.add(findViewById(R.id.keyboard_special_mouse));

        specialButtons.add(findViewById(R.id.keyboard_special_mid));
        specialButtons.add(findViewById(R.id.keyboard_special_scrollup));
        specialButtons.add(findViewById(R.id.keyboard_special_scrolldown));
        specialButtons.add(findViewById(R.id.keyboard_special_menu));

        buttons.add(findViewById(R.id.keyboard_unknown));

        buttons.add(findViewById(R.id.keyboard_home));
        buttons.add(findViewById(R.id.keyboard_esc));

        buttons.add(findViewById(R.id.keyboard_0));
        buttons.add(findViewById(R.id.keyboard_1));
        buttons.add(findViewById(R.id.keyboard_2));
        buttons.add(findViewById(R.id.keyboard_3));
        buttons.add(findViewById(R.id.keyboard_4));
        buttons.add(findViewById(R.id.keyboard_5));
        buttons.add(findViewById(R.id.keyboard_6));
        buttons.add(findViewById(R.id.keyboard_7));
        buttons.add(findViewById(R.id.keyboard_8));
        buttons.add(findViewById(R.id.keyboard_9));

        buttons.add(findViewById(R.id.keyboard_up));
        buttons.add(findViewById(R.id.keyboard_down));
        buttons.add(findViewById(R.id.keyboard_left));
        buttons.add(findViewById(R.id.keyboard_right));

        buttons.add(findViewById(R.id.keyboard_a));
        buttons.add(findViewById(R.id.keyboard_b));
        buttons.add(findViewById(R.id.keyboard_c));
        buttons.add(findViewById(R.id.keyboard_d));
        buttons.add(findViewById(R.id.keyboard_e));
        buttons.add(findViewById(R.id.keyboard_f));
        buttons.add(findViewById(R.id.keyboard_g));
        buttons.add(findViewById(R.id.keyboard_h));
        buttons.add(findViewById(R.id.keyboard_i));
        buttons.add(findViewById(R.id.keyboard_j));
        buttons.add(findViewById(R.id.keyboard_k));
        buttons.add(findViewById(R.id.keyboard_l));
        buttons.add(findViewById(R.id.keyboard_m));
        buttons.add(findViewById(R.id.keyboard_n));
        buttons.add(findViewById(R.id.keyboard_o));
        buttons.add(findViewById(R.id.keyboard_p));
        buttons.add(findViewById(R.id.keyboard_q));
        buttons.add(findViewById(R.id.keyboard_r));
        buttons.add(findViewById(R.id.keyboard_s));
        buttons.add(findViewById(R.id.keyboard_t));
        buttons.add(findViewById(R.id.keyboard_u));
        buttons.add(findViewById(R.id.keyboard_v));
        buttons.add(findViewById(R.id.keyboard_w));
        buttons.add(findViewById(R.id.keyboard_x));
        buttons.add(findViewById(R.id.keyboard_y));
        buttons.add(findViewById(R.id.keyboard_z));

        buttons.add(findViewById(R.id.keyboard_comma));
        buttons.add(findViewById(R.id.keyboard_period));

        buttons.add(findViewById(R.id.keyboard_left_alt));
        buttons.add(findViewById(R.id.keyboard_right_alt));

        buttons.add(findViewById(R.id.keyboard_left_shift));
        buttons.add(findViewById(R.id.keyboard_right_shift));

        buttons.add(findViewById(R.id.keyboard_tab));
        buttons.add(findViewById(R.id.keyboard_space));
        buttons.add(findViewById(R.id.keyboard_enter));
        buttons.add(findViewById(R.id.keyboard_backspace));
        buttons.add(findViewById(R.id.keyboard_grave));
        buttons.add(findViewById(R.id.keyboard_minus));
        buttons.add(findViewById(R.id.keyboard_equals));
        buttons.add(findViewById(R.id.keyboard_left_bracket));
        buttons.add(findViewById(R.id.keyboard_right_bracket));
        buttons.add(findViewById(R.id.keyboard_backslash));
        buttons.add(findViewById(R.id.keyboard_semicolon));
        buttons.add(findViewById(R.id.keyboard_apostrophe));
        buttons.add(findViewById(R.id.keyboard_slash));

        buttons.add(findViewById(R.id.keyboard_kp_add));

        buttons.add(findViewById(R.id.keyboard_page_up));
        buttons.add(findViewById(R.id.keyboard_page_down));

        buttons.add(findViewById(R.id.keyboard_left_ctrl));
        buttons.add(findViewById(R.id.keyboard_right_ctrl));

        buttons.add(findViewById(R.id.keyboard_capslock));
        buttons.add(findViewById(R.id.keyboard_pause));
        buttons.add(findViewById(R.id.keyboard_end));
        buttons.add(findViewById(R.id.keyboard_insert));

        buttons.add(findViewById(R.id.keyboard_f1));
        buttons.add(findViewById(R.id.keyboard_f2));
        buttons.add(findViewById(R.id.keyboard_f3));
        buttons.add(findViewById(R.id.keyboard_f4));
        buttons.add(findViewById(R.id.keyboard_f5));
        buttons.add(findViewById(R.id.keyboard_f6));
        buttons.add(findViewById(R.id.keyboard_f7));
        buttons.add(findViewById(R.id.keyboard_f8));
        buttons.add(findViewById(R.id.keyboard_f9));
        buttons.add(findViewById(R.id.keyboard_f10));
        buttons.add(findViewById(R.id.keyboard_f11));
        buttons.add(findViewById(R.id.keyboard_f12));

        buttons.add(findViewById(R.id.keyboard_num_lock));
        buttons.add(findViewById(R.id.keyboard_kp_0));
        buttons.add(findViewById(R.id.keyboard_kp_1));
        buttons.add(findViewById(R.id.keyboard_kp_2));
        buttons.add(findViewById(R.id.keyboard_kp_3));
        buttons.add(findViewById(R.id.keyboard_kp_4));
        buttons.add(findViewById(R.id.keyboard_kp_5));
        buttons.add(findViewById(R.id.keyboard_kp_6));
        buttons.add(findViewById(R.id.keyboard_kp_7));
        buttons.add(findViewById(R.id.keyboard_kp_8));
        buttons.add(findViewById(R.id.keyboard_kp_9));
        buttons.add(findViewById(R.id.keyboard_kp_divide));
        buttons.add(findViewById(R.id.keyboard_kp_multiply));
        buttons.add(findViewById(R.id.keyboard_kp_subract));
        buttons.add(findViewById(R.id.keyboard_kp_decimal));
        buttons.add(findViewById(R.id.keyboard_kp_enter));

        closeButton.setOnClickListener(v -> this.dismiss());

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
                    Objects.equals(button, findViewById(R.id.keyboard_9)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_slash)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_page_down)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_pause)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_kp_subract)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_kp_decimal)) ||
                            Objects.equals(button, findViewById(R.id.keyboard_kp_enter))
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

    public void setOnKeycodeSelectListener(OnKeycodeSelectListener listener) {
        this.mOnKeycodeSelectListener = listener;
    }

    public interface OnKeycodeSelectListener {
        void onSelect(int index);
    }
}
