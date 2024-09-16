package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.ui.subassembly.customcontrols.ControlsListViewCreator
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import java.io.File

class SelectControlsDialog(context: Context) : AbstractSelectDialog(context) {
    private var controlsListViewCreator: ControlsListViewCreator? = null

    override fun initDialog(
        recyclerView: RecyclerView,
        titleView: TextView,
        messageView: TextView
    ) {
        controlsListViewCreator = ControlsListViewCreator(context, recyclerView)
        controlsListViewCreator?.listAtPath()
    }

    fun setOnSelectedListener(controlSelectedListener: ControlSelectedListener) {
        controlsListViewCreator?.apply {
            setFileSelectedListener(object : FileSelectedListener() {
                override fun onFileSelected(file: File?, path: String?) {
                    controlSelectedListener.onSelectedListener(file)
                    dismiss()
                }

                override fun onItemLongClick(file: File?, path: String?) {
                }
            })
        }
    }

    interface ControlSelectedListener {
        fun onSelectedListener(file: File?)
    }
}
