package com.movtery.pojavzh.ui.fragment;

import static com.movtery.pojavzh.utils.ZHTools.getLastUpdateTime;
import static com.movtery.pojavzh.utils.ZHTools.getVersionCode;
import static com.movtery.pojavzh.utils.ZHTools.getVersionName;
import static com.movtery.pojavzh.utils.ZHTools.getVersionStatus;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.pojavzh.ui.subassembly.about.AboutItemBean;
import com.movtery.pojavzh.ui.subassembly.about.AboutRecyclerAdapter;

import com.movtery.pojavzh.utils.ZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends Fragment {
    public static final String TAG = "AboutFragment";
    private final List<AboutItemBean> mData = new ArrayList<>();
    private Button mReturnButton, mGithubButton, mPojavLauncherButton, mLicenseButton;
    private RecyclerView mAboutRecyclerView;

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        loadAboutData(requireContext().getResources());

        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mGithubButton.setOnClickListener(v -> Tools.openURL(requireActivity(), Tools.URL_HOME));

        mPojavLauncherButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://github.com/PojavLauncherTeam/PojavLauncher"));
        mLicenseButton.setOnClickListener(v -> Tools.openURL(requireActivity(), "https://www.gnu.org/licenses/gpl-3.0.html"));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        AboutRecyclerAdapter adapter = new AboutRecyclerAdapter(this.mData);
        mAboutRecyclerView.setLayoutManager(layoutManager);
        mAboutRecyclerView.setNestedScrollingEnabled(false); //禁止滑动
        mAboutRecyclerView.setAdapter(adapter);
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_about_return_button);
        mGithubButton = view.findViewById(R.id.zh_about_github_button);
        mPojavLauncherButton = view.findViewById(R.id.zh_about_pojavlauncher_button);
        mLicenseButton = view.findViewById(R.id.zh_about_license_button);
        mAboutRecyclerView = view.findViewById(R.id.zh_about_about_recycler);

        TextView mVersionName = view.findViewById(R.id.zh_about_version_name);
        TextView mVersionCode = view.findViewById(R.id.zh_about_version_code);
        TextView mLastUpdateTime = view.findViewById(R.id.zh_about_last_update_time);
        TextView mVersionStatus = view.findViewById(R.id.zh_about_version_status);

        //软件信息
        String versionName = getString(R.string.zh_about_version_name) + getVersionName(requireContext());
        mVersionName.setText(versionName);
        String versionCode = getString(R.string.zh_about_version_code) + getVersionCode(requireContext());
        mVersionCode.setText(versionCode);
        String lastUpdateTime = getString(R.string.zh_about_last_update_time) + getLastUpdateTime(requireContext());
        mLastUpdateTime.setText(lastUpdateTime);
        String versionStatus = getString(R.string.zh_about_version_status) + getVersionStatus(requireContext());
        mVersionStatus.setText(versionStatus);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadAboutData(Resources resources) {
        this.mData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_movtery, requireContext().getTheme()),
                "墨北MovTery",
                getString(R.string.zh_about_movtery_desc),
                new AboutItemBean.AboutItemButtonBean(requireActivity(), getString(R.string.zh_about_access_space), "https://space.bilibili.com/2008204513")));

        this.mData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_verafirefly, requireContext().getTheme()),
                "Vera-Firefly",
                getString(R.string.zh_about_verafirefly_desc),
                new AboutItemBean.AboutItemButtonBean(requireActivity(), getString(R.string.zh_about_access_space), "https://space.bilibili.com/1412062866")));

        this.mData.add(new AboutItemBean(
                resources.getDrawable(R.drawable.image_about_lingmuqiuzhu, requireContext().getTheme()),
                "柃木秋竹",
                getString(R.string.zh_about_lingmuqiuzhu_desc),
                null));
    }
}

