package com.movtery.pojavzh.ui.subassembly.view

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.movtery.pojavzh.ui.subassembly.view.DraggableViewWrapper.AttributesFetcher
import com.movtery.pojavzh.ui.subassembly.view.DraggableViewWrapper.ScreenPixels
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.setVisibilityAnim
import net.kdt.pojavlaunch.R

class SearchViewWrapper(private val parentView: View, private val mainView: View) {
    private var mSearchEditText: EditText? = null
    private var searchListener: SearchListener? = null
    private var showSearchResultsListener: ShowSearchResultsListener? = null
    private var searchAsynchronousUpdatesListener: SearchAsynchronousUpdatesListener? = null

    init {
        init()
    }

    private fun init() {
        mSearchEditText = mainView.findViewById(R.id.zh_search_edit_text)
        val mSearchButton = mainView.findViewById<ImageButton>(R.id.zh_search_search_button)
        val mShowSearchResultsOnly = mainView.findViewById<CheckBox>(R.id.zh_search_show_search_results_only)
        val mCaseSensitive = mainView.findViewById<CheckBox>(R.id.zh_search_case_sensitive)
        val searchCountText = mainView.findViewById<TextView>(R.id.zh_search_text)

        mSearchButton.setOnClickListener {
            search(searchCountText, mCaseSensitive.isChecked)
        }
        mShowSearchResultsOnly.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (showSearchResultsListener != null) showSearchResultsListener?.onSearch(isChecked)
            if (mSearchEditText?.getText().toString().isNotEmpty()) search(searchCountText, mCaseSensitive.isChecked)
        }

        val draggableViewWrapper = DraggableViewWrapper(mainView, object : AttributesFetcher {
            override val screenPixels: ScreenPixels
                get() = ScreenPixels(0, 0, parentView.width - mainView.width,
                    parentView.height - mainView.height)

            override fun get(): IntArray {
                return intArrayOf(mainView.x.toInt(), mainView.y.toInt())
            }

            override fun set(x: Int, y: Int) {
                mainView.x = x.toFloat()
                mainView.y = y.toFloat()
            }
        })
        draggableViewWrapper.init()
    }

    private fun search(searchCountText: TextView, caseSensitive: Boolean) {
        val searchCount: Int
        val string = mSearchEditText!!.text.toString()
        if (searchListener != null) {
            searchCount = searchListener!!.onSearch(string, caseSensitive)
            searchCountText.text = searchCountText.context.getString(R.string.zh_search_count, searchCount)
            if (searchCount != 0) searchCountText.visibility = View.VISIBLE
        } else if (searchAsynchronousUpdatesListener != null) {
            searchAsynchronousUpdatesListener?.onSearch(searchCountText, string, caseSensitive)
        }
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

    fun isVisible() = mainView.visibility == View.VISIBLE

    fun setVisibility() {
        setVisibility(!isVisible())
    }

    fun setVisibility(visible: Boolean) {
        setVisibilityAnim(mainView, visible, 150)
    }

    fun close() {
        if (mainView.visibility != View.GONE) {
            setVisibilityAnim(mainView, false, 150)
        }
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
