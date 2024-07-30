package com.movtery.pojavzh.ui.subassembly.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewModel extends ViewModel {
    @SuppressLint("StaticFieldLeak")
    private RecyclerView view;

    public RecyclerView getView() {
        return view;
    }

    public void setView(RecyclerView view) {
        this.view = view;
    }
}
