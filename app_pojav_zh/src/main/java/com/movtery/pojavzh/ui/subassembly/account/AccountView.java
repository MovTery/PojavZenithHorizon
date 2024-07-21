package com.movtery.pojavzh.ui.subassembly.account;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.ui.dialog.AccountsDialog;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;

public class AccountView {
    private final Context mContext;
    private final View mMainView;
    private final ImageView mUserIconView;
    private final TextView mUserNameView;

    public AccountView(View view) {
        this.mContext = view.getContext();

        mMainView = view;
        mUserIconView = view.findViewById(R.id.user_icon);
        mUserNameView = view.findViewById(R.id.user_name);
    }

    public void refreshAccountInfo() {
        MinecraftAccount account = AccountsManager.getInstance().getCurrentAccount();
        if (mMainView != null && mUserIconView != null && mUserNameView != null) {
            if (account == null) {
                mUserIconView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add));
                mUserNameView.setText(R.string.main_add_account);
                mMainView.setOnClickListener(v -> ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true));
                return;
            }

            SelectAccountListener refreshListener = selectAccount -> refreshAccountInfo();
            mMainView.setOnClickListener(v -> new AccountsDialog(mContext, refreshListener).show());

            Drawable drawable = null;
            if (account.isMicrosoft) {
                File iconFile = new File(ZHTools.DIR_USER_ICON, account.username + ".png");
                if (iconFile.exists()) {
                    drawable = Drawable.createFromPath(iconFile.getAbsolutePath());
                }
            }
            mUserIconView.setImageDrawable(drawable == null ? ContextCompat.getDrawable(mContext, R.drawable.ic_head_steve) : drawable);
            mUserNameView.setText(account.username);
        }
    }
}
