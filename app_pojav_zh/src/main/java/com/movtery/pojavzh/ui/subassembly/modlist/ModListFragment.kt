package com.movtery.pojavzh.ui.subassembly.modlist

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.setting.AllSettings
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.playVisibilityAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.FragmentModDownloadBinding
import java.util.concurrent.Future

abstract class ModListFragment : FragmentWithAnim(R.layout.fragment_mod_download) {
    private lateinit var binding: FragmentModDownloadBinding
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var releaseCheckBox: CheckBox
    protected var fragmentActivity: FragmentActivity? = null
    private var parentAdapter: RecyclerView.Adapter<*>? = null
    protected var currentTask: Future<*>? = null
    private var releaseCheckBoxVisible = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentModDownloadBinding.inflate(layoutInflater)
        recyclerView = binding.recyclerView
        releaseCheckBox = binding.releaseVersion
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (layoutManager != null && recyclerView.adapter != null) {
                    val lastPosition = layoutManager.findFirstVisibleItemPosition()
                    val b = lastPosition >= 12

                    AnimUtils.setVisibilityAnim(binding.backToTop, b)
                }
            }
        })

        init()
    }

    protected open fun init() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        binding.apply {
            recyclerView.layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards))
            recyclerView.layoutManager = layoutManager

            refreshButton.setOnClickListener { refreshTask() }
            releaseVersion.setOnClickListener { initRefresh() }
            returnButton.setOnClickListener {
                parentAdapter?.apply {
                    hideParentElement(false)
                    recyclerView.adapter = this
                    recyclerView.scheduleLayoutAnimation()
                    parentAdapter = null
                    return@setOnClickListener
                }
                ZHTools.onBackPressed(requireActivity())
            }

            backToTop.setOnClickListener { recyclerView.smoothScrollToPosition(0) }
        }

        currentTask = initRefresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.fragmentActivity = requireActivity()
    }

    override fun onPause() {
        cancelTask()
        super.onPause()
    }

    override fun onDestroy() {
        cancelTask()
        super.onDestroy()
    }

    private fun hideParentElement(hide: Boolean) {
        cancelTask()

        binding.apply {
            refreshButton.isClickable = !hide
            releaseVersion.isClickable = !hide

            setViewAnim(selectTitle, if (hide) Animations.FadeIn else Animations.FadeOut, (AllSettings.animationSpeed * 0.7).toLong())
            setViewAnim(refreshButton, if (hide) Animations.FadeOut else Animations.FadeIn, (AllSettings.animationSpeed * 0.7).toLong())
            if (releaseCheckBoxVisible) setViewAnim(releaseVersion, if (hide) Animations.FadeOut else Animations.FadeIn, (AllSettings.animationSpeed * 0.7).toLong())
        }
    }

    private fun cancelTask() {
        currentTask?.apply { if (!isDone) cancel(true) }
    }

    private fun refreshTask() {
        currentTask = refresh()
    }

    protected abstract fun initRefresh(): Future<*>?
    protected abstract fun refresh(): Future<*>?

    protected fun componentProcessing(state: Boolean) {
        binding.apply {
            playVisibilityAnim(loadingLayout, state)
            recyclerView.visibility = if (state) View.GONE else View.VISIBLE
            refreshButton.isClickable = !state
            releaseVersion.isClickable = !state
        }
    }

    protected fun setNameText(nameText: String?) {
        binding.name.text = nameText
    }

    protected fun setSubTitleText(text: String?) {
        binding.subtitle.apply {
            visibility = if (text != null) View.VISIBLE else View.GONE
            text?.let { this.text = it }
        }
    }

    protected fun setIcon(icon: Drawable?) {
        binding.icon.setImageDrawable(icon)
    }

    protected fun setReleaseCheckBoxGone() {
        releaseCheckBoxVisible = false
        binding.releaseVersion.visibility = View.GONE
    }

    protected fun setFailedToLoad(reasons: String?) {
        val text = fragmentActivity!!.getString(R.string.mod_failed_to_load_list)
        binding.failedToLoad.text = if (reasons == null) text else StringUtils.insertNewline(text, reasons)
        playVisibilityAnim(binding.failedToLoad, true)
    }

    protected fun cancelFailedToLoad() {
        playVisibilityAnim(binding.failedToLoad, false)
    }

    protected fun setLink(link: String?) {
        if (link == null) return
        binding.launchLink.let { view ->
            view.setOnClickListener { Tools.openURL(fragmentActivity, link) }
            AnimUtils.setVisibilityAnim(view, true)
        }
    }

    fun switchToChild(adapter: RecyclerView.Adapter<*>?, title: String?) {
        if (currentTask!!.isDone && adapter != null) {
            binding.apply {
                //保存父级，设置选中的标题文本，切换至子级
                parentAdapter = recyclerView.adapter
                selectTitle.text = title
                hideParentElement(true)
                recyclerView.adapter = adapter
                recyclerView.scheduleLayoutAnimation()
            }
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(modsLayout, Animations.BounceInDown))
                .apply(AnimPlayer.Entry(operateLayout, Animations.BounceInLeft))
                .apply(AnimPlayer.Entry(icon, Animations.Wobble))
                .apply(AnimPlayer.Entry(modTitleLayout, Animations.FadeInLeft))
                .apply(AnimPlayer.Entry(returnButton, Animations.FadeInLeft))
                .apply(AnimPlayer.Entry(refreshButton, Animations.FadeInLeft))
                .apply(AnimPlayer.Entry(releaseVersion, Animations.FadeInLeft))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.modsLayout, Animations.FadeOutUp))
            .apply(AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight))
    }
}
