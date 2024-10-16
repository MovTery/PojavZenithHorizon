package com.movtery.pojavzh.ui.subassembly.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.accounts.AccountUtils;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.skin.SkinLoader;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.ItemAccountManagerBinding;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.IOException;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {
    private final List<MinecraftAccount> mData;
    private AccountUpdateListener accountUpdateListener;

    public AccountAdapter(List<MinecraftAccount> mData) {
        this.mData = mData;
    }

    @NonNull
    @Override
    public AccountAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountManagerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountAdapter.Holder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setAccountUpdateListener(AccountUpdateListener accountUpdateListener) {
        this.accountUpdateListener = accountUpdateListener;
    }

    public interface AccountUpdateListener {
        void onViewClick(MinecraftAccount account);

        void onRefresh(MinecraftAccount account);

        void onDelete(MinecraftAccount account);
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final Context mContext;
        private final ItemAccountManagerBinding binding;

        public Holder(@NonNull ItemAccountManagerBinding binding) {
            super(binding.getRoot());
            this.mContext = binding.getRoot().getContext();
            this.binding = binding;
        }

        public void setData(MinecraftAccount account) {
            if (accountUpdateListener != null) {
                itemView.setOnClickListener(v -> accountUpdateListener.onViewClick(account));
                binding.refresh.setOnClickListener(v -> accountUpdateListener.onRefresh(account));
                binding.delete.setOnClickListener(v -> accountUpdateListener.onDelete(account));
            }

            if (account != null) {
                binding.name.setText(account.username);

                int loginType;
                if (account.isMicrosoft) {
                    setButtonClickable(binding.refresh, true);
                    loginType = R.string.account_microsoft_account;
                } else if (AccountUtils.isOtherLoginAccount(account)) {
                    setButtonClickable(binding.refresh, true);
                    loginType = R.string.other_login;
                } else {
                    setButtonClickable(binding.refresh, false);
                    loginType = R.string.account_local_account;
                }

                try {
                    binding.icon.setImageDrawable(SkinLoader.getAvatarDrawable(mContext, account, (int) Tools.dpToPx(38f)));
                } catch (IOException e) {
                    Logging.e("AccountAdapter", Tools.printToString(e));
                }

                binding.loginType.setText(loginType);
            } else {
                binding.icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add));
                binding.name.setVisibility(View.GONE);
                binding.loginType.setVisibility(View.GONE);
                binding.add.setVisibility(View.VISIBLE);

                binding.refresh.setVisibility(View.GONE);
                binding.delete.setVisibility(View.GONE);
            }
        }

        private void setButtonClickable(ImageButton button, boolean clickable) {
            button.setAlpha(clickable ? 1.0f : 0.5f);
            button.setClickable(clickable);
        }
    }
}
