package com.movtery.pojavzh.ui.subassembly.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.accounts.AccountUtils;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.skin.SkinLoader;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_manager, parent, false);
        return new Holder(view);
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
        private final View mMainView;
        private final ImageView mUserIcon;
        private final TextView mUserName, mUserLoginType, mUserAdd;
        private final ImageButton mRefreshButton, mDeleteButton;

        public Holder(@NonNull View itemView) {
            super(itemView);
            this.mContext = itemView.getContext();
            this.mMainView = itemView;

            this.mUserIcon = itemView.findViewById(R.id.user_icon);
            this.mUserName = itemView.findViewById(R.id.user_name);
            this.mUserLoginType = itemView.findViewById(R.id.user_login_type);
            this.mUserAdd = itemView.findViewById(R.id.user_add);
            this.mRefreshButton = itemView.findViewById(R.id.user_refresh);
            this.mDeleteButton = itemView.findViewById(R.id.user_delete);
        }

        public void setData(MinecraftAccount account) {
            if (accountUpdateListener != null) {
                mMainView.setOnClickListener(v -> accountUpdateListener.onViewClick(account));
                mRefreshButton.setOnClickListener(v -> accountUpdateListener.onRefresh(account));
                mDeleteButton.setOnClickListener(v -> accountUpdateListener.onDelete(account));
            }

            if (account != null) {
                mUserName.setText(account.username);

                int loginType;
                if (account.isMicrosoft) {
                    setButtonClickable(mRefreshButton, true);
                    loginType = R.string.zh_account_microsoft_account;
                } else if (AccountUtils.isOtherLoginAccount(account)) {
                    setButtonClickable(mRefreshButton, true);
                    loginType = R.string.zh_other_login_api;
                } else {
                    setButtonClickable(mRefreshButton, false);
                    loginType = R.string.zh_account_local_account;
                }

                try {
                    mUserIcon.setImageDrawable(SkinLoader.getAvatarDrawable(mContext, account, (int) Tools.dpToPx(38f)));
                } catch (IOException e) {
                    Logging.e("AccountAdapter", Tools.printToString(e));
                }

                mUserLoginType.setText(loginType);
            } else {
                mUserIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add));
                mUserName.setVisibility(View.GONE);
                mUserLoginType.setVisibility(View.GONE);
                mUserAdd.setVisibility(View.VISIBLE);

                mRefreshButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
            }
        }

        private void setButtonClickable(ImageButton button, boolean clickable) {
            button.setAlpha(clickable ? 1.0f : 0.5f);
            button.setClickable(clickable);
        }
    }
}
