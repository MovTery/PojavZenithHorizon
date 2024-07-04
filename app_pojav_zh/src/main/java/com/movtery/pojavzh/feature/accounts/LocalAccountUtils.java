package com.movtery.pojavzh.feature.accounts;

import android.app.Activity;

import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class LocalAccountUtils {
    public static void checkUsageAllowed(CheckResultListener listener) {
        if (AccountsManager.haveMicrosoftAccount()) {
            listener.onUsageAllowed();
        } else {
            listener.onUsageDenied();
        }
    }

    public static void openDialog(Activity activity, TipDialog.OnConfirmClickListener confirmClickListener, String message, int confirm) {
        new TipDialog.Builder(activity)
                .setTitle(R.string.zh_warning)
                .setMessage(message)
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
