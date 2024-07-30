package com.movtery.pojavzh.ui.subassembly.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewModel : ViewModel() {
    @JvmField
    @SuppressLint("StaticFieldLeak")
    var view: RecyclerView? = null
}
