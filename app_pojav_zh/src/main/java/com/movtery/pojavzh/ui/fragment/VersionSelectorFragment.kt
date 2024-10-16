package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.event.sticky.VersionSelectorEvent
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathHome.Companion.gameHome
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionSelectedListener
import com.movtery.pojavzh.ui.subassembly.versionlist.VersionType
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentVersionBinding
import org.greenrobot.eventbus.EventBus
import java.io.File

class VersionSelectorFragment : FragmentWithAnim(R.layout.fragment_version) {
    companion object {
        const val TAG: String = "FileSelectorFragment"
    }

    private lateinit var binding: FragmentVersionBinding
    private var installed: TabLayout.Tab? = null
    private var release: TabLayout.Tab? = null
    private var snapshot: TabLayout.Tab? = null
    private var beta: TabLayout.Tab? = null
    private var alpha: TabLayout.Tab? = null
    private var versionType: VersionType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVersionBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindTab()

        binding.apply {
            refresh(versionTab.getTabAt(versionTab.selectedTabPosition))

            versionTab.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    refresh(tab)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })

            refreshButton.setOnClickListener {
                refresh(versionTab.getTabAt(versionTab.selectedTabPosition))
            }
            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

            zhVersion.setVersionSelectedListener(object : VersionSelectedListener() {
                override fun onVersionSelected(version: String?) {
                    EventBus.getDefault().postSticky(VersionSelectorEvent(version))
                    ZHTools.onBackPressed(requireActivity())
                }
            })
        }
    }

    private fun refresh(tab: TabLayout.Tab?) {
        binding.apply {
            setVersionType(tab)

            val installedVersionsList = File("$gameHome/versions").list()
            //如果安装的版本列表为空，那么隐藏 已安装 按钮
            val hasInstalled = !(installedVersionsList == null || installedVersionsList.isEmpty())
            if (hasInstalled) {
                if (versionTab.getTabAt(0) !== installed) versionTab.addTab(installed!!, 0)
            } else {
                if (versionTab.getTabAt(0) === installed) versionTab.removeTab(installed!!)
            }

            zhVersion.setVersionType(versionType)
        }
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

    private fun bindTab() {
        binding.apply {
            installed = versionTab.newTab().setText(getString(R.string.version_installed))
            release = versionTab.newTab().setText(getString(R.string.version_release))
            snapshot = versionTab.newTab().setText(getString(R.string.version_snapshot))
            beta = versionTab.newTab().setText(getString(R.string.version_beta))
            alpha = versionTab.newTab().setText(getString(R.string.version_alpha))

            versionTab.addTab(installed!!)
            versionTab.addTab(release!!)
            versionTab.addTab(snapshot!!)
            versionTab.addTab(beta!!)
            versionTab.addTab(alpha!!)

            versionTab.selectTab(release)
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.versionLayout, Animations.BounceInDown))
            .apply(AnimPlayer.Entry(binding.operateLayout, Animations.BounceInLeft))
            .apply(AnimPlayer.Entry(binding.refreshButton, Animations.FadeInLeft))
            .apply(AnimPlayer.Entry(binding.returnButton, Animations.FadeInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.versionLayout, Animations.FadeOutUp))
            .apply(AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight))
    }
}
