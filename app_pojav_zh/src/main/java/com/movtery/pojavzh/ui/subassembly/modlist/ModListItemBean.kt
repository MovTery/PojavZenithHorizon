package com.movtery.pojavzh.ui.subassembly.modlist

import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.atomic.AtomicReference

class ModListItemBean(@JvmField val title: String, adapter: RecyclerView.Adapter<*>) {
    private val adapter = AtomicReference(adapter)

    fun getAdapter(): RecyclerView.Adapter<*> {
        return adapter.get()
    }

    override fun toString(): String {
        return "CollapsibleExpandItemBean{" +
                "title='" + title + '\'' +
                ", adapter=" + adapter +
                '}'
    }
}

