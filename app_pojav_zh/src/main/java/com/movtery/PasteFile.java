package com.movtery;

import java.io.File;

public class PasteFile {
    public static File COPY_FILE = null;
    public static PasteType PASTE_TYPE = null;

    public enum PasteType {
        COPY, MOVE
    }
}
