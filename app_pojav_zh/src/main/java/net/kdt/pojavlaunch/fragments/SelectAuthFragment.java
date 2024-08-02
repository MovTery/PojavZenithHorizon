package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.OtherLoginFragment;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.R;

public class SelectAuthFragment extends FragmentWithAnim {
    public static final String TAG = "AUTH_SELECT_FRAGMENT";
    private View mMainView;

    public SelectAuthFragment(){
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mMainView = view;
        ImageView mReturnButton = view.findViewById(R.id.zh_login_return);
        Button mMicrosoftButton = view.findViewById(R.id.button_microsoft_authentication);
        Button mLocalButton = view.findViewById(R.id.button_local_authentication);
        Button mOtherButton = view.findViewById(R.id.button_other_authentication);

        if (!ZHTools.areaChecks()) mOtherButton.setVisibility(View.GONE);

        FragmentWithAnim fragment = this;
        FragmentActivity fragmentActivity = requireActivity();
        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(fragmentActivity));
        mMicrosoftButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, MicrosoftLoginFragment.class, MicrosoftLoginFragment.TAG, null));
        mOtherButton.setOnClickListener(v -> LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed() {
                ZHTools.swapFragmentWithAnim(fragment, OtherLoginFragment.class, OtherLoginFragment.TAG, null);
            }

            @Override
            public void onUsageDenied() {
                if (!DEFAULT_PREF.getBoolean("localAccountReminders", true)) {
                    ZHTools.swapFragmentWithAnim(fragment, OtherLoginFragment.class, OtherLoginFragment.TAG, null);
                } else {
                    LocalAccountUtils.openDialog(fragmentActivity, () -> ZHTools.swapFragmentWithAnim(fragment, OtherLoginFragment.class, OtherLoginFragment.TAG, null),
                            getString(R.string.zh_account_no_microsoft_account_other) + getString(R.string.zh_account_purchase_minecraft_account_tip),
                            R.string.zh_account_no_microsoft_account_other_confirm);
                }
            }
        }));

        mLocalButton.setOnClickListener(v -> LocalAccountUtils.checkUsageAllowed(new LocalAccountUtils.CheckResultListener() {
            @Override
            public void onUsageAllowed() {
                ZHTools.swapFragmentWithAnim(fragment, LocalLoginFragment.class, LocalLoginFragment.TAG, null);
            }

            @Override
            public void onUsageDenied() {
                if (!DEFAULT_PREF.getBoolean("localAccountReminders", true)) {
                    ZHTools.swapFragmentWithAnim(fragment, LocalLoginFragment.class, LocalLoginFragment.TAG, null);
                } else {
                    LocalAccountUtils.openDialog(fragmentActivity, () -> ZHTools.swapFragmentWithAnim(fragment, LocalLoginFragment.class, LocalLoginFragment.TAG, null),
                            getString(R.string.zh_account_no_microsoft_account_local) + getString(R.string.zh_account_purchase_minecraft_account_tip),
                            R.string.zh_account_no_microsoft_account_local_confirm);
                }
            }
        }));

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        YoYo.YoYoString yoYoString = ViewAnimUtils.setViewAnim(mMainView, Techniques.BounceInDown);
        YoYo.YoYoString[] array = {yoYoString};
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        YoYo.YoYoString yoYoString = ViewAnimUtils.setViewAnim(mMainView, Techniques.FadeOutUp);
        YoYo.YoYoString[] array = {yoYoString};
        super.setYoYos(array);
        return array;
    }
}
