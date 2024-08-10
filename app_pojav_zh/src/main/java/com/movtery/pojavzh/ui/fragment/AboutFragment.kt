package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.feature.CheckSponsor
import com.movtery.pojavzh.feature.CheckSponsor.Companion.check
import com.movtery.pojavzh.feature.CheckSponsor.Companion.getSponsorData
import com.movtery.pojavzh.ui.dialog.MoreSponsorDialog
import com.movtery.pojavzh.ui.subassembly.about.AboutItemBean
import com.movtery.pojavzh.ui.subassembly.about.AboutItemBean.AboutItemButtonBean
import com.movtery.pojavzh.ui.subassembly.about.AboutRecyclerAdapter
import com.movtery.pojavzh.ui.subassembly.about.SponsorItemBean
import com.movtery.pojavzh.ui.subassembly.about.SponsorRecyclerAdapter
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools

class AboutFragment : FragmentWithAnim(R.layout.fragment_about) {
    companion object {
        const val TAG: String = "AboutFragment"
    }

    private val mAboutData: MutableList<AboutItemBean> = ArrayList()
    private var mReturnButton: Button? = null
    private var mGithubButton: Button? = null
    private var mPojavLauncherButton: Button? = null
    private var mLicenseButton: Button? = null
    private var mSupportButton: Button? = null
    private var mSupportMoreButton: Button? = null
    private var mAboutRecyclerView: RecyclerView? = null
    private var mSponsorRecyclerView: RecyclerView? = null
    private var mInfoLayout: View? = null
    private var mOperateLayout: View? = null
    private var mAppTitleView: View? = null
    private var mSponsorView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        loadSponsorData()
        loadAboutData(requireContext().resources)

        mAppTitleView?.setOnClickListener { setViewAnim(mAppTitleView!!, Techniques.Pulse) }
        mReturnButton?.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        mGithubButton?.setOnClickListener { Tools.openURL(requireActivity(), PathAndUrlManager.URL_HOME) }
        mPojavLauncherButton?.setOnClickListener { Tools.openURL(requireActivity(), PathAndUrlManager.URL_GITHUB_POJAVLAUNCHER) }
        mLicenseButton?.setOnClickListener { Tools.openURL(requireActivity(), "https://www.gnu.org/licenses/gpl-3.0.html") }
        mSupportButton?.setOnClickListener { Tools.openURL(requireActivity(), PathAndUrlManager.URL_SUPPORT) }

        val aboutAdapter = AboutRecyclerAdapter(this.mAboutData)
        mAboutRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        mAboutRecyclerView?.isNestedScrollingEnabled = false //禁止滑动
        mAboutRecyclerView?.adapter = aboutAdapter

        slideInAnim(this)
    }

    private fun bindViews(view: View) {
        mInfoLayout = view.findViewById(R.id.info_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)
        mAppTitleView = view.findViewById(R.id.zh_about_title)

        mReturnButton = view.findViewById(R.id.zh_about_return_button)
        mGithubButton = view.findViewById(R.id.zh_about_github_button)
        mPojavLauncherButton = view.findViewById(R.id.zh_about_pojavlauncher_button)
        mLicenseButton = view.findViewById(R.id.zh_about_license_button)
        mSupportButton = view.findViewById(R.id.zh_about_support_development)
        mSupportMoreButton = view.findViewById(R.id.zh_about_sponsor_more)
        mAboutRecyclerView = view.findViewById(R.id.zh_about_about_recycler)
        mSponsorRecyclerView = view.findViewById(R.id.zh_about_sponsor_recycler)
        mSponsorView = view.findViewById(R.id.constraintLayout5)

        val mVersionInfo = view.findViewById<TextView>(R.id.zh_about_info)
        mVersionInfo.text = StringUtils.insertNewline(StringUtils.insertSpace(getString(R.string.zh_about_version_name), ZHTools.getVersionName()),
            StringUtils.insertSpace(getString(R.string.zh_about_version_code), ZHTools.getVersionCode()),
            StringUtils.insertSpace(getString(R.string.zh_about_last_update_time), ZHTools.getLastUpdateTime(requireContext())),
            StringUtils.insertSpace(getString(R.string.zh_about_version_status), ZHTools.getVersionStatus(requireContext())))
        mVersionInfo.setOnClickListener{ StringUtils.copyText("text", mVersionInfo.text.toString(), requireContext()) }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadAboutData(resources: Resources) {
        mAboutData.clear()

        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.ic_pojav_full, requireContext().theme),
                "PojavLauncherTeam",
                getString(R.string.zh_about_pojavlauncher_desc),
                AboutItemButtonBean(requireActivity(), "Github", PathAndUrlManager.URL_GITHUB_POJAVLAUNCHER)
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_movtery, requireContext().theme),
                "墨北MovTery",
                getString(R.string.zh_about_movtery_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.zh_about_access_space),
                    "https://space.bilibili.com/2008204513"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_verafirefly, requireContext().theme),
                "Vera-Firefly",
                getString(R.string.zh_about_verafirefly_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.zh_about_access_space),
                    "https://space.bilibili.com/1412062866"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_lingmuqiuzhu, requireContext().theme),
                "柃木湫竹",
                getString(R.string.zh_about_lingmuqiuzhu_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.zh_about_access_space),
                    "https://space.bilibili.com/515165764"
                )
            )
        )
    }

    private fun loadSponsorData() {
        check(requireContext(), object : CheckSponsor.CheckListener {
            override fun onFailure() { setSponsorVisible(false) }
            override fun onSuccessful(data: List<SponsorItemBean>?) { setSponsorVisible(true) }
        })
    }

    private fun setSponsorVisible(visible: Boolean) {
        Tools.runOnUiThread {
            try {
                mSponsorView?.visibility = if (visible) View.VISIBLE else View.GONE

                if (visible) {
                    setupSponsorRecyclerView()
                    mSupportMoreButton?.setOnClickListener {
                        getSponsorData()?.let { data ->
                            MoreSponsorDialog(requireContext(), data).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("setSponsorVisible", e.toString())
            }
        }
    }

    private fun setupSponsorRecyclerView() {
        val sponsorData = getSponsorData()?.take(6) ?: return
        val sponsorAdapter = SponsorRecyclerAdapter(sponsorData)

        mSponsorRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
            adapter = sponsorAdapter
        }
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mInfoLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))

        yoYos.add(setViewAnim(mReturnButton!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(mGithubButton!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(mSupportButton!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mInfoLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}

