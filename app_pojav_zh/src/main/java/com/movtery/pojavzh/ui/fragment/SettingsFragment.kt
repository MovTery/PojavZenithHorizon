package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.fragment.preference.PreferenceExperimentalFragment
import com.movtery.pojavzh.ui.fragment.preference.PreferenceLauncherFragment
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment
import java.util.Objects

class SettingsFragment : FragmentWithAnim(R.layout.fragment_settings) {
    companion object {
        const val TAG: String = "SettingsFragment"
    }

    private var mSettingsLayout: View? = null
    private lateinit var mButtons: Map<Int, View>
    private var mSettingsViewpager: ViewPager2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        initViewPager()

        mButtons.forEach { (index, view) ->
            view.setOnClickListener { _: View ->
                mSettingsViewpager?.setCurrentItem(index, true)
            }
        }

        slideInAnim(this)
    }

    private fun initViewPager() {
        mSettingsViewpager?.apply {
            adapter = ViewPagerAdapter(requireActivity())
            isUserInputEnabled = false
            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onFragmentSelect(position)
                }
            })
        }
    }

    private fun onFragmentSelect(position: Int) {
        val selectedView = mButtons[position]
        mButtons.forEach { (_, view) -> setAnim(selectedView!!, view) }
    }

    private fun setAnim(clickedView: View, view: View) {
        view.animate()
            .alpha(if (Objects.equals(clickedView, view)) 0.4f else 1f)
            .setDuration(250)
    }

    private fun bindViews(view: View) {
        mSettingsLayout = view.findViewById(R.id.scroll_settings_layout)
        mSettingsViewpager = view.findViewById(R.id.settings_viewpager)

        mButtons = mapOf(
            0 to view.findViewById(R.id.video_settings),
            1 to view.findViewById(R.id.controls_settings),
            2 to view.findViewById(R.id.java_settings),
            3 to view.findViewById(R.id.misc_settings),
            4 to view.findViewById(R.id.launcher_settings),
            5 to view.findViewById(R.id.experimental_settings)
        )
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mSettingsLayout!!, Techniques.BounceInRight))
        yoYos.add(setViewAnim(mSettingsViewpager!!, Techniques.BounceInDown))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mSettingsLayout!!, Techniques.FadeOutLeft))
        yoYos.add(setViewAnim(mSettingsViewpager!!, Techniques.FadeOutUp))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    private class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 6
        override fun createFragment(position: Int): Fragment {
            return when(position) {
                1 -> LauncherPreferenceControlFragment()
                2 -> LauncherPreferenceJavaFragment()
                3 -> LauncherPreferenceMiscellaneousFragment()
                4 -> PreferenceLauncherFragment()
                5 -> PreferenceExperimentalFragment()
                else -> LauncherPreferenceVideoFragment()
            }
        }
    }
}