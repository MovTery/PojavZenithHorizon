package com.movtery.pojavzh.ui.subassembly.about;

import androidx.annotation.NonNull;

public class SponsorItemBean {
    private final String name;
    private final String time;
    private final float amount;

    public SponsorItemBean(String name, String time, float amount) {
        this.name = name;
        this.time = time;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public float getAmount() {
        return amount;
    }

    @NonNull
    @Override
    public String toString() {
        return "SponsorItemBean{" +
                "name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", amount=" + amount +
                '}';
    }
}
