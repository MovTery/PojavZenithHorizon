package com.movtery.zalithlauncher.feature.download

import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.platform.AbstractPlatformHelper
import java.io.File

class InfoViewModel : ViewModel() {
    lateinit var platformHelper: AbstractPlatformHelper
    lateinit var infoItem: InfoItem
    var targetPath: File? = null
}