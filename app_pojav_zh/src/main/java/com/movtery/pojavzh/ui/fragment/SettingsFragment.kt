package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.fragment.settings.ControlSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.ExperimentalSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.JavaSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.LauncherSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.MiscellaneousSettingsFragment
import com.movtery.pojavzh.ui.fragment.settings.VideoSettingsFragment
import com.movtery.pojavzh.ui.view.AnimSideIndicatorView
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

class SettingsFragment : FragmentWithAnim(R.layout.fragment_settings) {
    companion object {
        const val TAG: String = "SettingsFragment"
    }

    private var mSettingsLayout: View? = null
    private lateinit var mButtons: Map<Int, View>
    private var mSettingsViewpager: ViewPager2? = null
    private var mSideIndicator: AnimSideIndicatorView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        initViewPager()

        mButtons.forEach { (index, view) ->
            view.setOnClickListener { _: View ->
                mSettingsViewpager?.currentItem = index
            }
        }

        slideInAnim(this)
    }

    private fun initViewPager() {
        mSettingsViewpager?.apply {
            adapter = ViewPagerAdapter(requireActivity())
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
        mSideIndicator?.apply { setSelectedView(mButtons[position], -Tools.dpToPx(3F).toInt()) }
    }

    private fun bindViews(view: View) {
        mSettingsLayout = view.findViewById(R.id.scroll_settings_layout)
        mSettingsViewpager = view.findViewById(R.id.settings_viewpager)
        mSideIndicator = view.findViewById(R.id.side_indicator)

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
                1 -> ControlSettingsFragment()
                2 -> JavaSettingsFragment()
                3 -> MiscellaneousSettingsFragment()
                4 -> LauncherSettingsFragment()
                5 -> ExperimentalSettingsFragment()
                else -> VideoSettingsFragment()
            }
        }
    }
}