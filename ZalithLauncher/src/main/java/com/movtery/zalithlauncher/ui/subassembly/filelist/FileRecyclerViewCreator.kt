package com.movtery.zalithlauncher.ui.subassembly.filelist

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.zalithlauncher.feature.mod.ModUtils
import com.movtery.zalithlauncher.ui.subassembly.filelist.FileRecyclerAdapter.OnMultiSelectListener
import com.movtery.zalithlauncher.utils.stringutils.StringFilter.Companion.containsSubstring
import net.kdt.pojavlaunch.R
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class FileRecyclerViewCreator(
    context: Context?,
    recyclerView: RecyclerView,
    onItemClickListener: FileRecyclerAdapter.OnItemClickListener?,
    onItemLongClickListener: FileRecyclerAdapter.OnItemLongClickListener?,
    private val mData: MutableList<FileItemBean>
) {
    @JvmField
    val fileRecyclerAdapter: FileRecyclerAdapter = FileRecyclerAdapter(this.mData)
    private val mainRecyclerView: RecyclerView

    init {
        fileRecyclerAdapter.setOnItemClickListener(onItemClickListener)
        fileRecyclerAdapter.setOnItemLongClickListener(onItemLongClickListener)

        this.mainRecyclerView = recyclerView

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        mainRecyclerView.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(
                context,
                R.anim.fade_downwards
            )
        )
        mainRecyclerView.layoutManager = layoutManager
        mainRecyclerView.adapter = this.fileRecyclerAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadData(itemBeans: List<FileItemBean>?) {
        mData.clear()
        mData.addAll(itemBeans!!)
        fileRecyclerAdapter.notifyDataSetChanged()
        mainRecyclerView.scheduleLayoutAnimation()
    }

    fun setOnMultiSelectListener(listener: OnMultiSelectListener?) {
        fileRecyclerAdapter.setOnMultiSelectListener(listener)
    }

    companion object {
        fun loadItemBeansFromPath(context: Context, path: File, fileIcon: FileIcon,
            showFile: Boolean, showFolder: Boolean
        ): MutableList<FileItemBean> {
            return loadItemBeansFromPath(
                context,
                null,
                showSearchResultsOnly = false,
                caseSensitive = false,
                null,
                path,
                fileIcon,
                showFile,
                showFolder
            )
        }

        @JvmStatic
        @SuppressLint("UseCompatLoadingForDrawables")
        fun loadItemBeansFromPath(
            context: Context,
            filterString: String?,
            showSearchResultsOnly: Boolean,
            caseSensitive: Boolean,
            searchCount: AtomicInteger?,
            path: File,
            fileIcon: FileIcon,
            showFile: Boolean,
            showFolder: Boolean
        ): MutableList<FileItemBean> {
            val itemBeans: MutableList<FileItemBean> = ArrayList()
            val files = path.listFiles()
            if (files != null) {
                val resources = context.resources
                for (file in files) {
                    if (!showFileOrFolder(file, showFile, showFolder)) continue

                    val itemBean = FileItemBean(file)
                    if (!filterString.isNullOrEmpty()) {
                        if (containsSubstring(file.name, filterString, caseSensitive)) {
                            itemBean.isHighlighted = true
                            searchCount?.addAndGet(1)
                        } else if (showSearchResultsOnly) {
                            continue
                        }
                    }
                    itemBean.image = getIcon(context, file, fileIcon, resources)
                    itemBeans.add(itemBean)
                }
            }
            return itemBeans
        }

        private fun showFileOrFolder(file: File, showFile: Boolean, showFolder: Boolean): Boolean {
            //显示文件与显示文件夹
            if (file.isDirectory && !showFolder) return false
            return !file.isFile || showFile
        }

        private fun getIcon(context: Context, file: File, fileIcon: FileIcon, resources: Resources): Drawable? {
            return if (file.isFile) {
                when (fileIcon) {
                    FileIcon.MOD -> if (file.name.endsWith(ModUtils.JAR_FILE_SUFFIX)) {
                        ContextCompat.getDrawable(context, R.drawable.ic_java)
                    } else if (file.name.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) {
                        ContextCompat.getDrawable(context, R.drawable.ic_disabled)
                    } else {
                        getFileIcon(file, resources)
                    }

                    FileIcon.FILE -> ContextCompat.getDrawable(context, R.drawable.ic_file)
                }
            } else {
                ContextCompat.getDrawable(context, R.drawable.ic_folder)
            }
        }

        @JvmStatic
        fun loadItemBean(drawable: Drawable?, names: Array<String>?): List<FileItemBean> {
            val itemBeans: MutableList<FileItemBean> = ArrayList()
            names?.apply {
                for (name in this) {
                    itemBeans.add(FileItemBean(name, drawable))
                }
            }
            return itemBeans
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun getFileIcon(file: File, resources: Resources): Drawable {
            return if (file.isDirectory) {
                resources.getDrawable(R.drawable.ic_folder, resources.newTheme())
            } else {
                resources.getDrawable(R.drawable.ic_file, resources.newTheme())
            }
        }
    }
}
