package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class AboutFragment extends Fragment {
    public static final String TAG = "AboutFragment";
    private Button mReturnButton, mGithubButton;

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mGithubButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mGithubButton = view.findViewById(R.id.zh_about_github_button);
    }
}

