package com.movtery.pojavzh.ui.subassembly.modlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.R

class ModListAdapter(
    private val fragment: ModListFragment,
    private val mData: MutableList<ModListItemBean>?
) : RecyclerView.Adapter<ModListAdapter.InnerHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mod_download, parent, false)
        return InnerHolder(view)
    }

    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.setData(mData!![position])
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<ModListItemBean>?) {
        mData?.clear()
        mData?.addAll(newData!!)
        super.notifyDataSetChanged()
    }

    val data: List<ModListItemBean>?
        get() = mData

    inner class InnerHolder(private val mainView: View) : RecyclerView.ViewHolder(
        mainView
    ) {
        private val versionId: TextView = itemView.findViewById(R.id.mod_version_id)

        fun setData(modListItemBean: ModListItemBean) {
            mainView.setOnClickListener {
                fragment.switchToChild(
                    modListItemBean.getAdapter(),
                    modListItemBean.title
                )
            }

            versionId.text = modListItemBean.title
        }
    }
}
