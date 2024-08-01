package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.movtery.pojavzh.ui.fragment.DownloadFabricFragment;
import com.movtery.pojavzh.ui.fragment.DownloadQuiltFragment;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.DownloadForgeFragment;
import com.movtery.pojavzh.ui.fragment.DownloadNeoForgeFragment;
import com.movtery.pojavzh.ui.fragment.DownloadOptiFineFragment;
import com.movtery.pojavzh.ui.fragment.SelectModPackFragment;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

public class ProfileTypeSelectFragment extends FragmentWithAnim {
    public static final String TAG = "ProfileTypeSelectFragment";
    private View mVanillaLayout, mModdedLayout;

    public ProfileTypeSelectFragment() {
        super(R.layout.fragment_profile_type);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVanillaLayout = view.findViewById(R.id.vanilla_like_layout);
        mModdedLayout = view.findViewById(R.id.modded_versions_layout);

        view.findViewById(R.id.vanilla_profile).setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ProfileEditorFragment.class,
                ProfileEditorFragment.TAG, new Bundle(1)));

        // NOTE: Special care needed! If you wll decide to add these to the back stack, please read
        // the comment in FabricInstallFragment.onDownloadFinished() and amend the code
        // in FabricInstallFragment.onDownloadFinished() and ModVersionListFragment.onDownloadFinished()
        view.findViewById(R.id.optifine_profile).setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, DownloadOptiFineFragment.class,
                DownloadOptiFineFragment.TAG, null));
        view.findViewById(R.id.modded_profile_fabric).setOnClickListener((v)->
                ZHTools.swapFragmentWithAnim(this, DownloadFabricFragment.class, DownloadFabricFragment.TAG, null));
        view.findViewById(R.id.modded_profile_forge).setOnClickListener((v)->
                ZHTools.swapFragmentWithAnim(this, DownloadForgeFragment.class, DownloadForgeFragment.TAG, null));
        view.findViewById(R.id.zh_modded_profile_neoforge).setOnClickListener((v)->
                ZHTools.swapFragmentWithAnim(this, DownloadNeoForgeFragment.class, DownloadNeoForgeFragment.TAG, null));
        view.findViewById(R.id.modded_profile_modpack).setOnClickListener((v)->
                ZHTools.swapFragmentWithAnim(this, SelectModPackFragment.class, SelectModPackFragment.TAG, null));
        view.findViewById(R.id.modded_profile_quilt).setOnClickListener((v)->
                ZHTools.swapFragmentWithAnim(this, DownloadQuiltFragment.class, DownloadQuiltFragment.TAG, null));

        ViewAnimUtils.slideInAnim(this);
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mVanillaLayout, Techniques.BounceInRight));
        yoYos.add(ViewAnimUtils.setViewAnim(mModdedLayout, Techniques.BounceInLeft));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        List<YoYo.YoYoString> yoYos = new ArrayList<>();
        yoYos.add(ViewAnimUtils.setViewAnim(mVanillaLayout, Techniques.FadeOutLeft));
        yoYos.add(ViewAnimUtils.setViewAnim(mModdedLayout, Techniques.FadeOutRight));
        YoYo.YoYoString[] array = yoYos.toArray(new YoYo.YoYoString[]{});
        super.setYoYos(array);
        return array;
    }
}
