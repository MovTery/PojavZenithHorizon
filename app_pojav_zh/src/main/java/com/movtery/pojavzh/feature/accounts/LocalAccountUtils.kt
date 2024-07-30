package com.movtery.pojavzh.feature.accounts;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class LocalAccountUtils {
    public static void checkUsageAllowed(CheckResultListener listener) {
        if (AccountsManager.getInstance().haveMicrosoftAccount()) {
            listener.onUsageAllowed();
        } else {
            listener.onUsageDenied();
        }
    }

    @SuppressLint("InflateParams")
    public static void openDialog(Activity activity, TipDialog.OnConfirmClickListener confirmClickListener, String message, int confirm) {
        LayoutInflater inflater = activity.getLayoutInflater();
        SharedPreferences.Editor edit = DEFAULT_PREF.edit();
        //不再提醒
        CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.item_check_box, null);
        checkBox.setText(R.string.zh_no_more_reminders);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> edit.putBoolean("localAccountReminders", !isChecked).apply());

        new TipDialog.Builder(activity)
                .setTitle(R.string.zh_warning)
                .setMessage(message)
                .addView(checkBox)
                .setConfirmClickListener(confirmClickListener)
                .setConfirm(confirm)
                .setCancelClickListener(() -> Tools.openURL(activity, ZHTools.URL_MINECRAFT))
                .setCancel(R.string.zh_account_purchase_minecraft_account)
                .buildDialog();
    }

    public interface CheckResultListener {
        void onUsageAllowed();
        void onUsageDenied();
    }
}
