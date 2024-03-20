package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

public class AboutFragment extends Fragment {
    public static final String TAG = "AboutFragment";
    public static final String BUNDLE_GITHUB_URI = "github_uri";
    private Button mReturnButton, mGithubButton;
    private TextView mContributors1, mContributors2;
    private String mGithubUri;

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        parseBundle();
        bindViews(view);

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mGithubButton.setOnClickListener(v -> Tools.openURL(requireActivity(), mGithubUri));

        mContributors1.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://space.bilibili.com/2008204513"));
        mContributors2.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://space.bilibili.com/1412062866"));
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mGithubUri = bundle.getString(BUNDLE_GITHUB_URI, mGithubUri);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mGithubButton = view.findViewById(R.id.zh_about_github_button);

        mContributors1 = view.findViewById(R.id.zh_about_contributors1);
        mContributors2 = view.findViewById(R.id.zh_about_contributors2);
    }
}

