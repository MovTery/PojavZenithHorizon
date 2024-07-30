package com.movtery.pojavzh.ui.subassembly.filelist

import java.io.File

abstract class FileSelectedListener {
    abstract fun onFileSelected(file: File?, path: String?)

    abstract fun onItemLongClick(file: File?, path: String?)
}
