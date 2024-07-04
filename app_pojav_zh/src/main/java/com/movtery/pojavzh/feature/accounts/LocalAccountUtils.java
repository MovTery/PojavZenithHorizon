package com.movtery.pojavzh.feature.accounts;

import android.app.Activity;

import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;

public class LocalAccountUtils {
    public static void checkUsageAllowed(CheckResultListener listener) {
        boolean areaChecks = ZHTools.areaChecks();
        if (AccountsManager.haveMicrosoftAccount()) {
            listener.onUsageAllowed(areaChecks);
        } else {
            listener.onUsageDenied(areaChecks);
        }
    }

    public static void openDialog(Activity activity, TipDialog.OnConfirmClickListener confirmClickListener, TipDialog.OnCancelClickListener cancelClickListener, String message, int confirm, String cancel) {
        new TipDialog.Builder(activity)
                .setTitle(R.string.zh_warning)
                .setMessage(message)
                .setConfirmClickListener(confirmClickListener)
                .setConfirm(confirm)
                .setCancelClickListener(cancelClickListener)
                .setCancel(cancel)
                .buildDialog();
    }

    public interface CheckResultListener {
        void onUsageAllowed(boolean areaChecks);
        void onUsageDenied(boolean areaChecks);
    }
}
