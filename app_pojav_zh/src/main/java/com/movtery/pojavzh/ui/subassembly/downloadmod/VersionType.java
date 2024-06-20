package com.movtery.pojavzh.ui.subassembly.downloadmod;

public class VersionType {
    public static VersionTypeEnum getVersionType(String typeString) {
        switch (typeString) {
            case "beta":
            case "2":
                return VersionTypeEnum.BETA;
            case "alpha":
            case "3":
                return VersionTypeEnum.ALPHA;
            default:
            case "release":
            case "1":
                return VersionTypeEnum.RELEASE;
        }
    }

    public enum VersionTypeEnum {
        RELEASE, BETA, ALPHA
    }
}
