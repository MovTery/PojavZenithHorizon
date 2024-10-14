package com.movtery.pojavzh.ui.dialog

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlSelectedListener
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlsListViewCreator
import java.io.File

class SelectControlsDialog(context: Context) : AbstractSelectDialog(context) {
    private var controlsListViewCreator: ControlsListViewCreator? = null

    override fun initDialog(recyclerView: RecyclerView) {
        controlsListViewCreator = ControlsListViewCreator(context, recyclerView)
        controlsListViewCreator?.listAtPath()
    }

    fun setOnSelectedListener(selectedListener: SelectedListener) {
        controlsListViewCreator?.apply {
            setSelectedListener(object : ControlSelectedListener() {
                override fun onItemSelected(file: File) {
                    selectedListener.onSelected(file)
                    dismiss()
                }

                override fun onItemLongClick(file: File) {
                }
            })
        }
    }

    interface SelectedListener {
        fun onSelected(file: File)
    }
}
