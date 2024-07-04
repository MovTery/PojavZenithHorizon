package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.movtery.pojavzh.feature.accounts.LocalAccountUtils;
import com.movtery.pojavzh.ui.fragment.OtherLoginFragment;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class SelectAuthFragment extends Fragment {
    public static final String TAG = "AUTH_SELECT_FRAGMENT";

    public SelectAuthFragment(){
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button mMicrosoftButton = view.findViewById(R.id.button_microsoft_authentication);
        Button mLocalButton = view.findViewById(R.id.button_local_authentication);
        Button mOtherButton = view.findViewById(R.id.button_other_authentication);

        if (!ZHTools.areaChecks()) mOtherButton.setVisibility(View.GONE);

        FragmentActivity fragmentActivity = requireActivity();
        mMicrosoftButton.setOnClickListener(v -> Tools.swapFragment(fragmentActivity, MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null));
        mOtherButton.setOnClickListener(v -> LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed(boolean b) {
                Tools.swapFragment(fragmentActivity, OtherLoginFragment.class, OtherLoginFragment.TAG, null);
            }

            @Override
            public void onUsageDenied(boolean b) {
                LocalAccountUtils.openDialog(fragmentActivity, () -> {
                            if (b) {
                                Tools.swapFragment(fragmentActivity, OtherLoginFragment.class, OtherLoginFragment.TAG, null);
                            } else {
                                Tools.openURL(fragmentActivity, ZHTools.URL_MINECRAFT);
                            }
                        }, () -> {
                            if (b) {
                                Tools.openURL(fragmentActivity, ZHTools.URL_MINECRAFT);
                            }
                        }, getString(R.string.zh_account_no_microsoft_account_other) + getString(R.string.zh_account_purchase_minecraft_account_tip),
                        b ? R.string.zh_account_no_microsoft_account_other_confirm : R.string.zh_confirm, b ? getString(R.string.zh_account_purchase_minecraft_account) : null);
            }
        }));

        mLocalButton.setOnClickListener(v -> LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed(boolean b) {
                Tools.swapFragment(fragmentActivity, LocalLoginFragment.class, LocalLoginFragment.TAG, null);
            }

            @Override
            public void onUsageDenied(boolean b) {
                LocalAccountUtils.openDialog(fragmentActivity, () -> {
                            if (b) {
                                Tools.swapFragment(fragmentActivity, LocalLoginFragment.class, LocalLoginFragment.TAG, null);
                            } else {
                                Tools.openURL(fragmentActivity, ZHTools.URL_MINECRAFT);
                            }
                        }, () -> {
                            if (b) {
                                Tools.openURL(fragmentActivity, ZHTools.URL_MINECRAFT);
                            }
                        }, getString(R.string.zh_account_no_microsoft_account_local) + getString(R.string.zh_account_purchase_minecraft_account_tip),
                        b ? R.string.zh_account_no_microsoft_account_local_confirm : R.string.zh_account_purchase_minecraft_account, b ? getString(R.string.zh_account_purchase_minecraft_account) : null);
            }
        }));
    }
}
