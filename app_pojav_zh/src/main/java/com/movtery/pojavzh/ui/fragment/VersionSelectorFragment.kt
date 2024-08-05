package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.gameHome
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionListView
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionType
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.slideInAnim
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraCore
import java.io.File

class VersionSelectorFragment : FragmentWithAnim(R.layout.fragment_version) {
    companion object {
        const val TAG: String = "FileSelectorFragment"
    }

    private var mRefreshButton: Button? = null
    private var mReturnButton: Button? = null
    private var mVersionLayout: View? = null
    private var mOperateLayout: View? = null
    private var mVersionListView: VersionListView? = null
    private var mTabLayout: TabLayout? = null
    private var installed: TabLayout.Tab? = null
    private var release: TabLayout.Tab? = null
    private var snapshot: TabLayout.Tab? = null
    private var beta: TabLayout.Tab? = null
    private var alpha: TabLayout.Tab? = null
    private var versionType: VersionType? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        bindTab()

        refresh(mTabLayout?.getTabAt(mTabLayout!!.selectedTabPosition))

        mTabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                refresh(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        mRefreshButton?.setOnClickListener {
            refresh(mTabLayout?.getTabAt(mTabLayout!!.selectedTabPosition))
        }
        mReturnButton?.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

        mVersionListView?.setVersionSelectedListener(object : VersionSelectedListener() {
            override fun onVersionSelected(version: String?) {
                ExtraCore.setValue(ZHExtraConstants.VERSION_SELECTOR, version)
                ZHTools.onBackPressed(requireActivity())
            }
        })

        slideInAnim(this)
    }

    private fun refresh(tab: TabLayout.Tab?) {
        setVersionType(tab)

        val installedVersionsList = File("$gameHome/versions").list()
        //如果安装的版本列表为空，那么隐藏 已安装 按钮
        val hasInstalled = !(installedVersionsList == null || installedVersionsList.isEmpty())
        if (hasInstalled) {
            if (mTabLayout?.getTabAt(0) !== installed) mTabLayout?.addTab(installed!!, 0)
        } else {
            if (mTabLayout?.getTabAt(0) === installed) mTabLayout?.removeTab(installed!!)
        }

        mVersionListView?.setVersionType(versionType)
    }

    private fun setVersionType(tab: TabLayout.Tab?) {
        versionType = when (tab) {
            installed -> VersionType.INSTALLED
            release -> VersionType.RELEASE
            snapshot -> VersionType.SNAPSHOT
            beta -> VersionType.BETA
            alpha -> VersionType.ALPHA
            else -> VersionType.RELEASE
        }
    }

    private fun bindViews(view: View) {
        mVersionLayout = view.findViewById(R.id.version_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        mRefreshButton = view.findViewById(R.id.zh_version_refresh_button)
        mReturnButton = view.findViewById(R.id.zh_version_return_button)

        mTabLayout = view.findViewById(R.id.zh_version_tab)

        mVersionListView = view.findViewById(R.id.zh_version)
    }

    private fun bindTab() {
        installed = mTabLayout?.newTab()
        release = mTabLayout?.newTab()
        snapshot = mTabLayout?.newTab()
        beta = mTabLayout?.newTab()
        alpha = mTabLayout?.newTab()

        installed?.setText(getString(R.string.mcl_setting_veroption_installed))
        release?.setText(getString(R.string.mcl_setting_veroption_release))
        snapshot?.setText(getString(R.string.mcl_setting_veroption_snapshot))
        beta?.setText(getString(R.string.mcl_setting_veroption_oldbeta))
        alpha?.setText(getString(R.string.mcl_setting_veroption_oldalpha))

        mTabLayout?.addTab(installed!!)
        mTabLayout?.addTab(release!!)
        mTabLayout?.addTab(snapshot!!)
        mTabLayout?.addTab(beta!!)
        mTabLayout?.addTab(alpha!!)

        mTabLayout?.selectTab(release)
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mVersionLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))
        yoYos.add(setViewAnim(mRefreshButton!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(mReturnButton!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mVersionLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
