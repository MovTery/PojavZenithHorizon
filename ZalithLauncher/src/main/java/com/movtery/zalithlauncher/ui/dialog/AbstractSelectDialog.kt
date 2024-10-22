package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.databinding.DialogSelectItemBinding

abstract class AbstractSelectDialog(context: Context) : FullScreenDialog(context) {
    protected val binding = DialogSelectItemBinding.inflate(layoutInflater)

    init {
        this.setContentView(binding.root)
        binding.closeButton.setOnClickListener { this.dismiss() }
        setupDialog()
    }

    private fun setupDialog() {
        initDialog(binding.recyclerView)
    }

    fun setTitleText(text: Int) {
        setTitleText(context.getString(text))
    }

    fun setTitleText(text: String) {
        binding.titleView.text = text
    }

    fun setMessageText(text: Int) {
        setMessageText(context.getString(text))
    }

    fun setMessageText(text: String?) {
        binding.messageView.text = text
        binding.messageView.visibility = if (text != null) View.VISIBLE else View.GONE
    }

    abstract fun initDialog(recyclerView: RecyclerView)
}
