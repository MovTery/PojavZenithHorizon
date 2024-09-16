package com.movtery.pojavzh.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movtery.pojavzh.feature.login.AuthResult.AvailableProfiles
import net.kdt.pojavlaunch.R

class SelectRoleDialog(context: Context, private val mProfiles: List<AvailableProfiles>) :
    AbstractSelectDialog(context) {
    private var selectedListener: RoleSelectedListener? = null

    override fun initDialog(
        recyclerView: RecyclerView,
        titleView: TextView,
        messageView: TextView
    ) {
        titleView.setText(R.string.zh_other_login_select_role_title)
        messageView.setText(R.string.zh_other_login_select_role_message)
        messageView.visibility = View.VISIBLE

        val adapter = RoleAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    fun setOnSelectedListener(selectedListener: RoleSelectedListener?) {
        this.selectedListener = selectedListener
    }

    private inner class RoleAdapter : RecyclerView.Adapter<RoleAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file_list_view, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setProfile(mProfiles[position])
        }

        override fun getItemCount(): Int {
            return mProfiles.size
        }

        inner class ViewHolder(private val mMainView: View) : RecyclerView.ViewHolder(mMainView) {
            private val mNameView: TextView

            init {
                itemView.findViewById<View>(R.id.zh_file_image).visibility = View.GONE
                itemView.findViewById<View>(R.id.zh_file_check).visibility = View.GONE
                mNameView = itemView.findViewById(R.id.zh_file_name)
            }

            fun setProfile(availableProfiles: AvailableProfiles) {
                val name = availableProfiles.name
                mNameView.text = name
                selectedListener?.apply {
                    mMainView.setOnClickListener {
                        onSelectedListener(availableProfiles)
                        dismiss()
                    }
                }
            }
        }
    }

    interface RoleSelectedListener {
        fun onSelectedListener(profile: AvailableProfiles)
    }
}
