package com.movtery.pojavzh.ui.activity

import com.movtery.pojavzh.feature.unpack.AbstractUnpackTask

class InstallableItem(
    val name: String,
    val summary: String?,
    val task: AbstractUnpackTask,
    var isRunning: Boolean = false,
    var isFinished: Boolean = false
)
