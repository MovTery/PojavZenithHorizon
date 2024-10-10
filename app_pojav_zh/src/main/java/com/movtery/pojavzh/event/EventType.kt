package com.movtery.pojavzh.event

enum class EventType {
    /* 触发账号管理更新事件 */
    ACCOUNT_UPDATE,
    /* 页面不透明度设置，用于刷新LauncherActivity的不透明度 */
    PAGE_OPACITY_CHANGE,
    /* LauncherActivity的背景图片变更 */
    MAIN_BACKGROUND_CHANGE,

    /* Add minecraft account procedure, the user has to select between mojang or microsoft */
    SELECT_AUTH_METHOD,
    /* When we want to launch the game */
    LAUNCH_GAME
}