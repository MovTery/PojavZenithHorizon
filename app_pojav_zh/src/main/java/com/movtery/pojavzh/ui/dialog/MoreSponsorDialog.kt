package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.ui.subassembly.about.SponsorItemBean
import com.movtery.pojavzh.ui.subassembly.about.SponsorRecyclerAdapter
import net.kdt.pojavlaunch.R

class MoreSponsorDialog(private val mContext: Context, private val mData: List<SponsorItemBean>) :
    FullScreenDialog(mContext) {
    init {
        this.setCancelable(false)
        this.setContentView(R.layout.dialog_select_item)
        init()
    }

    private fun init() {
        val mRecyclerView = findViewById<RecyclerView>(R.id.zh_select_view)
        val mTitleText = findViewById<TextView>(R.id.zh_select_item_title)
        val mMessageText = findViewById<TextView>(R.id.zh_select_item_message)
        mTitleText.setText(R.string.zh_about_dec5)
        mMessageText.setText(R.string.zh_about_dec6)
        mMessageText.visibility = View.VISIBLE
        val mCloseButton = findViewById<ImageButton>(R.id.zh_select_item_close_button)

        mCloseButton.setOnClickListener { this.dismiss() }
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)

        mRecyclerView.adapter = SponsorRecyclerAdapter(mData)
    }
}
