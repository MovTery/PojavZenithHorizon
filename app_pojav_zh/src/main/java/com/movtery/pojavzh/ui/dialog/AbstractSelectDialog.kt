package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.R

abstract class AbstractSelectDialog(context: Context) : FullScreenDialog(context) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleView: TextView
    private lateinit var messageView: TextView

    init {
        this.setContentView(R.layout.dialog_select_item)
        bindView()
        setupDialog()
    }

    private fun bindView() {
        recyclerView = findViewById(R.id.zh_select_view)
        titleView = findViewById(R.id.zh_select_item_title)
        messageView = findViewById(R.id.zh_select_item_message)
        findViewById<ImageButton>(R.id.zh_select_item_close_button).setOnClickListener { this.dismiss() }
    }

    private fun setupDialog() {
        initDialog(recyclerView, titleView, messageView)
    }

    abstract fun initDialog(recyclerView: RecyclerView, titleView: TextView, messageView: TextView)
}
