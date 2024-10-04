package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.ui.fragment.settings.ControlSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.ExperimentalSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.JavaSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.LauncherSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.MiscellaneousSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.VideoSettingsFragment
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.FragmentSettingsBinding

class SettingsFragment : FragmentWithAnim(R.layout.fragment_settings) {
    companion object {
        const val TAG: String = "SettingsFragment"
    }

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var mButtons: Map<Int, View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        initViewPager()

        mButtons.forEach { (index, view) ->
            view.setOnClickListener { _: View ->
                binding.settingsViewpager.currentItem = index
            }
        }
    }

    private fun initViewPager() {
        binding.settingsViewpager.apply {
            adapter = ViewPagerAdapter(this@SettingsFragment)
            isUserInputEnabled = false
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
            setPageTransformer(MarginPageTransformer(Tools.dpToPx(12F).toInt()))
            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onFragmentSelect(position)
                }
            })
        }
    }

    private fun onFragmentSelect(position: Int) {
        binding.sideIndicator.apply { setSelectedView(mButtons[position], -Tools.dpToPx(3F).toInt()) }
    }

    private fun initViews() {
        mButtons = mapOf(
            0 to binding.videoSettings,
            1 to binding.controlsSettings,
            2 to binding.javaSettings,
            3 to binding.miscSettings,
            4 to binding.launcherSettings,
            5 to binding.experimentalSettings
        )
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.settingsLayout, Animations.BounceInRight))
            .apply(AnimPlayer.Entry(binding.settingsViewpager, Animations.BounceInDown))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.settingsLayout, Animations.FadeOutLeft))
            .apply(AnimPlayer.Entry(binding.settingsViewpager, Animations.FadeOutUp))
    }

    private class ViewPagerAdapter(val fragment: FragmentWithAnim): FragmentStateAdapter(fragment.requireActivity()) {
        override fun getItemCount(): Int = 6
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                1 -> ControlSettingsFragment(fragment)
                2 -> JavaSettingsFragment()
                3 -> MiscellaneousSettingsFragment()
                4 -> LauncherSettingsFragment(fragment)
                5 -> ExperimentalSettingsFragment()
                else -> VideoSettingsFragment()
            }
        }
    }
}