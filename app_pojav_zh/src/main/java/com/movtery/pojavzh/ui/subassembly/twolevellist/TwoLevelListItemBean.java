package com.movtery.pojavzh.ui.subassembly.twolevellist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.atomic.AtomicReference;

public class TwoLevelListItemBean {
    private final String title;
    private final AtomicReference<RecyclerView.Adapter<?>> adapter;

    public TwoLevelListItemBean(String title, RecyclerView.Adapter<?> adapter) {
        this.title = title;
        this.adapter = new AtomicReference<>(adapter);
    }

    public String getTitle() {
        return title;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter.get();
    }


    @NonNull
    @Override
    public String toString() {
        return "CollapsibleExpandItemBean{" +
                "title='" + title + '\'' +
                ", adapter=" + adapter +
                '}';
    }
}

