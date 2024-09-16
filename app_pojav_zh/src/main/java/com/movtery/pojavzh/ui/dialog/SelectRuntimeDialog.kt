package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.multirt.MultiRTUtils
import net.kdt.pojavlaunch.multirt.RTRecyclerViewAdapter
import net.kdt.pojavlaunch.multirt.Runtime

class SelectRuntimeDialog(context: Context) : AbstractSelectDialog(context) {
    private var recyclerView: RecyclerView? = null

    init {
        this.setCancelable(false)
    }

    override fun initDialog(
        recyclerView: RecyclerView,
        titleView: TextView,
        messageView: TextView
    ) {
        this.recyclerView = recyclerView
        titleView.setText(R.string.zh_install_select_jre_environment)
        messageView.setText(R.string.zh_install_recommend_use_jre8)
        messageView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun setListener(listener: RuntimeSelectedListener?) {
        val runtimes: MutableList<Runtime> = ArrayList(MultiRTUtils.getRuntimes())
        if (runtimes.isNotEmpty()) runtimes.add(Runtime("auto"))
        val adapter = RTRecyclerViewAdapter(runtimes, listener)
        recyclerView?.adapter = adapter
    }

    fun interface RuntimeSelectedListener {
        fun onSelected(jreName: String?)
    }
}
