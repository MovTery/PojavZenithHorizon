package net.kdt.pojavlaunch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.kdt.pojavlaunch.R;

public class SelectKeycodeDialog extends Dialog {
    private static SelectKeycodeDialog mDialog = null;

    public static SelectKeycodeDialog createDialog(Context context) {
        if (mDialog == null) mDialog = new SelectKeycodeDialog(context);
        return mDialog;
    }

    public static void setSelectKeyCodeListener(@NonNull SelectKeycodeDialog dialog, OnKeycodeSelectListener onKeycodeSelectListener) {
        dialog.setOnKeycodeSelectListener(onKeycodeSelectListener);
    }

    private OnKeycodeSelectListener mOnKeycodeSelectListener;

    public SelectKeycodeDialog(@NonNull Context context) {
        super(context);

        setContentView(R.layout.dialog_keyboard);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.CENTER; // 如果需要的话，设置居中显示

            window.setAttributes(layoutParams);
        }
    }

    private void init() {
        ImageView closeButton = findViewById(R.id.zh_keyboard_close);

        Button specialKeyboard = findViewById(R.id.keyboard_special_keyboard);
        Button specialGUI = findViewById(R.id.keyboard_special_gui);
        Button specialPri = findViewById(R.id.keyboard_special_pri);
        Button specialSec = findViewById(R.id.keyboard_special_sec);
        Button specialMouse = findViewById(R.id.keyboard_special_mouse);

        Button specialMid = findViewById(R.id.keyboard_special_mid);
        Button specialScrollUp = findViewById(R.id.keyboard_special_scrollup);
        Button specialScrollDown = findViewById(R.id.keyboard_special_scrolldown);
        Button specialMenu = findViewById(R.id.keyboard_special_menu);

        Button keyUnknown = findViewById(R.id.keyboard_unknown);

        Button keyHome = findViewById(R.id.keyboard_home);
        Button keyEsc = findViewById(R.id.keyboard_esc);

        Button key0 = findViewById(R.id.keyboard_0);
        Button key1 = findViewById(R.id.keyboard_1);
        Button key2 = findViewById(R.id.keyboard_2);
        Button key3 = findViewById(R.id.keyboard_3);
        Button key4 = findViewById(R.id.keyboard_4);
        Button key5 = findViewById(R.id.keyboard_5);
        Button key6 = findViewById(R.id.keyboard_6);
        Button key7 = findViewById(R.id.keyboard_7);
        Button key8 = findViewById(R.id.keyboard_8);
        Button key9 = findViewById(R.id.keyboard_9);

        Button keyUp = findViewById(R.id.keyboard_up);
        Button keyDown = findViewById(R.id.keyboard_down);
        Button keyLeft = findViewById(R.id.keyboard_left);
        Button keyRight = findViewById(R.id.keyboard_right);

        Button keyA = findViewById(R.id.keyboard_a);
        Button keyB = findViewById(R.id.keyboard_b);
        Button keyC = findViewById(R.id.keyboard_c);
        Button keyD = findViewById(R.id.keyboard_d);
        Button keyE = findViewById(R.id.keyboard_e);
        Button keyF = findViewById(R.id.keyboard_f);
        Button keyG = findViewById(R.id.keyboard_g);
        Button keyH = findViewById(R.id.keyboard_h);
        Button keyI = findViewById(R.id.keyboard_i);
        Button keyJ = findViewById(R.id.keyboard_j);
        Button keyK = findViewById(R.id.keyboard_k);
        Button keyL = findViewById(R.id.keyboard_l);
        Button keyM = findViewById(R.id.keyboard_m);
        Button keyN = findViewById(R.id.keyboard_n);
        Button keyO = findViewById(R.id.keyboard_o);
        Button keyP = findViewById(R.id.keyboard_p);
        Button keyQ = findViewById(R.id.keyboard_q);
        Button keyR = findViewById(R.id.keyboard_r);
        Button keyS = findViewById(R.id.keyboard_s);
        Button keyT = findViewById(R.id.keyboard_t);
        Button keyU = findViewById(R.id.keyboard_u);
        Button keyV = findViewById(R.id.keyboard_v);
        Button keyW = findViewById(R.id.keyboard_w);
        Button keyX = findViewById(R.id.keyboard_x);
        Button keyY = findViewById(R.id.keyboard_y);
        Button keyZ = findViewById(R.id.keyboard_z);

        Button keyComma = findViewById(R.id.keyboard_comma);
        Button keyPeriod = findViewById(R.id.keyboard_period);

        Button keyLeftAlt = findViewById(R.id.keyboard_left_alt);
        Button keyRightAlt = findViewById(R.id.keyboard_right_alt);

        Button keyLeftShift = findViewById(R.id.keyboard_left_shift);
        Button keyRightShift = findViewById(R.id.keyboard_right_shift);

        Button keyTab = findViewById(R.id.keyboard_tab);
        Button keySpace = findViewById(R.id.keyboard_space);
        Button keyEnter = findViewById(R.id.keyboard_enter);
        Button keyBackspace = findViewById(R.id.keyboard_backspace);
        Button keyGraveAccent = findViewById(R.id.keyboard_grave);
        Button keyMinus = findViewById(R.id.keyboard_minus);
        Button keyEquals = findViewById(R.id.keyboard_equals);
        Button keyLeftBracket = findViewById(R.id.keyboard_left_bracket);
        Button keyRightBracket = findViewById(R.id.keyboard_right_bracket);
        Button keyBackslash = findViewById(R.id.keyboard_backslash);
        Button keySemicolon = findViewById(R.id.keyboard_semicolon);
        Button keyApostrophe = findViewById(R.id.keyboard_apostrophe);
        Button keySlash = findViewById(R.id.keyboard_slash);

        Button keyKPAdd = findViewById(R.id.keyboard_kp_add);

        Button keyPageUp = findViewById(R.id.keyboard_page_up);
        Button keyPageDown = findViewById(R.id.keyboard_page_down);

        Button keyLeftControl = findViewById(R.id.keyboard_left_ctrl);
        Button keyRightControl = findViewById(R.id.keyboard_right_ctrl);

        Button keyCapsLock = findViewById(R.id.keyboard_capslock);
        Button keyPause = findViewById(R.id.keyboard_pause);
        Button keyEnd = findViewById(R.id.keyboard_end);
        Button keyInsert = findViewById(R.id.keyboard_insert);

        Button keyF1 = findViewById(R.id.keyboard_f1);
        Button keyF2 = findViewById(R.id.keyboard_f2);
        Button keyF3 = findViewById(R.id.keyboard_f3);
        Button keyF4 = findViewById(R.id.keyboard_f4);
        Button keyF5 = findViewById(R.id.keyboard_f5);
        Button keyF6 = findViewById(R.id.keyboard_f6);
        Button keyF7 = findViewById(R.id.keyboard_f7);
        Button keyF8 = findViewById(R.id.keyboard_f8);
        Button keyF9 = findViewById(R.id.keyboard_f9);
        Button keyF10 = findViewById(R.id.keyboard_f10);
        Button keyF11 = findViewById(R.id.keyboard_f11);
        Button keyF12 = findViewById(R.id.keyboard_f12);

        Button keyNumLock = findViewById(R.id.keyboard_num_lock);
        Button keyKP0 = findViewById(R.id.keyboard_kp_0);
        Button keyKP1 = findViewById(R.id.keyboard_kp_1);
        Button keyKP2 = findViewById(R.id.keyboard_kp_2);
        Button keyKP3 = findViewById(R.id.keyboard_kp_3);
        Button keyKP4 = findViewById(R.id.keyboard_kp_4);
        Button keyKP5 = findViewById(R.id.keyboard_kp_5);
        Button keyKP6 = findViewById(R.id.keyboard_kp_6);
        Button keyKP7 = findViewById(R.id.keyboard_kp_7);
        Button keyKP8 = findViewById(R.id.keyboard_kp_8);
        Button keyKP9 = findViewById(R.id.keyboard_kp_9);
        Button keyKPDivide = findViewById(R.id.keyboard_kp_divide);
        Button keyKPMultiply = findViewById(R.id.keyboard_kp_multiply);
        Button keyKPSubtract = findViewById(R.id.keyboard_kp_subract);
        Button keyKPDecimal = findViewById(R.id.keyboard_kp_decimal);
        Button keyKPEnter = findViewById(R.id.keyboard_kp_enter);

        closeButton.setOnClickListener(v -> this.dismiss());

        //这里的按键比较特殊，它的顺序就是翻着的
        specialKeyboard.setOnClickListener(v -> onKeycodeSelect(8));
        specialGUI.setOnClickListener(v -> onKeycodeSelect(7));
        specialPri.setOnClickListener(v -> onKeycodeSelect(6));
        specialSec.setOnClickListener(v -> onKeycodeSelect(5));
        specialMouse.setOnClickListener(v -> onKeycodeSelect(4));

        specialMid.setOnClickListener(v -> onKeycodeSelect(3));
        specialScrollUp.setOnClickListener(v -> onKeycodeSelect(2));
        specialScrollDown.setOnClickListener(v -> onKeycodeSelect(1));
        specialMenu.setOnClickListener(v -> onKeycodeSelect(0));

        keyUnknown.setOnClickListener(v -> onKeycodeSelect(9));

        keyHome.setOnClickListener(v -> onKeycodeSelect(10));
        keyEsc.setOnClickListener(v -> onKeycodeSelect(11));

        key0.setOnClickListener(v -> onKeycodeSelect(12));
        key1.setOnClickListener(v -> onKeycodeSelect(13));
        key2.setOnClickListener(v -> onKeycodeSelect(14));
        key3.setOnClickListener(v -> onKeycodeSelect(15));
        key4.setOnClickListener(v -> onKeycodeSelect(16));
        key5.setOnClickListener(v -> onKeycodeSelect(17));
        key6.setOnClickListener(v -> onKeycodeSelect(18));
        key7.setOnClickListener(v -> onKeycodeSelect(19));
        key8.setOnClickListener(v -> onKeycodeSelect(20));
        key9.setOnClickListener(v -> onKeycodeSelect(21));//

        keyUp.setOnClickListener(v -> onKeycodeSelect(23));
        keyDown.setOnClickListener(v -> onKeycodeSelect(24));
        keyLeft.setOnClickListener(v -> onKeycodeSelect(25));
        keyRight.setOnClickListener(v -> onKeycodeSelect(26));

        keyA.setOnClickListener(v -> onKeycodeSelect(27));
        keyB.setOnClickListener(v -> onKeycodeSelect(28));
        keyC.setOnClickListener(v -> onKeycodeSelect(29));
        keyD.setOnClickListener(v -> onKeycodeSelect(30));
        keyE.setOnClickListener(v -> onKeycodeSelect(31));
        keyF.setOnClickListener(v -> onKeycodeSelect(32));
        keyG.setOnClickListener(v -> onKeycodeSelect(33));
        keyH.setOnClickListener(v -> onKeycodeSelect(34));
        keyI.setOnClickListener(v -> onKeycodeSelect(35));
        keyJ.setOnClickListener(v -> onKeycodeSelect(36));
        keyK.setOnClickListener(v -> onKeycodeSelect(37));
        keyL.setOnClickListener(v -> onKeycodeSelect(38));
        keyM.setOnClickListener(v -> onKeycodeSelect(39));
        keyN.setOnClickListener(v -> onKeycodeSelect(40));
        keyO.setOnClickListener(v -> onKeycodeSelect(41));
        keyP.setOnClickListener(v -> onKeycodeSelect(42));
        keyQ.setOnClickListener(v -> onKeycodeSelect(43));
        keyR.setOnClickListener(v -> onKeycodeSelect(44));
        keyS.setOnClickListener(v -> onKeycodeSelect(45));
        keyT.setOnClickListener(v -> onKeycodeSelect(46));
        keyU.setOnClickListener(v -> onKeycodeSelect(47));
        keyV.setOnClickListener(v -> onKeycodeSelect(48));
        keyW.setOnClickListener(v -> onKeycodeSelect(49));
        keyX.setOnClickListener(v -> onKeycodeSelect(50));
        keyY.setOnClickListener(v -> onKeycodeSelect(51));
        keyZ.setOnClickListener(v -> onKeycodeSelect(52));

        keyComma.setOnClickListener(v -> onKeycodeSelect(53));
        keyPeriod.setOnClickListener(v -> onKeycodeSelect(54));

        keyLeftAlt.setOnClickListener(v -> onKeycodeSelect(55));
        keyRightAlt.setOnClickListener(v -> onKeycodeSelect(56));

        keyLeftShift.setOnClickListener(v -> onKeycodeSelect(57));
        keyRightShift.setOnClickListener(v -> onKeycodeSelect(58));

        keyTab.setOnClickListener(v -> onKeycodeSelect(59));
        keySpace.setOnClickListener(v -> onKeycodeSelect(60));
        keyEnter.setOnClickListener(v -> onKeycodeSelect(61));
        keyBackspace.setOnClickListener(v -> onKeycodeSelect(62));
        keyGraveAccent.setOnClickListener(v -> onKeycodeSelect(63));
        keyMinus.setOnClickListener(v -> onKeycodeSelect(64));
        keyEquals.setOnClickListener(v -> onKeycodeSelect(65));
        keyLeftBracket.setOnClickListener(v -> onKeycodeSelect(66));
        keyRightBracket.setOnClickListener(v -> onKeycodeSelect(67));
        keyBackslash.setOnClickListener(v -> onKeycodeSelect(68));
        keySemicolon.setOnClickListener(v -> onKeycodeSelect(69));
        keyApostrophe.setOnClickListener(v -> onKeycodeSelect(70));
        keySlash.setOnClickListener(v -> onKeycodeSelect(71));//

        keyKPAdd.setOnClickListener(v -> onKeycodeSelect(73));

        keyPageUp.setOnClickListener(v -> onKeycodeSelect(74));
        keyPageDown.setOnClickListener(v -> onKeycodeSelect(75));//

        keyLeftControl.setOnClickListener(v -> onKeycodeSelect(77));
        keyRightControl.setOnClickListener(v -> onKeycodeSelect(78));

        keyCapsLock.setOnClickListener(v -> onKeycodeSelect(79));
        keyPause.setOnClickListener(v -> onKeycodeSelect(80));
        keyHome.setOnClickListener(v -> onKeycodeSelect(81));
        keyEnd.setOnClickListener(v -> onKeycodeSelect(82));
        keyInsert.setOnClickListener(v -> onKeycodeSelect(83));

        keyF1.setOnClickListener(v -> onKeycodeSelect(84));
        keyF2.setOnClickListener(v -> onKeycodeSelect(85));
        keyF3.setOnClickListener(v -> onKeycodeSelect(86));
        keyF4.setOnClickListener(v -> onKeycodeSelect(87));
        keyF5.setOnClickListener(v -> onKeycodeSelect(88));
        keyF6.setOnClickListener(v -> onKeycodeSelect(89));
        keyF7.setOnClickListener(v -> onKeycodeSelect(90));
        keyF8.setOnClickListener(v -> onKeycodeSelect(91));
        keyF9.setOnClickListener(v -> onKeycodeSelect(92));
        keyF10.setOnClickListener(v -> onKeycodeSelect(93));
        keyF11.setOnClickListener(v -> onKeycodeSelect(94));
        keyF12.setOnClickListener(v -> onKeycodeSelect(95));

        keyNumLock.setOnClickListener(v -> onKeycodeSelect(96));
        keyKP0.setOnClickListener(v -> onKeycodeSelect(97));
        keyKP1.setOnClickListener(v -> onKeycodeSelect(98));
        keyKP2.setOnClickListener(v -> onKeycodeSelect(99));
        keyKP3.setOnClickListener(v -> onKeycodeSelect(100));
        keyKP4.setOnClickListener(v -> onKeycodeSelect(101));
        keyKP5.setOnClickListener(v -> onKeycodeSelect(102));
        keyKP6.setOnClickListener(v -> onKeycodeSelect(103));
        keyKP7.setOnClickListener(v -> onKeycodeSelect(104));
        keyKP8.setOnClickListener(v -> onKeycodeSelect(105));
        keyKP9.setOnClickListener(v -> onKeycodeSelect(106));
        keyKPDivide.setOnClickListener(v -> onKeycodeSelect(107));
        keyKPMultiply.setOnClickListener(v -> onKeycodeSelect(108));
        keyKPSubtract.setOnClickListener(v -> onKeycodeSelect(109));
//      110
        keyKPDecimal.setOnClickListener(v -> onKeycodeSelect(111));
//      112
        keyKPEnter.setOnClickListener(v -> onKeycodeSelect(113));
//      114
    }

    private void onKeycodeSelect(int index) {
        if (this.mOnKeycodeSelectListener != null) {
            this.mOnKeycodeSelectListener.onSelect(index);
        }
    }

    private void setOnKeycodeSelectListener(OnKeycodeSelectListener listener) {
        this.mOnKeycodeSelectListener = listener;
    }

    public interface OnKeycodeSelectListener {
        void onSelect(int index);
    }
}
