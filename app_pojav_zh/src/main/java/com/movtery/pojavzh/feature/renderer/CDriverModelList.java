package com.movtery.pojavzh.feature.renderer;

import com.movtery.pojavzh.utils.ListAndArray;

import java.util.List;

public class CDriverModelList implements ListAndArray {
    public final List<String> CDriverModelIds;
    public final String[] CDriverModels;

    public CDriverModelList(List<String> CDriverModelIds, String[] CDriverModels) {
        this.CDriverModelIds = CDriverModelIds;
        this.CDriverModels = CDriverModels;
    }

    @Override
    public List<String> getList() {
        return CDriverModelIds;
    }

    @Override
    public String[] getArray() {
        return CDriverModels;
    }
}
