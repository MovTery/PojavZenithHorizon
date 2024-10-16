package com.movtery.pojavzh.ui.dialog

import android.content.Context
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

    override fun initDialog(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        setTitleText(R.string.install_select_jre_environment)
        setMessageText(R.string.install_recommend_use_jre8)
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
