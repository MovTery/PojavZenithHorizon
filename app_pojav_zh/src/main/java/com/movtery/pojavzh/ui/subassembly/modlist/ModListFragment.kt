package com.movtery.pojavzh.ui.subassembly.modlist

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.AnimatorCallback
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.fragment.FragmentWithAnim
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.slideInAnim
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.R
import java.util.concurrent.Future

abstract class ModListFragment : FragmentWithAnim(R.layout.fragment_mod_download) {
    protected var fragmentActivity: FragmentActivity? = null
    private var parentAdapter: RecyclerView.Adapter<*>? = null
    protected var recyclerView: RecyclerView? = null
    private var mModsLayout: View? = null
    private var mOperateLayout: View? = null
    private var mLoadingView: View? = null
    private var mNameText: TextView? = null
    private var mSelectTitle: TextView? = null
    private var mFailedToLoad: TextView? = null
    private var mIcon: ImageView? = null
    private var mBackToTop: ImageButton? = null
    private var mReturnButton: Button? = null
    private var mRefreshButton: Button? = null
    protected var releaseCheckBox: CheckBox? = null
    protected var currentTask: Future<*>? = null
    private var releaseCheckBoxVisible = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        init()

        slideInAnim(this)
    }

    protected open fun init() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext())
        recyclerView?.layoutAnimation = LayoutAnimationController(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_downwards))
        recyclerView?.layoutManager = layoutManager

        mRefreshButton?.setOnClickListener { refreshTask() }
        releaseCheckBox?.setOnClickListener { refreshTask() }
        mReturnButton?.setOnClickListener {
            if (parentAdapter != null) {
                hideParentElement(false)
                recyclerView?.adapter = parentAdapter
                recyclerView?.scheduleLayoutAnimation()
                parentAdapter = null
            } else {
                ZHTools.onBackPressed(requireActivity())
            }
        }

        mBackToTop?.setOnClickListener { recyclerView?.smoothScrollToPosition(0) }

        refreshTask()
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

    private fun hideParentElement(visible: Boolean) {
        cancelTask() //中断当前正在执行的任务

        mRefreshButton?.isClickable = !visible
        releaseCheckBox?.isClickable = !visible

        setVisibilityAnim(mSelectTitle!!, visible)
        setVisibilityAnim(mRefreshButton!!, !visible)

        if (releaseCheckBoxVisible) setVisibilityAnim(releaseCheckBox!!, !visible)
    }

    private fun cancelTask() {
        if (currentTask != null && !currentTask!!.isDone) {
            currentTask?.cancel(true)
        }
    }

    private fun refreshTask() {
        currentTask = refresh()
    }

    protected abstract fun refresh(): Future<*>?

    protected fun componentProcessing(state: Boolean) {
        setVisibilityAnim(mLoadingView!!, state)
        recyclerView?.visibility = if (state) View.GONE else View.VISIBLE

        mRefreshButton?.isClickable = !state
        releaseCheckBox?.isClickable = !state
    }

    private fun bindViews(view: View) {
        mModsLayout = view.findViewById(R.id.mods_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        recyclerView = view.findViewById(R.id.zh_mod)
        mBackToTop = view.findViewById(R.id.zh_mod_back_to_top)
        mLoadingView = view.findViewById(R.id.zh_mod_loading)
        mIcon = view.findViewById(R.id.zh_mod_icon)
        mNameText = view.findViewById(R.id.zh_mod_name)
        mSelectTitle = view.findViewById(R.id.zh_select_title)
        mFailedToLoad = view.findViewById(R.id.zh_mod_failed_to_load)

        mReturnButton = view.findViewById(R.id.zh_mod_return_button)
        mRefreshButton = view.findViewById(R.id.zh_mod_refresh_button)
        releaseCheckBox = view.findViewById(R.id.zh_mod_release_version)

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val adapter = recyclerView.adapter
                if (layoutManager != null && adapter != null) {
                    val firstPosition = layoutManager.findFirstVisibleItemPosition()
                    val b = firstPosition >= adapter.itemCount / 3

                    AnimUtils.setVisibilityAnim(mBackToTop!!, b)
                }
            }
        })
    }

    protected fun setNameText(nameText: String?) {
        mNameText?.text = nameText
    }

    protected fun setIcon(icon: Drawable?) {
        mIcon?.setImageDrawable(icon)
    }

    protected fun setReleaseCheckBoxGone() {
        releaseCheckBoxVisible = false
        releaseCheckBox?.visibility = View.GONE
    }

    protected fun setFailedToLoad(reasons: String?) {
        val text = fragmentActivity!!.getString(R.string.modloader_dl_failed_to_load_list)
        mFailedToLoad?.text = if (reasons == null) text else StringUtils.insertNewline(text, reasons)
        setVisibilityAnim(mFailedToLoad!!, true)
    }

    protected fun cancelFailedToLoad() {
        setVisibilityAnim(mFailedToLoad!!, false)
    }

    private fun setVisibilityAnim(view: View, visible: Boolean) {
        setViewAnim(view, if (visible) Techniques.FadeIn else Techniques.FadeOut,
            AnimatorCallback { view.visibility = View.VISIBLE },
            AnimatorCallback { view.visibility = if (visible) View.VISIBLE else View.GONE })
    }

    fun switchToChild(adapter: RecyclerView.Adapter<*>?, title: String?) {
        if (currentTask!!.isDone && adapter != null) {
            //保存父级，设置选中的标题文本，切换至子级
            parentAdapter = recyclerView!!.adapter
            mSelectTitle?.text = title
            hideParentElement(true)
            recyclerView?.adapter = adapter
            recyclerView?.scheduleLayoutAnimation()
        }
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mModsLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))

        yoYos.add(setViewAnim(mIcon!!, Techniques.Wobble))
        yoYos.add(setViewAnim(mNameText!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(mReturnButton!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(mRefreshButton!!, Techniques.FadeInLeft))
        yoYos.add(setViewAnim(releaseCheckBox!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mModsLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
