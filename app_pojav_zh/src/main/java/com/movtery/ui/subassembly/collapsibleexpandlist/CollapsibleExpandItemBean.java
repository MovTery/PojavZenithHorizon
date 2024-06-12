package com.movtery.ui.subassembly.collapsibleexpandlist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.atomic.AtomicReference;

public class CollapsibleExpandItemBean {
    private String title;
    private final AtomicReference<RecyclerView.Adapter<?>> adapter;
    private boolean unfold;

    public CollapsibleExpandItemBean(String title, RecyclerView.Adapter<?> adapter) {
        this.title = title;
        this.adapter = new AtomicReference<>(adapter);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter.get();
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter.set(adapter);
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
                ", adapter=" + adapter.get() +
                ", unfold=" + unfold +
                '}';
    }
}

