package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.login.AuthResult.AvailableProfiles
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.ItemFileListViewBinding

class SelectRoleDialog(context: Context, private val mProfiles: List<AvailableProfiles>) :
    AbstractSelectDialog(context) {
    private var selectedListener: RoleSelectedListener? = null

    override fun initDialog(recyclerView: RecyclerView) {
        setTitleText(R.string.other_login_select_role_title)
        setMessageText(R.string.other_login_select_role_message)

        val adapter = RoleAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    fun setOnSelectedListener(selectedListener: RoleSelectedListener?) {
        this.selectedListener = selectedListener
    }

    private inner class RoleAdapter : RecyclerView.Adapter<RoleAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setProfile(mProfiles[position])
        }

        override fun getItemCount(): Int {
            return mProfiles.size
        }

        inner class ViewHolder(private val binding: ItemFileListViewBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.image.visibility = View.GONE
                binding.check.visibility = View.GONE
            }

            fun setProfile(availableProfiles: AvailableProfiles) {
                val name = availableProfiles.name
                binding.name.text = name
                selectedListener?.apply {
                    itemView.setOnClickListener {
                        onSelectedListener(availableProfiles)
                        dismiss()
                    }
                }
            }
        }
    }

    fun interface RoleSelectedListener {
        fun onSelectedListener(profile: AvailableProfiles)
    }
}
