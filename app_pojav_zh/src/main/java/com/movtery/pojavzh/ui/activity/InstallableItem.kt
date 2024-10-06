package com.movtery.pojavzh.ui.activity

import com.movtery.pojavzh.feature.unpack.AbstractUnpackTask

class InstallableItem(
    val name: String,
    val summary: String?,
    val task: AbstractUnpackTask,
    var isRunning: Boolean = false,
    var isFinished: Boolean = false
) : Comparable<InstallableItem> {

    override fun compareTo(other: InstallableItem): Int {
        val thisHasSummary = summary != null
        val otherHasSummary = other.summary != null

        return if (thisHasSummary && !otherHasSummary) {
            -1
        } else if (!thisHasSummary && otherHasSummary) {
            1
        } else {
            name.compareTo(other.name)
        }
    }
}