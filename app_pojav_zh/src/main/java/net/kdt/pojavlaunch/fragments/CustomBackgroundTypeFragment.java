package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;

public class CustomBackgroundTypeFragment extends Fragment {
    public static final String TAG = "CustomBackgroundTypeFragment";
    private Button mReturnButton, mMainMenuButton, mSettingsButton, mControlsButton, mInGameButton;

    public CustomBackgroundTypeFragment() {
        super(R.layout.fragment_custom_background_type);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());

        mMainMenuButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt(CustomBackgroundFragment.BUNDLE_BACKGROUND_TYPE, 1);
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, bundle, true);
        });
        mSettingsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt(CustomBackgroundFragment.BUNDLE_BACKGROUND_TYPE, 2);
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, bundle, true);
        });
        mControlsButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt(CustomBackgroundFragment.BUNDLE_BACKGROUND_TYPE, 3);
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, bundle, true);
        });
        mInGameButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt(CustomBackgroundFragment.BUNDLE_BACKGROUND_TYPE, 4);
            PojavZHTools.swapSettingsFragment(requireActivity(), CustomBackgroundFragment.class, CustomBackgroundFragment.TAG, bundle, true);
        });
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_custom_background_return);
        mMainMenuButton = view.findViewById(R.id.zh_custom_background_main_menu);
        mSettingsButton = view.findViewById(R.id.zh_custom_background_settings);
        mControlsButton = view.findViewById(R.id.zh_custom_background_controls);
        mInGameButton = view.findViewById(R.id.zh_custom_background_in_game);
    }
}
