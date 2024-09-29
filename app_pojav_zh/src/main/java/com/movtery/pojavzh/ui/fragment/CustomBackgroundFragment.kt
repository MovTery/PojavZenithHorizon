package com.movtery.pojavzh.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.background.BackgroundManager
import com.movtery.pojavzh.feature.background.BackgroundType
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerView
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import com.movtery.pojavzh.utils.NewbieGuideUtils
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.setVisibilityAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.Companion.isImage
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraCore
import java.io.File

class CustomBackgroundFragment : FragmentWithAnim(R.layout.fragment_custom_background) {
    companion object {
        const val TAG: String = "CustomBackgroundFragment"
    }

    private val backgroundMap: MutableMap<BackgroundType?, String?> = HashMap()
    private var openDocumentLauncher: ActivityResultLauncher<Array<String>>? = null
    private var mBackgroundLayout: View? = null
    private var mOperateLayout: View? = null
    private var mReturnButton: ImageButton? = null
    private var mAddFileButton: ImageButton? = null
    private var mResetButton: ImageButton? = null
    private var mRefreshButton: ImageButton? = null
    private var mNothingTip: TextView? = null
    private var mTabLayout: TabLayout? = null
    private var mBackgroundPreview: ImageView? = null
    private var mFileRecyclerView: FileRecyclerView? = null
    private var backgroundType: BackgroundType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult<Array<String>, Uri>(ActivityResultContracts.OpenDocument()) { result: Uri? ->
            result?.let {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()

                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireActivity(), result, mFileRecyclerView!!.fullPath.absolutePath)
                    Tools.runOnUiThread {
                        Toast.makeText(requireActivity(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show()
                        mFileRecyclerView?.listFileAt(backgroundPath())
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)
        initBackgroundMap()
        bindTabs()

        mFileRecyclerView?.setShowFiles(true)
        mFileRecyclerView?.setShowFolders(false)

        mFileRecyclerView?.setFileSelectedListener(object : FileSelectedListener() {
            override fun onFileSelected(file: File?, path: String?) {
                val fileName = file!!.name

                val image = isImage(file)
                val filesButton = FilesButton()
                filesButton.setButtonVisibility(false, false, true, true, true, image)
                //默认虚拟鼠标不支持分享、重命名、删除操作
                val message = if (image) { //如果选中的不是一个图片，那么将显示默认的文件选择提示信息
                    getString(R.string.zh_custom_background_dialog_message, currentStatusName)
                } else {
                    getString(R.string.zh_file_message)
                }

                filesButton.setMessageText(message)
                filesButton.setMoreButtonText(getString(R.string.global_select))

                val filesDialog = FilesDialog(requireActivity(), filesButton,
                    { Tools.runOnUiThread { mFileRecyclerView?.refreshPath() } },
                    backgroundPath(), file)
                filesDialog.setMoreButtonClick {
                    backgroundMap[backgroundType] = fileName
                    BackgroundManager.getInstance()?.apply {
                        saveProperties(backgroundMap)
                    }
                    refreshBackground()

                    Toast.makeText(requireActivity(),
                        StringUtils.insertSpace(getString(R.string.zh_custom_background_selected, currentStatusName), fileName),
                        Toast.LENGTH_SHORT
                    ).show()
                    filesDialog.dismiss()
                }
                filesDialog.show()
            }

            override fun onItemLongClick(file: File?, path: String?) {
            }
        })

        mFileRecyclerView?.setRefreshListener {
            val itemCount = mFileRecyclerView!!.itemCount
            val show = itemCount == 0
            setVisibilityAnim(mNothingTip!!, show)
        }

        mTabLayout?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                refreshType(mTabLayout!!.selectedTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        mResetButton?.setOnClickListener { _: View? ->
            backgroundMap[backgroundType] = "null"
            BackgroundManager.getInstance()?.apply {
                saveProperties(backgroundMap)
            }
            Toast.makeText(requireActivity(), getString(R.string.zh_custom_background_reset, currentStatusName), Toast.LENGTH_SHORT).show()
            refreshBackground()
        }

        mReturnButton?.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        mAddFileButton?.setOnClickListener { openDocumentLauncher?.launch(arrayOf("image/*")) }
        mRefreshButton?.setOnClickListener {
            mFileRecyclerView?.listFileAt(backgroundPath())
        }

        mFileRecyclerView?.lockAndListAt(backgroundPath(), backgroundPath())

        refreshType(mTabLayout!!.selectedTabPosition)

        startNewbieGuide()
    }

    private fun startNewbieGuide() {
        if (NewbieGuideUtils.showOnlyOne(TAG)) return
        val fragmentActivity = requireActivity()
        TapTargetSequence(fragmentActivity)
            .targets(
                NewbieGuideUtils.getSimpleTarget(fragmentActivity, mRefreshButton, getString(R.string.zh_refresh), getString(R.string.zh_newbie_guide_general_refresh)),
                NewbieGuideUtils.getSimpleTarget(fragmentActivity, mResetButton, getString(R.string.cropper_reset), getString(R.string.zh_newbie_guide_background_reset)),
                NewbieGuideUtils.getSimpleTarget(fragmentActivity, mAddFileButton, getString(R.string.zh_custom_background_add), getString(R.string.zh_newbie_guide_background_import)),
                NewbieGuideUtils.getSimpleTarget(fragmentActivity, mReturnButton, getString(R.string.zh_return), getString(R.string.zh_newbie_guide_general_close)))
            .start()
    }

    private fun initBackgroundMap() {
        BackgroundManager.getInstance()?.apply {
            backgroundMap[BackgroundType.MAIN_MENU] = properties[BackgroundType.MAIN_MENU.name] as String?
            backgroundMap[BackgroundType.CUSTOM_CONTROLS] = properties[BackgroundType.CUSTOM_CONTROLS.name] as String?
            backgroundMap[BackgroundType.IN_GAME] = properties[BackgroundType.IN_GAME.name] as String?
        }
    }

    private fun backgroundPath(): File {
        val dirBackground = PathAndUrlManager.DIR_BACKGROUND
        if (!dirBackground!!.exists()) mkdirs(dirBackground)
        return dirBackground
    }

    private fun refreshBackground() {
        if (backgroundType == BackgroundType.MAIN_MENU) ExtraCore.setValue(ZHExtraConstants.MAIN_BACKGROUND_CHANGE, true)
        refreshBackgroundPreview()
    }

    private val currentStatusName: String
        get() = when (this.backgroundType) {
            BackgroundType.MAIN_MENU -> getString(R.string.zh_custom_background_main_menu)
            BackgroundType.CUSTOM_CONTROLS -> getString(R.string.zh_custom_background_controls)
            BackgroundType.IN_GAME -> getString(R.string.zh_custom_background_in_game)
            else -> getString(R.string.zh_unknown)
        }

    private fun refreshType(index: Int) {
        when (index) {
            1 -> this.backgroundType = BackgroundType.CUSTOM_CONTROLS
            2 -> this.backgroundType = BackgroundType.IN_GAME
            0 -> this.backgroundType = BackgroundType.MAIN_MENU
            else -> this.backgroundType = BackgroundType.MAIN_MENU
        }

        refreshBackgroundPreview()
    }

    private fun refreshBackgroundPreview() {
        mBackgroundPreview?.let {
            BackgroundManager.getInstance()?.getBackgroundImage(backgroundType!!)?.apply {
                Glide.with(requireActivity())
                    .load(this)
                    .fitCenter()
                    .into(DrawableImageViewTarget(it))
                return
            }

            it.setImageDrawable(null)
        }
    }

    private fun bindViews(view: View) {
        view.apply {
            mBackgroundLayout = findViewById(R.id.background_layout)
            mOperateLayout = findViewById(R.id.operate_layout)
            mTabLayout = findViewById(R.id.zh_custom_background_tab)
            mBackgroundPreview = findViewById(R.id.zh_custom_background_preview)

            mReturnButton = findViewById(R.id.zh_return_button)
            mAddFileButton = findViewById(R.id.zh_add_file_button)
            mResetButton = findViewById(R.id.zh_paste_button)
            mRefreshButton = findViewById(R.id.zh_refresh_button)
            mNothingTip = findViewById(R.id.zh_custom_background_nothing)

            findViewById<View>(R.id.zh_create_folder_button).visibility = View.GONE
            findViewById<View>(R.id.zh_search_button).visibility = View.GONE

            mFileRecyclerView = findViewById(R.id.zh_custom_background)
        }

        mResetButton?.setContentDescription(getString(R.string.cropper_reset))
        mAddFileButton?.setContentDescription(getString(R.string.zh_custom_background_add))
        mResetButton?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_reset))
        mFileRecyclerView?.setFileIcon(FileIcon.FILE)

        ZHTools.setTooltipText(mReturnButton, mReturnButton?.contentDescription)
        ZHTools.setTooltipText(mAddFileButton, mAddFileButton?.contentDescription)
        ZHTools.setTooltipText(mResetButton, mResetButton?.contentDescription)
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton?.contentDescription)
    }

    private fun bindTabs() {
        val mainMenu = mTabLayout!!.newTab()
        val controls = mTabLayout!!.newTab()
        val inGame = mTabLayout!!.newTab()

        mainMenu.setText(resources.getText(R.string.zh_custom_background_main_menu))
        controls.setText(resources.getText(R.string.zh_custom_background_controls))
        inGame.setText(resources.getText(R.string.zh_custom_background_in_game))

        mTabLayout?.addTab(mainMenu)
        mTabLayout?.addTab(controls)
        mTabLayout?.addTab(inGame)

        mTabLayout?.selectTab(mainMenu)
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(mBackgroundLayout!!, Animations.BounceInDown))
            .apply(AnimPlayer.Entry(mOperateLayout!!, Animations.BounceInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(mBackgroundLayout!!, Animations.FadeOutUp))
            .apply(AnimPlayer.Entry(mOperateLayout!!, Animations.FadeOutRight))
    }
}
