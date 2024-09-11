package com.movtery.pojavzh.ui.fragment.settings

import android.view.View

class SettingsCategoryItem(val category: View) {
    private val subItems: MutableList<SettingsViewItem> = ArrayList()
    fun addSubView(item: SettingsViewItem) {
        subItems.add(item)
    }

    fun setVisibility(visibility: Boolean) {
        category.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}