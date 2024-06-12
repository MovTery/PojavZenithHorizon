package com.movtery.ui.subassembly.collapsibleexpandlist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CollapsibleExpandItemBean {
    private String title;
    private RecyclerView.Adapter<?> adapter;
    private boolean unfold;

    public CollapsibleExpandItemBean(String title, RecyclerView.Adapter<?> adapter) {
        this.title = title;
        this.adapter = adapter;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
    }

    public boolean isUnfold() {
        return unfold;
    }

    public void setUnfold(boolean unfold) {
        this.unfold = unfold;
    }

    @NonNull
    @Override
    public String toString() {
        return "CollapsibleExpandItemBean{" +
                "title='" + title + '\'' +
                ", adapter=" + adapter +
                ", unfold=" + unfold +
                '}';
    }
}
