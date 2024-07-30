package com.movtery.pojavzh.feature.mod.modloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.OptiFineUtils.OptiFineVersion

open class ModVersionListAdapter(private val mData: List<*>?) :
    RecyclerView.Adapter<ModVersionListAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null
    private var iconDrawable = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(mData!![position])
    }

    override fun getItemCount(): Int {
        mData?.let {
            return mData.size
        }
        return 0
    }

    open fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    fun setIconDrawable(iconDrawable: Int) {
        this.iconDrawable = iconDrawable
    }

    interface OnItemClickListener {
        fun onClick(version: Any?)
    }

    inner class ViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val versionName: TextView

        init {
            val icon = itemView.findViewById<ImageView>(R.id.zh_file_image)
            versionName = itemView.findViewById(R.id.zh_file_name)

            if (iconDrawable != 0) icon.setImageResource(iconDrawable)
            itemView.findViewById<View>(R.id.zh_file_check).visibility = View.GONE
        }

        fun setView(version: Any?) {
            if (version is OptiFineVersion) {
                versionName.text = version.versionName
            } else if (version is String) {
                versionName.text = version
            }
            mainView.setOnClickListener { _: View? ->
                if (onItemClickListener != null) onItemClickListener!!.onClick(version)
            }
        }
    }
}
