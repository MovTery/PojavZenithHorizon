package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.event.value.LocalLoginEvent;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.FragmentLocalLoginBinding;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalLoginFragment extends FragmentWithAnim {
    public static final String TAG = "LOCAL_LOGIN_FRAGMENT";
    private FragmentLocalLoginBinding binding;

    public LocalLoginFragment(){
        super(R.layout.fragment_local_login);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocalLoginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.loginButton.setOnClickListener(v -> {
            String text = binding.loginEditName.getText().toString().trim();

            if (!checkEditText(text)) return;

            Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                new TipDialog.Builder(requireContext())
                        .setTitle(R.string.generic_warning)
                        .setMessage(R.string.account_local_account_invalid)
                        .setCenterMessage(false)
                        .setConfirmClickListener(this::startLogin)
                        .buildDialog();
            } else startLogin();
        });

        binding.returnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
    }

    /** @return Whether the mail (and password) text are eligible to make an auth request  */
    private boolean checkEditText(String text) {
        if (text.isBlank() || text.isEmpty()) {
            binding.loginEditName.setError(getString(R.string.account_local_account_empty));
            return false;
        }

        boolean exists = new File(PathAndUrlManager.DIR_ACCOUNT_NEW + "/" + text + ".json").exists();
        if (exists) {
            binding.loginEditName.setError(getString(R.string.account_local_account_exists));
        }
        return !(exists);
    }

    private void startLogin() {
        EventBus.getDefault().post(new LocalLoginEvent(binding.loginEditName.getText().toString().trim()));
        Tools.backToMainMenu(requireActivity());
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.getRoot(), Animations.BounceInDown));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.getRoot(), Animations.FadeOutUp));
    }
}
