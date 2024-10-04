package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentProfileTypeBinding
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment

class ProfileTypeSelectFragment : FragmentWithAnim(R.layout.fragment_profile_type) {
    companion object {
        const val TAG = "ProfileTypeSelectFragment"
    }
    private lateinit var binding: FragmentProfileTypeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vanillaProfile.setOnClickListener { swapToFragment(ProfileEditorFragment::class.java, ProfileEditorFragment.TAG, Bundle(1)) }
            optifineProfile.setOnClickListener { swapToFragment(DownloadOptiFineFragment::class.java, DownloadOptiFineFragment.TAG) }
            moddedProfileFabric.setOnClickListener { swapToFragment(DownloadFabricFragment::class.java, DownloadFabricFragment.TAG) }
            moddedProfileForge.setOnClickListener { swapToFragment(DownloadForgeFragment::class.java, DownloadForgeFragment.TAG) }
            moddedProfileNeoforge.setOnClickListener { swapToFragment(DownloadNeoForgeFragment::class.java, DownloadNeoForgeFragment.TAG) }
            moddedProfileModpack.setOnClickListener { swapToFragment(SelectModPackFragment::class.java, SelectModPackFragment.TAG) }
            moddedProfileQuilt.setOnClickListener { swapToFragment(DownloadQuiltFragment::class.java, DownloadQuiltFragment.TAG) }
        }
    }

    private fun swapToFragment(fragmentClass: Class<out Fragment>, tag: String, bundle: Bundle? = null) {
        ZHTools.swapFragmentWithAnim(this, fragmentClass, tag, bundle)
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.vanillaLikeLayout, Animations.BounceInRight))
        .apply(AnimPlayer.Entry(binding.moddedVersionsLayout, Animations.BounceInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.vanillaLikeLayout, Animations.FadeOutLeft))
        .apply(AnimPlayer.Entry(binding.moddedVersionsLayout, Animations.FadeOutRight))
    }
}
