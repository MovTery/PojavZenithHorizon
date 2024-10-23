package com.movtery.zalithlauncher.feature.download.item

/**
 * 屏幕截图信息记录
 * @param imageUrl 截图的地址
 * @param title 截图的标题
 */
class ScreenshotItem(
    val imageUrl: String,
    val title: String?
) {
    override fun toString(): String {
        return "ScreenshotItem(imageUrl='$imageUrl', title='$title')"
    }
}