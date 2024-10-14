package com.movtery.pojavzh.feature.mod.modloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.utils.anim.ViewAnimUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.ItemFileListViewBinding
import net.kdt.pojavlaunch.modloaders.FabricVersion
import net.kdt.pojavlaunch.modloaders.OptiFineUtils.OptiFineVersion
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener

open class BaseModVersionListAdapter(private val mData: List<*>?) :
    RecyclerView.Adapter<BaseModVersionListAdapter.ViewHolder>(), TaskCountListener {
    private var mTasksRunning = false
    private var onItemClickListener: OnItemClickListener? = null
    private var iconDrawable = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(mData!![position])
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    open fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    fun setIconDrawable(iconDrawable: Int) {
        this.iconDrawable = iconDrawable
    }

    fun interface OnItemClickListener {
        fun onClick(version: Any?)
    }

    inner class ViewHolder(private val binding: ItemFileListViewBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = itemView.context

        init {
            if (iconDrawable != 0) binding.image.setImageResource(iconDrawable)
            binding.check.visibility = View.GONE
        }

        fun setView(version: Any?) {
            when (version) {
                is OptiFineVersion -> binding.name.text = version.versionName
                is FabricVersion -> binding.name.text = version.version
                is String -> binding.name.text = version
            }
            itemView.setOnClickListener { _: View? ->
                if (mTasksRunning) {
                    ViewAnimUtils.setViewAnim(itemView, Animations.Shake)
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                onItemClickListener?.onClick(version)
            }
        }
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }
}
