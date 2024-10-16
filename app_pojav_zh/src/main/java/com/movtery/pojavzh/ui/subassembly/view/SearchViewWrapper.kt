package com.movtery.pojavzh.ui.subassembly.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.petterp.floatingx.assist.FxAnimation
import com.petterp.floatingx.assist.FxGravity
import com.petterp.floatingx.listener.IFxViewLifecycle
import com.petterp.floatingx.util.createFx
import com.petterp.floatingx.view.FxViewHolder
import net.kdt.pojavlaunch.R

class SearchViewWrapper(private val fragment: Fragment) {
    private lateinit var mSearchEditText: EditText
    private var searchListener: SearchListener? = null
    private var showSearchResultsListener: ShowSearchResultsListener? = null
    private var searchAsynchronousUpdatesListener: SearchAsynchronousUpdatesListener? = null
    private var isShow = false

    private val scopeFx by createFx {
        setLayout(R.layout.view_search)
        setEnableEdgeAdsorption(false)
        addViewLifecycle(object : IFxViewLifecycle {
            override fun initView(holder: FxViewHolder) {
                mSearchEditText = holder.getView(R.id.zh_search_edit_text)
                val caseSensitive = holder.getView<CheckBox>(R.id.zh_search_case_sensitive)
                val searchCountText = holder.getView<TextView>(R.id.zh_search_text)

                holder.getView<ImageButton>(R.id.zh_search_search_button).setOnClickListener {
                    search(searchCountText, caseSensitive.isChecked)
                }
                holder.getView<CheckBox>(R.id.zh_search_show_search_results_only).setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    showSearchResultsListener?.apply { onSearch(isChecked) }
                    if (mSearchEditText.getText().toString().isNotEmpty()) search(searchCountText, caseSensitive.isChecked)
                }
            }
        })
        setGravity(FxGravity.TOP_OR_CENTER)
        setEnableAnimation(true)
        setAnimationImpl(object : FxAnimation() {
            override fun fromAnimator(view: FrameLayout?): Animator {
                return ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            }

            override fun toAnimator(view: FrameLayout?): Animator {
                return ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            }
        })
        build().toControl(fragment)
    }

    private fun search(searchCountText: TextView, caseSensitive: Boolean) {
        val searchCount: Int
        val string = mSearchEditText.text.toString()
        searchListener?.apply {
            searchCount = onSearch(string, caseSensitive)
            searchCountText.text = searchCountText.context.getString(R.string.search_count, searchCount)
            if (searchCount != 0) searchCountText.visibility = View.VISIBLE
            return
        }
        searchAsynchronousUpdatesListener?.apply { onSearch(searchCountText, string, caseSensitive) }
    }

    fun setSearchListener(listener: SearchListener?) {
        this.searchListener = listener
    }

    fun setAsynchronousUpdatesListener(listener: SearchAsynchronousUpdatesListener?) {
        this.searchAsynchronousUpdatesListener = listener
    }

    fun setShowSearchResultsListener(listener: ShowSearchResultsListener?) {
        this.showSearchResultsListener = listener
    }

    fun isVisible() = isShow

    fun setVisibility() {
        isShow = !isShow
        setVisibility(isShow)
    }

    fun setVisibility(visible: Boolean) {
        if (visible) scopeFx.show()
        else scopeFx.hide()
    }

    interface SearchListener {
        fun onSearch(string: String?, caseSensitive: Boolean): Int
    }

    interface SearchAsynchronousUpdatesListener {
        fun onSearch(searchCount: TextView?, string: String?, caseSensitive: Boolean)
    }

    interface ShowSearchResultsListener {
        fun onSearch(show: Boolean)
    }
}
