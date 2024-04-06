package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.getLastUpdateTime;
import static net.kdt.pojavlaunch.PojavZHTools.getVersionCode;
import static net.kdt.pojavlaunch.PojavZHTools.getVersionName;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
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
    private Button mReturnButton, mGithubButton, mPojavLauncherButton, mLicenseButton;
    private TextView mContributors1, mContributors2;

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

        mPojavLauncherButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://github.com/PojavLauncherTeam/PojavLauncher"));
        mLicenseButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://www.gnu.org/licenses/gpl-3.0.html"));

        mContributors1.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://space.bilibili.com/2008204513"));
        mContributors2.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://space.bilibili.com/1412062866"));
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_about_return_button);
        mGithubButton = view.findViewById(R.id.zh_about_github_button);
        mPojavLauncherButton = view.findViewById(R.id.zh_about_pojavlauncher_button);
        mLicenseButton = view.findViewById(R.id.zh_about_license_button);

        mContributors1 = view.findViewById(R.id.zh_about_contributors1);
        mContributors2 = view.findViewById(R.id.zh_about_contributors2);
        TextView mVersionName = view.findViewById(R.id.zh_about_version_name);
        TextView mVersionCode = view.findViewById(R.id.zh_about_version_code);
        TextView mLastUpdateTime = view.findViewById(R.id.zh_about_last_update_time);

        SpannableString spannableString1 = new SpannableString(mContributors1.getText().toString());
        spannableString1.setSpan(new UnderlineSpan(), 0, spannableString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mContributors1.setText(spannableString1);

        SpannableString spannableString2 = new SpannableString(mContributors2.getText().toString());
        spannableString2.setSpan(new UnderlineSpan(), 0, spannableString2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mContributors2.setText(spannableString2);

        //软件信息
        String versionName = getString(R.string.zh_about_version_name) + getVersionName(requireContext());
        mVersionName.setText(versionName);
        String versionCode = getString(R.string.zh_about_version_code) + getVersionCode(requireContext());
        mVersionCode.setText(versionCode);
        String lastUpdateTime = getString(R.string.zh_about_last_update_time) + getLastUpdateTime(requireContext());
        mLastUpdateTime.setText(lastUpdateTime);
    }
}

