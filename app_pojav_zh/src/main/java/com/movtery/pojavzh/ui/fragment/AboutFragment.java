package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.ZHTools.getLastUpdateTime;
import static com.movtery.pojavzh.utils.ZHTools.getVersionCode;
import static com.movtery.pojavzh.utils.ZHTools.getVersionName;
import static com.movtery.pojavzh.utils.ZHTools.getVersionStatus;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.feature.CheckSponsor;
import com.movtery.pojavzh.ui.subassembly.about.AboutItemBean;
import com.movtery.pojavzh.ui.subassembly.about.AboutRecyclerAdapter;
import com.movtery.pojavzh.ui.subassembly.about.SponsorItemBean;
import com.movtery.pojavzh.ui.subassembly.about.SponsorRecyclerAdapter;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends Fragment {
    public static final String TAG = "AboutFragment";
    private final List<AboutItemBean> mAboutData = new ArrayList<>();
    private Button mReturnButton, mGithubButton, mPojavLauncherButton, mLicenseButton, mSupportButton;
    private RecyclerView mAboutRecyclerView, mSponsorRecyclerView;
    private View mSponsorView;

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        loadSponsorData();
        loadAboutData(requireContext().getResources());

        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mGithubButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));
        mPojavLauncherButton.setOnClickListener(v -> Tools.openURL(requireActivity(), ZHTools.URL_GITHUB_POJAVLAUNCHER));
        mLicenseButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://www.gnu.org/licenses/gpl-3.0.html"));
        mSupportButton.setOnClickListener(v -> Tools.openURL(requireActivity(), ZHTools.URL_SUPPORT));

        AboutRecyclerAdapter aboutAdapter = new AboutRecyclerAdapter(this.mAboutData);
        mAboutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAboutRecyclerView.setNestedScrollingEnabled(false); //禁止滑动
        mAboutRecyclerView.setAdapter(aboutAdapter);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_about_return_button);
        mGithubButton = view.findViewById(R.id.zh_about_github_button);
        mPojavLauncherButton = view.findViewById(R.id.zh_about_pojavlauncher_button);
        mLicenseButton = view.findViewById(R.id.zh_about_license_button);
        mSupportButton = view.findViewById(R.id.zh_about_support_development);
        mAboutRecyclerView = view.findViewById(R.id.zh_about_about_recycler);
        mSponsorRecyclerView = view.findViewById(R.id.zh_about_sponsor_recycler);
        mSponsorView = view.findViewById(R.id.constraintLayout5);

        TextView mVersionName = view.findViewById(R.id.zh_about_version_name);
        TextView mVersionCode = view.findViewById(R.id.zh_about_version_code);
        TextView mLastUpdateTime = view.findViewById(R.id.zh_about_last_update_time);
        TextView mVersionStatus = view.findViewById(R.id.zh_about_version_status);

        //软件信息
        String versionName = StringUtils.insertSpace(getString(R.string.zh_about_version_name), getVersionName(requireContext()));
        mVersionName.setText(versionName);
        String versionCode = StringUtils.insertSpace(getString(R.string.zh_about_version_code), getVersionCode(requireContext()));
        mVersionCode.setText(versionCode);
        String lastUpdateTime = StringUtils.insertSpace(getString(R.string.zh_about_last_update_time), getLastUpdateTime(requireContext()));
        mLastUpdateTime.setText(lastUpdateTime);
        String versionStatus = StringUtils.insertSpace(getString(R.string.zh_about_version_status), getVersionStatus(requireContext()));
        mVersionStatus.setText(versionStatus);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadAboutData(Resources resources) {
        this.mAboutData.clear();

        this.mAboutData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.ic_pojav_full, requireContext().getTheme()),
                "PojavLauncherTeam",
                getString(R.string.zh_about_pojavlauncher_desc),
                new AboutItemBean.AboutItemButtonBean(requireActivity(), "Github", ZHTools.URL_GITHUB_POJAVLAUNCHER)));

        this.mAboutData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_movtery, requireContext().getTheme()),
                "墨北MovTery",
                getString(R.string.zh_about_movtery_desc),
                new AboutItemBean.AboutItemButtonBean(requireActivity(), getString(R.string.zh_about_access_space), "https://space.bilibili.com/2008204513")));

        this.mAboutData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_verafirefly, requireContext().getTheme()),
                "Vera-Firefly",
                getString(R.string.zh_about_verafirefly_desc),
                new AboutItemBean.AboutItemButtonBean(requireActivity(), getString(R.string.zh_about_access_space), "https://space.bilibili.com/1412062866")));

        this.mAboutData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_lingmuqiuzhu, requireContext().getTheme()),
                "柃木湫竹",
                getString(R.string.zh_about_lingmuqiuzhu_desc),
                null));
    }

    private void loadSponsorData() {
        CheckSponsor.check(requireContext(), new CheckSponsor.CheckListener() {
            @Override
            public void onFailure() {
                setSponsorVisible(false);
            }

            @Override
            public void onSuccessful(@Nullable List<? extends SponsorItemBean> data) {
                setSponsorVisible(true);
            }
        });
    }

    private void setSponsorVisible(boolean visible) {
        Tools.runOnUiThread(() -> {
            try {
                mSponsorView.setVisibility(visible ? View.VISIBLE : View.GONE);

                if (visible) {
                    SponsorRecyclerAdapter sponsorAdapter = new SponsorRecyclerAdapter(CheckSponsor.getSponsorData());
                    mSponsorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    mSponsorRecyclerView.setNestedScrollingEnabled(false); //禁止滑动
                    mSponsorRecyclerView.setAdapter(sponsorAdapter);
                }
            } catch (Exception e) {
                Log.e("setSponsorVisible", e.toString());
            }
        });
    }
}

