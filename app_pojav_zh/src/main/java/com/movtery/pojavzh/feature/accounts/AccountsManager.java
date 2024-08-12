package com.movtery.pojavzh.feature.accounts;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.LauncherActivity;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.authenticator.microsoft.PresentedException;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountsManager {
    private final static int MAX_LOGIN_STEP = 5;
    @SuppressLint("StaticFieldLeak")
    private static volatile AccountsManager accountsManager;
    private final Context context;
    private final List<MinecraftAccount> accounts = new ArrayList<>();
    private ObjectAnimator mLoginBarAnimator;
    private ProgressListener mProgressListener;
    private DoneListener mDoneListener;
    private ErrorListener mErrorListener;

    public AccountsManager(Context context) {
        this.context = context;
    }

    public static AccountsManager getInstance() {
        if (accountsManager == null) {
            synchronized (AccountsManager.class) {
                if (accountsManager == null) {
                    accountsManager = new AccountsManager(PojavApplication.getContext());
                    //确保完全初始化，初始化完成之后，初始化监听器，然后执行刷新与登录操作
                    accountsManager.initListener();
                    accountsManager.reload();
                    accountsManager.performLogin(accountsManager.getCurrentAccount());
                }
                return accountsManager;
            }
        }
        return accountsManager;
    }

    @SuppressLint("ObjectAnimatorBinding")
    private void initListener() {
        mProgressListener = step -> {
            // Animate the login bar, cosmetic purposes only
            float mLoginBarWidth = -1;
            if (mLoginBarAnimator != null) {
                mLoginBarAnimator.cancel();
                mLoginBarAnimator.setFloatValues(mLoginBarWidth, ((float) Tools.currentDisplayMetrics.widthPixels / MAX_LOGIN_STEP * step));
            } else {
                mLoginBarAnimator = ObjectAnimator.ofFloat(this, "LoginBarWidth", mLoginBarWidth, ((float) Tools.currentDisplayMetrics.widthPixels / MAX_LOGIN_STEP * step));
            }
            mLoginBarAnimator.start();
        };

        mDoneListener = account -> {
            Toast.makeText(context, R.string.main_login_done, Toast.LENGTH_SHORT).show();

            //检查账号是否已存在
            if (getAllAccount().contains(account)) return;

            if (getAllAccount().isEmpty())
                PojavProfile.setCurrentProfile(context, account.username);
            reload();
        };

        mErrorListener = errorMessage -> {
            Activity activity = LauncherActivity.getActivity();
            if (activity != null) {
                if (errorMessage instanceof PresentedException) {
                    PresentedException exception = (PresentedException) errorMessage;
                    Throwable cause = exception.getCause();
                    if (cause == null) {
                        Tools.dialog(activity, activity.getString(R.string.global_error), exception.toString(activity));
                    } else {
                        Tools.showError(activity, exception.toString(activity), exception.getCause());
                    }
                } else {
                    Tools.showError(activity, errorMessage);
                }
            }
        };
    }

    //强制登录（忽略账号是否未到期）
    public void forcedLogin(MinecraftAccount minecraftAccount) {
        if (AccountUtils.isNoLoginRequired(minecraftAccount)) return;

        if (AccountUtils.isOtherLoginAccount(minecraftAccount)) {
            AccountUtils.otherLogin(context, minecraftAccount);
            return;
        }

        if (minecraftAccount.isMicrosoft) {
            AccountUtils.microsoftLogin(minecraftAccount);
        }
    }

    //普通登录
    public void performLogin(MinecraftAccount minecraftAccount) {
        if (AccountUtils.isNoLoginRequired(minecraftAccount)) {
            return;
        }

        if (AccountUtils.isOtherLoginAccount(minecraftAccount) && ZHTools.getCurrentTimeMillis() > minecraftAccount.expiresAt) {
            AccountUtils.otherLogin(context, minecraftAccount);
            return;
        }

        if (minecraftAccount.isMicrosoft) {
            if (ZHTools.getCurrentTimeMillis() > minecraftAccount.expiresAt) {
                AccountUtils.microsoftLogin(minecraftAccount);
            }
        }
    }

    public void reload() {
        accounts.clear();
        File accountsPath = new File(Tools.DIR_ACCOUNT_NEW);
        if (accountsPath.exists() && accountsPath.isDirectory()) {
            File[] files = accountsPath.listFiles();
            if (files != null) {
                for (File accountFile : files) {
                    try {
                        String jsonString = Tools.read(accountFile);
                        MinecraftAccount account = MinecraftAccount.parse(jsonString);
                        if (account != null) accounts.add(account);
                    } catch (IOException e) {
                        Logging.e("AccountsManager", String.format("File %s is not recognized as a profile for an account", accountFile.getName()));
                    }
                }
            }
        }
        Logging.i("AccountsManager", "Reload complete.");
//        System.out.println(accounts);
    }

    public MinecraftAccount getCurrentAccount() {
        MinecraftAccount account = PojavProfile.getCurrentProfileContent(context, null);
        if (account == null) {
            if (getAllAccount().isEmpty()) return null;
            MinecraftAccount account1 = getAllAccount().get(0);
            PojavProfile.setCurrentProfile(context, account1.username);
            return account1;
        }
        return account;
    }

    public List<MinecraftAccount> getAllAccount() {
        return new ArrayList<>(accounts);
    }

    public boolean haveMicrosoftAccount() {
        for (MinecraftAccount account : accounts) {
            if (account.isMicrosoft) {
                return true;
            }
        }
        return false;
    }

    public ProgressListener getProgressListener() {
        return mProgressListener;
    }

    public DoneListener getDoneListener() {
        return mDoneListener;
    }

    public ErrorListener getErrorListener() {
        return mErrorListener;
    }
}
