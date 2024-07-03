package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.pojavzh.feature.accounts.AccountsManager;
import com.movtery.pojavzh.ui.dialog.TipDialog;
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

        boolean areaChecks = ZHTools.areaChecks();
        if (!areaChecks) mOtherButton.setVisibility(View.GONE);

        mMicrosoftButton.setOnClickListener(v -> Tools.swapFragment(requireActivity(), MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null));
        mOtherButton.setOnClickListener(v -> {
            if (AccountsManager.haveMicrosoftAccount()) {
                Tools.swapFragment(requireActivity(), OtherLoginFragment.class, OtherLoginFragment.TAG, null);
            } else {
                new TipDialog.Builder(requireContext())
                        .setTitle(R.string.zh_warning)
                        .setMessage(R.string.zh_account_no_microsoft_account_other)
                        .setConfirmClickListener(() -> Tools.swapFragment(requireActivity(), OtherLoginFragment.class, OtherLoginFragment.TAG, null))
                        .setConfirm(R.string.zh_account_no_microsoft_account_other_confirm)
                        .buildDialog();
            }
        });
        mLocalButton.setOnClickListener(v -> {
            if (AccountsManager.haveMicrosoftAccount()) {
                Tools.swapFragment(requireActivity(), LocalLoginFragment.class, LocalLoginFragment.TAG, null);
            } else {
                new TipDialog.Builder(requireContext())
                        .setTitle(R.string.zh_warning)
                        .setMessage(R.string.zh_account_no_microsoft_account_local)
                        .setConfirmClickListener(() -> Tools.swapFragment(requireActivity(), LocalLoginFragment.class, LocalLoginFragment.TAG, null))
                        .setConfirm(R.string.zh_account_no_microsoft_account_local_confirm)
                        .buildDialog();
            }
        });
    }
}
