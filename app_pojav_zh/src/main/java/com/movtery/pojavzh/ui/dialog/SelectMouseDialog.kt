package com.movtery.pojavzh.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.setting.Settings
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.Companion.isImage
import net.kdt.pojavlaunch.R
import java.io.File

class SelectMouseDialog(context: Context) : AbstractSelectDialog(context) {
    private var mouseSelectedListener: MouseSelectedListener? = null

    override fun initDialog(recyclerView: RecyclerView) {
        initView(recyclerView)
        setTitleText(R.string.custom_mouse_title)
    }

    private fun initView(mMouseListView: RecyclerView) {
        FileRecyclerViewCreator(
            context,
            mMouseListView,
            { position: Int, fileItemBean: FileItemBean ->
                val file = fileItemBean.file
                file?.apply {
                    if (exists() && isImage(this)) {
                        Settings.Manager.put("custom_mouse", name).save()
                        mouseSelectedListener!!.onSelectedListener()
                        dismiss()
                    }
                }
                if (position == 0) {
                    Settings.Manager.put("custom_mouse", null).save()
                    mouseSelectedListener!!.onSelectedListener()
                    this.dismiss()
                }
            },
            null,
            getItems()
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getItems(): MutableList<FileItemBean> {
        val fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(
            context,
            mousePath(),
            FileIcon.FILE,
            showFile = true,
            showFolder = false
        )
        fileItemBeans.add(0, FileItemBean(
            context.getString(R.string.custom_mouse_default),
            context.getDrawable(R.drawable.ic_mouse_pointer)
        ))
        return fileItemBeans
    }

    private fun mousePath(): File {
        val path = File(PathAndUrlManager.DIR_CUSTOM_MOUSE)
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
