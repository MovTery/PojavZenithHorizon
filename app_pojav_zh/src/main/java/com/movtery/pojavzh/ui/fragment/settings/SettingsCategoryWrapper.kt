package com.movtery.pojavzh.ui.fragment.settings

import android.view.View

class SettingsCategoryWrapper(val category: View) {
    private val subItems: MutableList<SettingsViewWrapper> = ArrayList()
    fun addSubView(item: SettingsViewWrapper) {
        subItems.add(item)
    }

    fun setVisibility(visibility: Boolean) {
        category.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}