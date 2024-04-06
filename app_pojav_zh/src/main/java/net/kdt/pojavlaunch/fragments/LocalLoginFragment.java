package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class LocalLoginFragment extends Fragment {
    public static final String TAG = "LOCAL_LOGIN_FRAGMENT";

    private EditText mUsernameEditText;

    public LocalLoginFragment(){
        super(R.layout.fragment_local_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUsernameEditText = view.findViewById(R.id.login_edit_name);
        view.findViewById(R.id.login_button).setOnClickListener(v -> {
            if(!checkEditText()) return;

            ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, new String[]{
                    mUsernameEditText.getText().toString(), "" });

            Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
        });
    }


    /** @return Whether the mail (and password) text are eligible to make an auth request  */
    private boolean checkEditText(){

        String text = mUsernameEditText.getText().toString();

        if (text.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.zh_account_local_account_empty), Toast.LENGTH_SHORT).show());
            return false;
        } else if (text.length() < 3) {
            runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.zh_account_local_account_less), Toast.LENGTH_SHORT).show());
            return false;
        } else if (text.length() > 16) {
            runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.zh_account_local_account_greater), Toast.LENGTH_SHORT).show());
            return false;
        } else if (!text.matches("\\w+")) {
            runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.zh_account_local_account_illegal), Toast.LENGTH_SHORT).show());
            return false;
        }

        return !(new File(Tools.DIR_ACCOUNT_NEW + "/" + text + ".json").exists());
    }
}
