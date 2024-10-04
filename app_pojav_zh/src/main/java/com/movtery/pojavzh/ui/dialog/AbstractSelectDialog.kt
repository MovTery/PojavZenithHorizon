package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.databinding.DialogSelectItemBinding

abstract class AbstractSelectDialog(context: Context) : FullScreenDialog(context) {
    private val binding = DialogSelectItemBinding.inflate(layoutInflater)

    init {
        this.setContentView(binding.root)
        binding.closeButton.setOnClickListener { this.dismiss() }
        setupDialog()
    }

    private fun setupDialog() {
        initDialog(binding.recyclerView, binding.titleView, binding.messageView)
    }

    abstract fun initDialog(recyclerView: RecyclerView, titleView: TextView, messageView: TextView)
}
