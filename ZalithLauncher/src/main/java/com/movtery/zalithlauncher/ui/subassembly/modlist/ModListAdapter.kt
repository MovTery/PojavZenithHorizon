package com.movtery.zalithlauncher.ui.subassembly.modlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.kdt.pojavlaunch.databinding.ItemModDownloadBinding

class ModListAdapter(
    private val fragment: ModListFragment,
    private val mData: MutableList<ModListItemBean>?
) : RecyclerView.Adapter<ModListAdapter.InnerHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerHolder {
        return InnerHolder(ItemModDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    inner class InnerHolder(private val binding: ItemModDownloadBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun setData(modListItemBean: ModListItemBean) {
            itemView.setOnClickListener {
                fragment.switchToChild(
                    modListItemBean.getAdapter(),
                    modListItemBean.title
                )
            }
            binding.modVersionId.text = modListItemBean.title
        }
    }
}
