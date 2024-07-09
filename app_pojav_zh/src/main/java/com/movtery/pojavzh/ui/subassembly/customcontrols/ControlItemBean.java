package com.movtery.pojavzh.ui.subassembly.customcontrols;

public class ControlItemBean {
    private final ControlInfoData controlInfoData;
    private boolean isHighlighted = false;
    private boolean isInvalid = false;

    public ControlItemBean(ControlInfoData controlInfoData) {
        this.controlInfoData = controlInfoData;
    }

    public ControlInfoData getControlInfoData() {
        return controlInfoData;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }
}
