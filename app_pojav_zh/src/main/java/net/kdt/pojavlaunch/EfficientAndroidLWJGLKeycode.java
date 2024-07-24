package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.PojavApplication.getResString;
import static org.lwjgl.glfw.CallbackBridge.sendKeyPress;

import android.view.KeyEvent;

import org.lwjgl.glfw.CallbackBridge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EfficientAndroidLWJGLKeycode {

    //This old version of this class was using an ArrayMap, a generic Key -> Value data structure.
    //The key being the android keycode from a KeyEvent
    //The value its LWJGL equivalent.
    private static final int KEYCODE_COUNT = 106;
    private static final int[] sAndroidKeycodes = new int[KEYCODE_COUNT];
    private static final short[] sLwjglKeycodes = new short[KEYCODE_COUNT];
    private static final Map<Integer, String> sKeycodesName = new HashMap<>();
    private static String[] androidKeyNameArray; /* = new String[androidKeycodes.length]; */
    private static int mTmpCount = 0;

    static {

        /*  BINARY SEARCH IS PERFORMED ON THE androidKeycodes ARRAY !
            WHEN ADDING A MAPPING, ADD IT SO THE androidKeycodes ARRAY STAYS SORTED ! */
        // Mapping Android Keycodes to LWJGL Keycodes
        add(KeyEvent.KEYCODE_UNKNOWN, LwjglGlfwKeycode.GLFW_KEY_UNKNOWN, getResString(R.string.zh_unknown));
        add(KeyEvent.KEYCODE_HOME, LwjglGlfwKeycode.GLFW_KEY_HOME, "Home");
        // Escape key
        add(KeyEvent.KEYCODE_BACK, LwjglGlfwKeycode.GLFW_KEY_ESCAPE, "Back (Esc)");

        // 0-9 keys
        add(KeyEvent.KEYCODE_0, LwjglGlfwKeycode.GLFW_KEY_0, "0"); //7
        add(KeyEvent.KEYCODE_1, LwjglGlfwKeycode.GLFW_KEY_1, "1");
        add(KeyEvent.KEYCODE_2, LwjglGlfwKeycode.GLFW_KEY_2, "2");
        add(KeyEvent.KEYCODE_3, LwjglGlfwKeycode.GLFW_KEY_3, "3");
        add(KeyEvent.KEYCODE_4, LwjglGlfwKeycode.GLFW_KEY_4, "4");
        add(KeyEvent.KEYCODE_5, LwjglGlfwKeycode.GLFW_KEY_5, "5");
        add(KeyEvent.KEYCODE_6, LwjglGlfwKeycode.GLFW_KEY_6, "6");
        add(KeyEvent.KEYCODE_7, LwjglGlfwKeycode.GLFW_KEY_7, "7");
        add(KeyEvent.KEYCODE_8, LwjglGlfwKeycode.GLFW_KEY_8, "8");
        add(KeyEvent.KEYCODE_9, LwjglGlfwKeycode.GLFW_KEY_9, "9"); //16

        add(KeyEvent.KEYCODE_POUND, LwjglGlfwKeycode.GLFW_KEY_3, "# (3)");

        // Arrow keys
        add(KeyEvent.KEYCODE_DPAD_UP, LwjglGlfwKeycode.GLFW_KEY_UP, "↑"); //19
        add(KeyEvent.KEYCODE_DPAD_DOWN, LwjglGlfwKeycode.GLFW_KEY_DOWN, "↓");
        add(KeyEvent.KEYCODE_DPAD_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT, "←");
        add(KeyEvent.KEYCODE_DPAD_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT, "→"); //22

        // A-Z keys
        add(KeyEvent.KEYCODE_A, LwjglGlfwKeycode.GLFW_KEY_A, "A"); //29
        add(KeyEvent.KEYCODE_B, LwjglGlfwKeycode.GLFW_KEY_B, "B");
        add(KeyEvent.KEYCODE_C, LwjglGlfwKeycode.GLFW_KEY_C, "C");
        add(KeyEvent.KEYCODE_D, LwjglGlfwKeycode.GLFW_KEY_D, "D");
        add(KeyEvent.KEYCODE_E, LwjglGlfwKeycode.GLFW_KEY_E, "E");
        add(KeyEvent.KEYCODE_F, LwjglGlfwKeycode.GLFW_KEY_F, "F");
        add(KeyEvent.KEYCODE_G, LwjglGlfwKeycode.GLFW_KEY_G, "G");
        add(KeyEvent.KEYCODE_H, LwjglGlfwKeycode.GLFW_KEY_H, "H");
        add(KeyEvent.KEYCODE_I, LwjglGlfwKeycode.GLFW_KEY_I, "I");
        add(KeyEvent.KEYCODE_J, LwjglGlfwKeycode.GLFW_KEY_J, "J");
        add(KeyEvent.KEYCODE_K, LwjglGlfwKeycode.GLFW_KEY_K, "K");
        add(KeyEvent.KEYCODE_L, LwjglGlfwKeycode.GLFW_KEY_L, "L");
        add(KeyEvent.KEYCODE_M, LwjglGlfwKeycode.GLFW_KEY_M, "M");
        add(KeyEvent.KEYCODE_N, LwjglGlfwKeycode.GLFW_KEY_N, "N");
        add(KeyEvent.KEYCODE_O, LwjglGlfwKeycode.GLFW_KEY_O, "O");
        add(KeyEvent.KEYCODE_P, LwjglGlfwKeycode.GLFW_KEY_P, "P");
        add(KeyEvent.KEYCODE_Q, LwjglGlfwKeycode.GLFW_KEY_Q, "Q");
        add(KeyEvent.KEYCODE_R, LwjglGlfwKeycode.GLFW_KEY_R, "R");
        add(KeyEvent.KEYCODE_S, LwjglGlfwKeycode.GLFW_KEY_S, "S");
        add(KeyEvent.KEYCODE_T, LwjglGlfwKeycode.GLFW_KEY_T, "T");
        add(KeyEvent.KEYCODE_U, LwjglGlfwKeycode.GLFW_KEY_U, "U");
        add(KeyEvent.KEYCODE_V, LwjglGlfwKeycode.GLFW_KEY_V, "V");
        add(KeyEvent.KEYCODE_W, LwjglGlfwKeycode.GLFW_KEY_W, "W");
        add(KeyEvent.KEYCODE_X, LwjglGlfwKeycode.GLFW_KEY_X, "X");
        add(KeyEvent.KEYCODE_Y, LwjglGlfwKeycode.GLFW_KEY_Y, "Y");
        add(KeyEvent.KEYCODE_Z, LwjglGlfwKeycode.GLFW_KEY_Z, "Z"); //54


        add(KeyEvent.KEYCODE_COMMA, LwjglGlfwKeycode.GLFW_KEY_COMMA, ",");
        add(KeyEvent.KEYCODE_PERIOD, LwjglGlfwKeycode.GLFW_KEY_PERIOD, ".");

        // Alt keys
        add(KeyEvent.KEYCODE_ALT_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_ALT, getResString(R.string.zh_keycode_left_alt));
        add(KeyEvent.KEYCODE_ALT_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_ALT, getResString(R.string.zh_keycode_right_alt));

        // Shift keys
        add(KeyEvent.KEYCODE_SHIFT_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_SHIFT, getResString(R.string.zh_keycode_left_shift));
        add(KeyEvent.KEYCODE_SHIFT_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_SHIFT, getResString(R.string.zh_keycode_right_shift));

        add(KeyEvent.KEYCODE_TAB, LwjglGlfwKeycode.GLFW_KEY_TAB, "Tab ⇄");
        add(KeyEvent.KEYCODE_SPACE, LwjglGlfwKeycode.GLFW_KEY_SPACE, getResString(R.string.zh_keycode_space));
        add(KeyEvent.KEYCODE_ENTER, LwjglGlfwKeycode.GLFW_KEY_ENTER, "Enter ↲"); //66
        add(KeyEvent.KEYCODE_DEL, LwjglGlfwKeycode.GLFW_KEY_BACKSPACE, "← Backspace"); // Backspace
        add(KeyEvent.KEYCODE_GRAVE, LwjglGlfwKeycode.GLFW_KEY_GRAVE_ACCENT, "`");
        add(KeyEvent.KEYCODE_MINUS, LwjglGlfwKeycode.GLFW_KEY_MINUS, "-");
        add(KeyEvent.KEYCODE_EQUALS, LwjglGlfwKeycode.GLFW_KEY_EQUAL, "=");
        add(KeyEvent.KEYCODE_LEFT_BRACKET, LwjglGlfwKeycode.GLFW_KEY_LEFT_BRACKET, "[");
        add(KeyEvent.KEYCODE_RIGHT_BRACKET, LwjglGlfwKeycode.GLFW_KEY_RIGHT_BRACKET, "]");
        add(KeyEvent.KEYCODE_BACKSLASH, LwjglGlfwKeycode.GLFW_KEY_BACKSLASH, "\\");
        add(KeyEvent.KEYCODE_SEMICOLON, LwjglGlfwKeycode.GLFW_KEY_SEMICOLON, ";"); //74
        add(KeyEvent.KEYCODE_APOSTROPHE, LwjglGlfwKeycode.GLFW_KEY_APOSTROPHE, "'");
        add(KeyEvent.KEYCODE_SLASH, LwjglGlfwKeycode.GLFW_KEY_SLASH, "/"); //76
        add(KeyEvent.KEYCODE_AT, LwjglGlfwKeycode.GLFW_KEY_2, "@ (2)");

        add(KeyEvent.KEYCODE_PLUS, LwjglGlfwKeycode.GLFW_KEY_KP_ADD, getResString(R.string.zh_keycode_kp_plus));

        // Page keys
        add(KeyEvent.KEYCODE_PAGE_UP, LwjglGlfwKeycode.GLFW_KEY_PAGE_UP, "PGUp +"); //92
        add(KeyEvent.KEYCODE_PAGE_DOWN, LwjglGlfwKeycode.GLFW_KEY_PAGE_DOWN, "PGDn -");

        add(KeyEvent.KEYCODE_ESCAPE, LwjglGlfwKeycode.GLFW_KEY_ESCAPE, "Esc");

        // Control keys
        add(KeyEvent.KEYCODE_CTRL_LEFT, LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL, getResString(R.string.zh_keycode_left_ctrl));
        add(KeyEvent.KEYCODE_CTRL_RIGHT, LwjglGlfwKeycode.GLFW_KEY_RIGHT_CONTROL, getResString(R.string.zh_keycode_right_ctrl));

        add(KeyEvent.KEYCODE_CAPS_LOCK, LwjglGlfwKeycode.GLFW_KEY_CAPS_LOCK, "Caps Lock");
        add(KeyEvent.KEYCODE_BREAK, LwjglGlfwKeycode.GLFW_KEY_PAUSE, "Pause/Break");
        add(KeyEvent.KEYCODE_MOVE_HOME, LwjglGlfwKeycode.GLFW_KEY_HOME, "Home");
        add(KeyEvent.KEYCODE_MOVE_END, LwjglGlfwKeycode.GLFW_KEY_END, "End");
        add(KeyEvent.KEYCODE_INSERT, LwjglGlfwKeycode.GLFW_KEY_INSERT, "Insert");


        // Fn keys
        add(KeyEvent.KEYCODE_F1, LwjglGlfwKeycode.GLFW_KEY_F1, "F1"); //131
        add(KeyEvent.KEYCODE_F2, LwjglGlfwKeycode.GLFW_KEY_F2, "F2");
        add(KeyEvent.KEYCODE_F3, LwjglGlfwKeycode.GLFW_KEY_F3, "F3");
        add(KeyEvent.KEYCODE_F4, LwjglGlfwKeycode.GLFW_KEY_F4, "F4");
        add(KeyEvent.KEYCODE_F5, LwjglGlfwKeycode.GLFW_KEY_F5, "F5");
        add(KeyEvent.KEYCODE_F6, LwjglGlfwKeycode.GLFW_KEY_F6, "F6");
        add(KeyEvent.KEYCODE_F7, LwjglGlfwKeycode.GLFW_KEY_F7, "F7");
        add(KeyEvent.KEYCODE_F8, LwjglGlfwKeycode.GLFW_KEY_F8, "F8");
        add(KeyEvent.KEYCODE_F9, LwjglGlfwKeycode.GLFW_KEY_F9, "F9");
        add(KeyEvent.KEYCODE_F10, LwjglGlfwKeycode.GLFW_KEY_F10, "F10");
        add(KeyEvent.KEYCODE_F11, LwjglGlfwKeycode.GLFW_KEY_F11, "F11");
        add(KeyEvent.KEYCODE_F12, LwjglGlfwKeycode.GLFW_KEY_F12, "F12"); //142

        // Num keys
        add(KeyEvent.KEYCODE_NUM_LOCK, LwjglGlfwKeycode.GLFW_KEY_NUM_LOCK, "Num Lock"); //143
        add(KeyEvent.KEYCODE_NUMPAD_0, LwjglGlfwKeycode.GLFW_KEY_KP_0, getResString(R.string.zh_keycode_kp_0));
        add(KeyEvent.KEYCODE_NUMPAD_1, LwjglGlfwKeycode.GLFW_KEY_KP_1, getResString(R.string.zh_keycode_kp_1));
        add(KeyEvent.KEYCODE_NUMPAD_2, LwjglGlfwKeycode.GLFW_KEY_KP_2, getResString(R.string.zh_keycode_kp_2));
        add(KeyEvent.KEYCODE_NUMPAD_3, LwjglGlfwKeycode.GLFW_KEY_KP_3, getResString(R.string.zh_keycode_kp_3));
        add(KeyEvent.KEYCODE_NUMPAD_4, LwjglGlfwKeycode.GLFW_KEY_KP_4, getResString(R.string.zh_keycode_kp_4));
        add(KeyEvent.KEYCODE_NUMPAD_5, LwjglGlfwKeycode.GLFW_KEY_KP_5, getResString(R.string.zh_keycode_kp_5));
        add(KeyEvent.KEYCODE_NUMPAD_6, LwjglGlfwKeycode.GLFW_KEY_KP_6, getResString(R.string.zh_keycode_kp_6));
        add(KeyEvent.KEYCODE_NUMPAD_7, LwjglGlfwKeycode.GLFW_KEY_KP_7, getResString(R.string.zh_keycode_kp_7));
        add(KeyEvent.KEYCODE_NUMPAD_8, LwjglGlfwKeycode.GLFW_KEY_KP_8, getResString(R.string.zh_keycode_kp_8));
        add(KeyEvent.KEYCODE_NUMPAD_9, LwjglGlfwKeycode.GLFW_KEY_KP_9, getResString(R.string.zh_keycode_kp_9));
        add(KeyEvent.KEYCODE_NUMPAD_DIVIDE, LwjglGlfwKeycode.GLFW_KEY_KP_DIVIDE, getResString(R.string.zh_keycode_kp_divide));
        add(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, LwjglGlfwKeycode.GLFW_KEY_KP_MULTIPLY, getResString(R.string.zh_keycode_kp_multiply));
        add(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, LwjglGlfwKeycode.GLFW_KEY_KP_SUBTRACT, getResString(R.string.zh_keycode_kp_subtract));
        add(KeyEvent.KEYCODE_NUMPAD_ADD, LwjglGlfwKeycode.GLFW_KEY_KP_ADD, getResString(R.string.zh_keycode_kp_plus));
        add(KeyEvent.KEYCODE_NUMPAD_DOT, LwjglGlfwKeycode.GLFW_KEY_KP_DECIMAL, getResString(R.string.zh_keycode_kp_decimal));
        add(KeyEvent.KEYCODE_NUMPAD_COMMA, LwjglGlfwKeycode.GLFW_KEY_COMMA, getResString(R.string.zh_keycode_kp_comma));
        add(KeyEvent.KEYCODE_NUMPAD_ENTER, LwjglGlfwKeycode.GLFW_KEY_KP_ENTER, getResString(R.string.zh_keycode_kp_enter));
        add(KeyEvent.KEYCODE_NUMPAD_EQUALS, LwjglGlfwKeycode.GLFW_KEY_EQUAL, getResString(R.string.zh_keycode_kp_equal)); //161


    }

    public static boolean containsIndex(int index){
        return index >= 0;
    }

    public static String[] generateKeyName() {
        if (androidKeyNameArray == null) {
            androidKeyNameArray = new String[sAndroidKeycodes.length];
            for(int i=0; i < androidKeyNameArray.length; ++i){
                androidKeyNameArray[i] = sKeycodesName.get(i);
            }
        }
        return androidKeyNameArray;
    }

    public static void execKey(KeyEvent keyEvent, int valueIndex) {
        //valueIndex points to where the value is stored in the array.
        CallbackBridge.holdingAlt = keyEvent.isAltPressed();
        CallbackBridge.holdingCapslock = keyEvent.isCapsLockOn();
        CallbackBridge.holdingCtrl = keyEvent.isCtrlPressed();
        CallbackBridge.holdingNumlock = keyEvent.isNumLockOn();
        CallbackBridge.holdingShift = keyEvent.isShiftPressed();

        System.out.println(keyEvent.getKeyCode() + " " +keyEvent.getDisplayLabel());
        char key = (char)(keyEvent.getUnicodeChar() != 0 ? keyEvent.getUnicodeChar() : '\u0000');
        sendKeyPress(
                getValueByIndex(valueIndex),
                key,
                0,
                CallbackBridge.getCurrentMods(),
                keyEvent.getAction() == KeyEvent.ACTION_DOWN);
    }

    public static void execKeyIndex(int index){
        //Send a quick key press.
        sendKeyPress(getValueByIndex(index));
    }

    public static int getValueByIndex(int index) {
        return sLwjglKeycodes[index];
    }

    public static int getIndexByKey(int key){
        return Arrays.binarySearch(sAndroidKeycodes, key);
    }

    /** @return the index at which the key is in the array, searching linearly */
    public static int getIndexByValue(int lwjglKey) {
        //You should avoid using this function on performance critical areas
        for (int i = 0; i < sLwjglKeycodes.length; i++) {
            if(sLwjglKeycodes[i] == lwjglKey) return i;
        }
        return 0;
    }

    private static void add(int androidKeycode, short LWJGLKeycode, String name){
        sAndroidKeycodes[mTmpCount] = androidKeycode;
        sLwjglKeycodes[mTmpCount] = LWJGLKeycode;

        sKeycodesName.put(mTmpCount, name);
        mTmpCount ++;
    }
}
