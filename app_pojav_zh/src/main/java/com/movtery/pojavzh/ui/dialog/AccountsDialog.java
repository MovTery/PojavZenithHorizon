package com.movtery.pojavzh.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movtery.pojavzh.event.single.SelectAuthMethodEvent;
import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.ui.subassembly.account.AccountAdapter;
import com.movtery.pojavzh.ui.subassembly.account.SelectAccountListener;
import com.movtery.pojavzh.utils.PathAndUrlManager;

import net.kdt.pojavlaunch.PojavProfile;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.DialogSelectItemBinding;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccountsDialog extends FullScreenDialog implements TaskCountListener {
    private final AccountsManager accountsManager = AccountsManager.getInstance();
    private final List<MinecraftAccount> mData = new ArrayList<>();
    private final DialogDismissListener dismissListener;
    private final DialogSelectItemBinding binding = DialogSelectItemBinding.inflate(getLayoutInflater());
    private AccountAdapter accountAdapter;
    private boolean isTaskRunning = false;

    public AccountsDialog(@NonNull Context context, DialogDismissListener dismissListener) {
        super(context);

        ProgressKeeper.addTaskCountListener(this);
        this.setContentView(binding.getRoot());
        this.dismissListener = dismissListener;

        binding.titleView.setText(R.string.account_manager);
        binding.closeButton.setOnClickListener(v -> this.dismiss());

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
                if (!isTaskRunning) PojavProfile.setCurrentProfile(getContext(), account.username);
                else Tools.runOnUiThread(() -> Toast.makeText(getContext(), R.string.tasks_ongoing, Toast.LENGTH_SHORT).show());
            } else {
                EventBus.getDefault().post(new SelectAuthMethodEvent());
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
                    accountsManager.performLogin(account, true);
                } else {
                    Toast.makeText(getContext(), R.string.tasks_ongoing, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }

            @Override
            public void onDelete(MinecraftAccount account) {
                new TipDialog.Builder(getContext())
                        .setMessage(R.string.account_remove)
                        .setConfirm(R.string.generic_delete)
                        .setConfirmClickListener(() -> {
                            File accountFile = new File(PathAndUrlManager.DIR_ACCOUNT_NEW, account.username + ".json");
                            File userSkinFile = new File(PathAndUrlManager.DIR_USER_SKIN, account.username + ".png");
                            if (accountFile.exists()) FileUtils.deleteQuietly(accountFile);
                            if (userSkinFile.exists()) FileUtils.deleteQuietly(userSkinFile);
                            refresh();
                        }).buildDialog();
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getContext(), R.anim.fade_downwards)));
        binding.recyclerView.setAdapter(accountAdapter);

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
        binding.recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        isTaskRunning = taskCount != 0;
    }

    public interface DialogDismissListener {
        void onDismiss();
    }
}
