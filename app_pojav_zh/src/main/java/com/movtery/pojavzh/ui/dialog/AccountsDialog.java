package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.ui.subassembly.account.AccountAdapter;
import com.movtery.pojavzh.ui.subassembly.account.SelectAccountListener;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountsDialog extends FullScreenDialog implements TaskCountListener {
    private final AccountsManager accountsManager = AccountsManager.getInstance();
    private final List<MinecraftAccount> mData = new ArrayList<>();
    private final DialogDismissListener dismissListener;
    private AccountAdapter accountAdapter;
    private RecyclerView recyclerView;
    private boolean isTaskRunning = false;

    public AccountsDialog(@NonNull Context context, DialogDismissListener dismissListener) {
        super(context);

        ProgressKeeper.addTaskCountListener(this);

        this.setContentView(R.layout.dialog_select_item);

        this.dismissListener = dismissListener;

        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        recyclerView = findViewById(R.id.zh_select_view);
        TextView mTitleText = findViewById(R.id.zh_select_item_title);
        ImageButton mCloseButton = findViewById(R.id.zh_select_item_close_button);
        mTitleText.setText(R.string.zh_account_manager);
        mCloseButton.setOnClickListener(v -> this.dismiss());

        initView();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (dismissListener != null) dismissListener.onDismiss();
        ProgressKeeper.removeTaskCountListener(this);
    }

    private void initView() {
        SelectAccountListener selectAccountListener = account -> {
            if (account != null) {
                PojavProfile.setCurrentProfile(getContext(), account.username);
            } else {
                ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true);
            }

            this.dismiss();
        };

        this.accountAdapter = new AccountAdapter(mData);
        accountAdapter.setAccountUpdateListener(new AccountAdapter.AccountUpdateListener() {
            @Override
            public void onViewClick(MinecraftAccount account) {
                selectAccountListener.onSelect(account);
            }

            @Override
            public void onRefresh(MinecraftAccount account) {
                if (!isTaskRunning) {
                    accountsManager.forcedLogin(account);
                } else {
                    Toast.makeText(getContext(), R.string.tasks_ongoing, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }

            @Override
            public void onDelete(MinecraftAccount account) {
                new TipDialog.Builder(getContext())
                        .setMessage(R.string.warning_remove_account)
                        .setConfirm(R.string.global_delete)
                        .setConfirmClickListener(() -> {
                            File accountFile = new File(Tools.DIR_ACCOUNT_NEW, account.username + ".json");
                            File userIconFile = new File(ZHTools.DIR_USER_ICON, account.username + ".png");
                            if (accountFile.exists()) FileUtils.deleteQuietly(accountFile);
                            if (userIconFile.exists()) FileUtils.deleteQuietly(userIconFile);
                            refresh();
                        }).buildDialog();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getContext(), R.anim.fade_downwards)));
        recyclerView.setAdapter(accountAdapter);

        refresh();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAccounts() {
        mData.clear();
        accountsManager.reload();
        mData.addAll(accountsManager.getAllAccount());
        mData.add(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refresh() {
        loadAccounts();

        accountAdapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        isTaskRunning = taskCount != 0;
    }

    public interface DialogDismissListener {
        void onDismiss();
    }
}
