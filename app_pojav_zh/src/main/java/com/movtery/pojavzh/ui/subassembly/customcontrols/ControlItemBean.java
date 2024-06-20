package com.movtery.pojavzh.ui.subassembly.customcontrols;

public class ControlItemBean {
    private ControlInfoData controlInfoData;
    private boolean isHighlighted = false;

    public ControlItemBean(ControlInfoData controlInfoData) {
        this.controlInfoData = controlInfoData;
    }

    public ControlInfoData getControlInfoData() {
        return controlInfoData;
    }

    public void setControlInfoData(ControlInfoData controlInfoData) {
        this.controlInfoData = controlInfoData;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
}
