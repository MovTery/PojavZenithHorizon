package com.movtery.pojavzh.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.Companion.isImage
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File

class SelectMouseDialog(context: Context) : AbstractSelectDialog(context) {
    private val mData: List<FileItemBean> = ArrayList()
    private var mouseSelectedListener: MouseSelectedListener? = null

    override fun initDialog(
        recyclerView: RecyclerView,
        titleView: TextView,
        messageView: TextView
    ) {
        initView(recyclerView)
        titleView.setText(R.string.zh_custom_mouse_title)
    }

    private fun initView(mMouseListView: RecyclerView) {
        val fileRecyclerViewCreator = FileRecyclerViewCreator(
            context,
            mMouseListView,
            { position: Int, fileItemBean: FileItemBean ->
                val file = fileItemBean.file
                file?.apply {
                    if (exists() && isImage(this)) {
                        LauncherPreferences.DEFAULT_PREF.edit().putString("custom_mouse", name).apply()
                        mouseSelectedListener!!.onSelectedListener()
                        dismiss()
                    }
                }
                if (position == 0) {
                    LauncherPreferences.DEFAULT_PREF.edit().putString("custom_mouse", null).apply()
                    mouseSelectedListener!!.onSelectedListener()
                    this.dismiss()
                }
            },
            null,
            mData
        )

        loadData(fileRecyclerViewCreator)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadData(fileRecyclerViewCreator: FileRecyclerViewCreator) {
        val fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(
            context,
            mousePath(),
            FileIcon.IMAGE,
            true,
            false
        )
        fileItemBeans.add(
            0,
            FileItemBean(
                context.getDrawable(R.drawable.ic_mouse_pointer),
                null,
                context.getString(R.string.zh_custom_mouse_default)
            )
        )
        Tools.runOnUiThread { fileRecyclerViewCreator.loadData(fileItemBeans) }
    }

    private fun mousePath(): File {
        val path = File(PathAndUrlManager.DIR_CUSTOM_MOUSE!!)
        if (!path.exists()) mkdirs(path)
        return path
    }

    fun setOnSelectedListener(mouseSelectedListener: MouseSelectedListener?) {
        this.mouseSelectedListener = mouseSelectedListener
    }

    interface MouseSelectedListener {
        fun onSelectedListener()
    }
}
