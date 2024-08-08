package com.movtery.pojavzh.ui.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean
import com.movtery.pojavzh.ui.subassembly.filelist.FileRecyclerViewCreator
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import com.movtery.pojavzh.utils.image.ImageUtils.Companion.isImage
import com.movtery.pojavzh.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import java.io.File

class CustomMouseFragment : FragmentWithAnim(R.layout.fragment_custom_mouse) {
    companion object {
        const val TAG: String = "CustomMouseFragment"
    }

    private val mData: List<FileItemBean> = ArrayList()
    private var mMouseLayout: View? = null
    private var mOperateLayout: View? = null
    private var openDocumentLauncher: ActivityResultLauncher<Array<String>>? = null
    private var mReturnButton: ImageButton? = null
    private var mAddFileButton: ImageButton? = null
    private var mRefreshButton: ImageButton? = null
    private var mMouseView: ImageView? = null
    private var fileRecyclerViewCreator: FileRecyclerViewCreator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult<Array<String>, Uri>(ActivityResultContracts.OpenDocument()) { result: Uri? ->
            result?.let{
                Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()

                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireContext(), result, mousePath().absolutePath)
                    Tools.runOnUiThread {
                        Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show()
                        loadData()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViews(view)

        mReturnButton?.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        mAddFileButton?.setOnClickListener { openDocumentLauncher?.launch(arrayOf("image/*")) }
        mRefreshButton?.setOnClickListener { loadData() }

        loadData()

        slideInAnim(this)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadData() {
        val fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(requireContext(),
            mousePath(), FileIcon.IMAGE, true, false)
        fileItemBeans.add(
            0, FileItemBean(requireContext().getDrawable(R.drawable.ic_mouse_pointer), null, getString(R.string.zh_custom_mouse_default)))
        Tools.runOnUiThread {
            fileRecyclerViewCreator?.loadData(fileItemBeans)
            //默认显示当前选中的鼠标
            refreshIcon()
        }
    }

    private fun mousePath(): File {
        val path = File(PathAndUrlManager.DIR_CUSTOM_MOUSE!!)
        if (!path.exists()) mkdirs(path)
        return path
    }

    private fun refreshIcon() {
        PojavApplication.sExecutorService.execute {
            Tools.runOnUiThread {
                mMouseView?.setImageDrawable(ZHTools.customMouse(requireContext()))
            }
        }
    }

    private fun bindViews(view: View) {
        mMouseLayout = view.findViewById(R.id.mouse_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)

        mReturnButton = view.findViewById(R.id.zh_return_button)
        mAddFileButton = view.findViewById(R.id.zh_add_file_button)
        mRefreshButton = view.findViewById(R.id.zh_refresh_button)

        mAddFileButton?.setContentDescription(getString(R.string.zh_custom_mouse_add))
        view.findViewById<View>(R.id.zh_search_button).visibility = View.GONE
        view.findViewById<View>(R.id.zh_paste_button).visibility = View.GONE
        view.findViewById<View>(R.id.zh_create_folder_button).visibility = View.GONE

        mMouseView = view.findViewById(R.id.zh_custom_mouse_icon)

        ZHTools.setTooltipText(mReturnButton, mReturnButton?.contentDescription)
        ZHTools.setTooltipText(mAddFileButton, mAddFileButton?.contentDescription)
        ZHTools.setTooltipText(mRefreshButton, mRefreshButton?.contentDescription)

        val mMouseListView = view.findViewById<RecyclerView>(R.id.zh_custom_mouse)
        fileRecyclerViewCreator = FileRecyclerViewCreator(requireContext(), mMouseListView, { position: Int, fileItemBean: FileItemBean ->
                val file = fileItemBean.file
                val fileName = file?.name
                val isDefaultMouse = position == 0

                val filesButton = FilesButton()
                filesButton.setButtonVisibility(false, false,
                    !isDefaultMouse, !isDefaultMouse, !isDefaultMouse, (isDefaultMouse || isImage(file))) //默认虚拟鼠标不支持分享、重命名、删除操作

                //如果选中的虚拟鼠标是默认的虚拟鼠标，那么将加上额外的提醒
                var message = getString(R.string.zh_file_message)
                if (isDefaultMouse) message += """
     
     ${getString(R.string.zh_custom_mouse_message_default)}
     """.trimIndent()
                filesButton.setMessageText(message)
                filesButton.setMoreButtonText(getString(R.string.global_select))

                val filesDialog = FilesDialog(requireContext(), filesButton, { this.loadData() }, mousePath(), file)
                filesDialog.setMoreButtonClick {
                    LauncherPreferences.DEFAULT_PREF.edit().putString("custom_mouse", fileName).apply()
                    refreshIcon()
                    Toast.makeText(requireContext(),
                        StringUtils.insertSpace(getString(R.string.zh_custom_mouse_added), (fileName ?: getString(R.string.zh_custom_mouse_default))),
                        Toast.LENGTH_SHORT).show()
                    filesDialog.dismiss()
                }
                filesDialog.show()
            },
            null,
            mData
        )
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mMouseLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mMouseLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
