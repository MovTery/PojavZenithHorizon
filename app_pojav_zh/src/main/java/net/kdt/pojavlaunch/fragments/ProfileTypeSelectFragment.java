package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.ui.fragment.DownloadFabricFragment;
import com.movtery.pojavzh.ui.fragment.DownloadQuiltFragment;
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim;
import com.movtery.pojavzh.ui.fragment.DownloadForgeFragment;
import com.movtery.pojavzh.ui.fragment.DownloadNeoForgeFragment;
import com.movtery.pojavzh.ui.fragment.DownloadOptiFineFragment;
import com.movtery.pojavzh.ui.fragment.SelectModPackFragment;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;

public class ProfileTypeSelectFragment extends FragmentWithAnim {
    public static final String TAG = "ProfileTypeSelectFragment";
    private View mVanillaLayout, mModdedLayout;

    public ProfileTypeSelectFragment() {
        super(R.layout.fragment_profile_type);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mVanillaLayout = view.findViewById(R.id.vanilla_like_layout);
        mModdedLayout = view.findViewById(R.id.modded_versions_layout);

        view.findViewById(R.id.vanilla_profile).setOnClickListener(v ->
                ZHTools.swapFragmentWithAnim(this, ProfileEditorFragment.class, ProfileEditorFragment.TAG, new Bundle(1)));
        view.findViewById(R.id.optifine_profile).setOnClickListener(v ->
                ZHTools.swapFragmentWithAnim(this, DownloadOptiFineFragment.class, DownloadOptiFineFragment.TAG, null));
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
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(mVanillaLayout, Animations.BounceInRight))
                .apply(new AnimPlayer.Entry(mModdedLayout, Animations.BounceInLeft));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(mVanillaLayout, Animations.FadeOutLeft))
                .apply(new AnimPlayer.Entry(mModdedLayout, Animations.FadeOutRight));
    }
}
