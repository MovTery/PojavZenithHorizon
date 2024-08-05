package com.movtery.pojavzh.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.google.android.material.tabs.TabLayout
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.subassembly.background.BackgroundManager.properties
import com.movtery.pojavzh.ui.subassembly.background.BackgroundManager.saveProperties
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerView
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils.setVisibilityAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.copyFileInBackground
import com.movtery.pojavzh.utils.file.FileTools.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.isImage
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
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
    private var mFileRecyclerView: FileRecyclerView? = null
    private var backgroundType: BackgroundType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult<Array<String>, Uri>(ActivityResultContracts.OpenDocument()) { result: Uri? ->
            result?.let {
                Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()

                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireContext(), result, mFileRecyclerView!!.fullPath.absolutePath)
                    Tools.runOnUiThread {
                        Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show()
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
                refreshType(mTabLayout!!.selectedTabPosition)

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

                val filesDialog = FilesDialog(requireContext(), filesButton,
                    { Tools.runOnUiThread { mFileRecyclerView?.refreshPath() } },
                    file
                )
                filesDialog.setMoreButtonClick {
                    backgroundMap[backgroundType] = fileName
                    saveProperties(backgroundMap)

                    Toast.makeText(requireContext(),
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

        mResetButton?.setOnClickListener { v: View? ->
            refreshType(mTabLayout!!.selectedTabPosition)
            backgroundMap[backgroundType] = "null"
            saveProperties(backgroundMap)
            Toast.makeText(requireContext(), getString(R.string.zh_custom_background_reset, currentStatusName), Toast.LENGTH_SHORT).show()
        }

        mReturnButton?.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        mAddFileButton?.setOnClickListener { openDocumentLauncher?.launch(arrayOf("image/*")) }
        mRefreshButton?.setOnClickListener {
            refreshType(mTabLayout!!.selectedTabPosition)
            mFileRecyclerView?.listFileAt(backgroundPath())
        }

        mFileRecyclerView?.lockAndListAt(backgroundPath(), backgroundPath())

        slideInAnim(this)
    }

    private fun initBackgroundMap() {
        val properties = properties
        backgroundMap[BackgroundType.MAIN_MENU] = properties[BackgroundType.MAIN_MENU.name] as String?
        backgroundMap[BackgroundType.SETTINGS] = properties[BackgroundType.SETTINGS.name] as String?
        backgroundMap[BackgroundType.CUSTOM_CONTROLS] = properties[BackgroundType.CUSTOM_CONTROLS.name] as String?
        backgroundMap[BackgroundType.IN_GAME] = properties[BackgroundType.IN_GAME.name] as String?
    }

    private fun backgroundPath(): File {
        if (!ZHTools.DIR_BACKGROUND.exists()) mkdirs(ZHTools.DIR_BACKGROUND)
        return ZHTools.DIR_BACKGROUND
    }

    private val currentStatusName: String
        get() = when (this.backgroundType) {
            BackgroundType.MAIN_MENU -> getString(R.string.zh_custom_background_main_menu)
            BackgroundType.SETTINGS -> getString(R.string.zh_custom_background_settings)
            BackgroundType.CUSTOM_CONTROLS -> getString(R.string.zh_custom_background_controls)
            BackgroundType.IN_GAME -> getString(R.string.zh_custom_background_in_game)
            else -> getString(R.string.zh_unknown)
        }

    private fun refreshType(index: Int) {
        when (index) {
            1 -> this.backgroundType = BackgroundType.SETTINGS
            2 -> this.backgroundType = BackgroundType.CUSTOM_CONTROLS
            3 -> this.backgroundType = BackgroundType.IN_GAME
            0 -> this.backgroundType = BackgroundType.MAIN_MENU
            else -> this.backgroundType = BackgroundType.MAIN_MENU
        }
    }

    private fun bindViews(view: View) {
        mBackgroundLayout = view.findViewById(R.id.background_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)
        mTabLayout = view.findViewById(R.id.zh_custom_background_tab)

        mReturnButton = view.findViewById(R.id.zh_return_button)
        mAddFileButton = view.findViewById(R.id.zh_add_file_button)
        mResetButton = view.findViewById(R.id.zh_paste_button)
        mRefreshButton = view.findViewById(R.id.zh_refresh_button)
        mNothingTip = view.findViewById(R.id.zh_custom_background_nothing)

        mResetButton?.setContentDescription(getString(R.string.cropper_reset))
        mAddFileButton?.setContentDescription(getString(R.string.zh_custom_background_add))
        mResetButton?.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_reset))

        view.findViewById<View>(R.id.zh_create_folder_button).visibility = View.GONE
        view.findViewById<View>(R.id.zh_search_button).visibility = View.GONE

        mFileRecyclerView = view.findViewById(R.id.zh_custom_background)
        mFileRecyclerView?.setFileIcon(FileIcon.FILE)

        ZHTools.setTooltipText(mReturnButton, mReturnButton?.contentDescription)
        ZHTools.setTooltipText(mAddFileButton, mAddFileButton?.contentDescription)
        ZHTools.setTooltipText(mResetButton, mResetButton?.contentDescription)
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton?.contentDescription)
    }

    private fun bindTabs() {
        val mainMenu = mTabLayout!!.newTab()
        val settings = mTabLayout!!.newTab()
        val controls = mTabLayout!!.newTab()
        val inGame = mTabLayout!!.newTab()

        mainMenu.setText(resources.getText(R.string.zh_custom_background_main_menu))
        settings.setText(resources.getText(R.string.zh_custom_background_settings))
        controls.setText(resources.getText(R.string.zh_custom_background_controls))
        inGame.setText(resources.getText(R.string.zh_custom_background_in_game))

        mTabLayout?.addTab(mainMenu)
        mTabLayout?.addTab(settings)
        mTabLayout?.addTab(controls)
        mTabLayout?.addTab(inGame)

        mTabLayout?.selectTab(mainMenu)
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mBackgroundLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mBackgroundLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
