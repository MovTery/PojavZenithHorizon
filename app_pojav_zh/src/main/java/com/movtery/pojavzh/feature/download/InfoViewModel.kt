package com.movtery.pojavzh.feature.download

import androidx.lifecycle.ViewModel
import com.movtery.pojavzh.feature.download.item.InfoItem
import com.movtery.pojavzh.feature.download.platform.AbstractPlatformHelper
import java.io.File

class InfoViewModel : ViewModel() {
    lateinit var platformHelper: AbstractPlatformHelper
    lateinit var infoItem: InfoItem
    var targetPath: File? = null
}